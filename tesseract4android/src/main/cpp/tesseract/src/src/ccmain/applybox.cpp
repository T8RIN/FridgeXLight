/**********************************************************************
 * File:        applybox.cpp  (Formerly applybox.c)
 * Description: Re segment rows according to box file data
 * Author:      Phil Cheatle
 *
 * (C) Copyright 1993, Hewlett-Packard Ltd.
 ** Licensed under the Apache License, Version 2.0 (the "License");
 ** you may not use this file except in compliance with the License.
 ** You may obtain a copy of the License at
 ** http://www.apache.org/licenses/LICENSE-2.0
 ** Unless required by applicable law or agreed to in writing, software
 ** distributed under the License is distributed on an "AS IS" BASIS,
 ** WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 ** See the License for the specific language governing permissions and
 ** limitations under the License.
 *
 **********************************************************************/

#ifndef DISABLED_LEGACY_ENGINE
#  include <allheaders.h>
#  include <cctype>
#  include <cerrno>
#  include <cstring>
#  include "boxread.h"
#endif // ndef DISABLED_LEGACY_ENGINE
#include <tesseract/unichar.h>
#include "pageres.h"
#include "tesseractclass.h"
#include "unicharset.h"

#ifndef DISABLED_LEGACY_ENGINE
/** Max number of blobs to classify together in FindSegmentation. */
const int kMaxGroupSize = 4;
/// Max fraction of median allowed as deviation in xheight before switching
/// to median.
const double kMaxXHeightDeviationFraction = 0.125;
#endif // ndef DISABLED_LEGACY_ENGINE

/**
 * The box file is assumed to contain box definitions, one per line, of the
 * following format for blob-level boxes:
 * @verbatim
 *   <UTF8 str> <left> <bottom> <right> <top> <page id>
 * @endverbatim
 * and for word/line-level boxes:
 * @verbatim
 *   WordStr <left> <bottom> <right> <top> <page id> #<space-delimited word str>
 * @endverbatim
 * NOTES:
 * The boxes use tesseract coordinates, i.e. 0,0 is at BOTTOM-LEFT.
 *
 * <page id> is 0-based, and the page number is used for multipage input (tiff).
 *
 * In the blob-level form, each line represents a recognizable unit, which may
 * be several UTF-8 bytes, but there is a bounding box around each recognizable
 * unit, and no classifier is needed to train in this mode (bootstrapping.)
 *
 * In the word/line-level form, the line begins with the literal "WordStr", and
 * the bounding box bounds either a whole line or a whole word. The recognizable
 * units in the word/line are listed after the # at the end of the line and
 * are space delimited, ignoring any original spaces on the line.
 * Eg.
 * @verbatim
 * word -> #w o r d
 * multi word line -> #m u l t i w o r d l i n e
 * @endverbatim
 * The recognizable units must be space-delimited in order to allow multiple
 * unicodes to be used for a single recognizable unit, eg Hindi.
 *
 * In this mode, the classifier must have been pre-trained with the desired
 * character set, or it will not be able to find the character segmentations.
 */

namespace tesseract {

#ifndef DISABLED_LEGACY_ENGINE
static void clear_any_old_text(BLOCK_LIST *block_list) {
  BLOCK_IT block_it(block_list);
  for (block_it.mark_cycle_pt(); !block_it.cycled_list(); block_it.forward()) {
    ROW_IT row_it(block_it.data()->row_list());
    for (row_it.mark_cycle_pt(); !row_it.cycled_list(); row_it.forward()) {
      WERD_IT word_it(row_it.data()->word_list());
      for (word_it.mark_cycle_pt(); !word_it.cycled_list(); word_it.forward()) {
        word_it.data()->set_text("");
      }
    }
  }
}

// Applies the box file based on the image name filename, and resegments
// the words in the block_list (page), with:
// blob-mode: one blob per line in the box file, words as input.
// word/line-mode: one blob per space-delimited unit after the #, and one word
// per line in the box file. (See comment above for box file format.)
// If find_segmentation is true, (word/line mode) then the classifier is used
// to re-segment words/lines to match the space-delimited truth string for
// each box. In this case, the input box may be for a word or even a whole
// text line, and the output words will contain multiple blobs corresponding
// to the space-delimited input string.
// With find_segmentation false, no classifier is needed, but the chopper
// can still be used to correctly segment touching characters with the help
// of the input boxes.
// In the returned PAGE_RES, the WERD_RES are setup as they would be returned
// from normal classification, ie. with a word, chopped_word, rebuild_word,
// seam_array, denorm, box_word, and best_state, but NO best_choice or
// raw_choice, as they would require a UNICHARSET, which we aim to avoid.
// Instead, the correct_text member of WERD_RES is set, and this may be later
// converted to a best_choice using CorrectClassifyWords. CorrectClassifyWords
// is not required before calling ApplyBoxTraining.
PAGE_RES *Tesseract::ApplyBoxes(const char *filename, bool find_segmentation,
                                BLOCK_LIST *block_list) {
  std::vector<TBOX> boxes;
  std::vector<std::string> texts, full_texts;
  if (!ReadAllBoxes(applybox_page, true, filename, &boxes, &texts, &full_texts, nullptr)) {
    return nullptr; // Can't do it.
  }

  const int box_count = boxes.size();
  int box_failures = 0;

  // In word mode, we use the boxes to make a word for each box, but
  // in blob mode we use the existing words and maximally chop them first.
  PAGE_RES *page_res = find_segmentation ? nullptr : SetupApplyBoxes(boxes, block_list);
  clear_any_old_text(block_list);

  for (int i = 0; i < box_count; i++) {
    bool foundit = false;
    if (page_res != nullptr) {
      foundit =
          ResegmentCharBox(page_res, (i == 0) ? nullptr : &boxes[i - 1], boxes[i],
                           (i == box_count - 1) ? nullptr : &boxes[i + 1], full_texts[i].c_str());
    } else {
      foundit = ResegmentWordBox(block_list, boxes[i],
                                 (i == box_count - 1) ? nullptr : &boxes[i + 1], texts[i].c_str());
    }
    if (!foundit) {
      box_failures++;
      ReportFailedBox(i, boxes[i], texts[i].c_str(), "FAILURE! Couldn't find a matching blob");
    }
  }

  if (page_res == nullptr) {
    // In word/line mode, we now maximally chop all the words and resegment
    // them with the classifier.
    page_res = SetupApplyBoxes(boxes, block_list);
    ReSegmentByClassification(page_res);
  }
  if (applybox_debug > 0) {
    tprintf("APPLY_BOXES:\n");
    tprintf("   Boxes read from boxfile:  %6d\n", box_count);
    if (box_failures > 0) {
      tprintf("   Boxes failed resegmentation:  %6d\n", box_failures);
    }
  }
  TidyUp(page_res);
  return page_res;
}

// Helper computes median xheight in the image.
static double MedianXHeight(BLOCK_LIST *block_list) {
  BLOCK_IT block_it(block_list);
  STATS xheights(0, block_it.data()->pdblk.bounding_box().height());
  for (block_it.mark_cycle_pt(); !block_it.cycled_list(); block_it.forward()) {
    ROW_IT row_it(block_it.data()->row_list());
    for (row_it.mark_cycle_pt(); !row_it.cycled_list(); row_it.forward()) {
      xheights.add(IntCastRounded(row_it.data()->x_height()), 1);
    }
  }
  return xheights.median();
}

/// Any row xheight that is significantly different from the median is set
/// to the median.
void Tesseract::PreenXHeights(BLOCK_LIST *block_list) {
  const double median_xheight = MedianXHeight(block_list);
  const double max_deviation = kMaxXHeightDeviationFraction * median_xheight;
  // Strip all fuzzy space markers to simplify the PAGE_RES.
  BLOCK_IT b_it(block_list);
  for (b_it.mark_cycle_pt(); !b_it.cycled_list(); b_it.forward()) {
    BLOCK *block = b_it.data();
    ROW_IT r_it(block->row_list());
    for (r_it.mark_cycle_pt(); !r_it.cycled_list(); r_it.forward()) {
      ROW *row = r_it.data();
      const double diff = fabs(row->x_height() - median_xheight);
      if (diff > max_deviation) {
        if (applybox_debug) {
          tprintf("row xheight=%g, but median xheight = %g\n", row->x_height(), median_xheight);
        }
        row->set_x_height(static_cast<float>(median_xheight));
      }
    }
  }
}

/// Builds a PAGE_RES from the block_list in the way required for ApplyBoxes:
/// All fuzzy spaces are removed, and all the words are maximally chopped.
PAGE_RES *Tesseract::SetupApplyBoxes(const std::vector<TBOX> &boxes, BLOCK_LIST *block_list) {
  PreenXHeights(block_list);
  // Strip all fuzzy space markers to simplify the PAGE_RES.
  BLOCK_IT b_it(block_list);
  for (b_it.mark_cycle_pt(); !b_it.cycled_list(); b_it.forward()) {
    BLOCK *block = b_it.data();
    ROW_IT r_it(block->row_list());
    for (r_it.mark_cycle_pt(); !r_it.cycled_list(); r_it.forward()) {
      ROW *row = r_it.data();
      WERD_IT w_it(row->word_list());
      for (w_it.mark_cycle_pt(); !w_it.cycled_list(); w_it.forward()) {
        WERD *word = w_it.data();
        if (word->cblob_list()->empty()) {
          delete w_it.extract();
        } else {
          word->set_flag(W_FUZZY_SP, false);
          word->set_flag(W_FUZZY_NON, false);
        }
      }
    }
  }
  auto *page_res = new PAGE_RES(false, block_list, nullptr);
  PAGE_RES_IT pr_it(page_res);
  WERD_RES *word_res;
  while ((word_res = pr_it.word()) != nullptr) {
    MaximallyChopWord(boxes, pr_it.block()->block, pr_it.row()->row, word_res);
    pr_it.forward();
  }
  return page_res;
}

/// Tests the chopper by exhaustively running chop_one_blob.
/// The word_res will contain filled chopped_word, seam_array, denorm,
/// box_word and best_state for the maximally chopped word.
void Tesseract::MaximallyChopWord(const std::vector<TBOX> &boxes, BLOCK *block, ROW *row,
                                  WERD_RES *word_res) {
  if (!word_res->SetupForRecognition(unicharset, this, BestPix(), tessedit_ocr_engine_mode, nullptr,
                                     classify_bln_numeric_mode, textord_use_cjk_fp_model,
                                     poly_allow_detailed_fx, row, block)) {
    word_res->CloneChoppedToRebuild();
    return;
  }
  if (chop_debug) {
    tprintf("Maximally chopping word at:");
    word_res->word->bounding_box().print();
  }
  std::vector<BLOB_CHOICE *> blob_choices;
  ASSERT_HOST(!word_res->chopped_word->blobs.empty());
  auto rating = static_cast<float>(INT8_MAX);
  for (unsigned i = 0; i < word_res->chopped_word->NumBlobs(); ++i) {
    // The rating and certainty are not quite arbitrary. Since
    // select_blob_to_chop uses the worst certainty to choose, they all have
    // to be different, so starting with INT8_MAX, subtract 1/8 for each blob
    // in here, and then divide by e each time they are chopped, which
    // should guarantee a set of unequal values for the whole tree of blobs
    // produced, however much chopping is required. The chops are thus only
    // limited by the ability of the chopper to find suitable chop points,
    // and not by the value of the certainties.
    auto *choice = new BLOB_CHOICE(0, rating, -rating, -1, 0.0f, 0.0f, 0.0f, BCC_FAKE);
    blob_choices.push_back(choice);
    rating -= 0.125f;
  }
  const double e = exp(1.0); // The base of natural logs.
  unsigned blob_number;
  int right_chop_index = 0;
  if (!assume_fixed_pitch_char_segment) {
    // We only chop if the language is not fixed pitch like CJK.
    SEAM *seam = nullptr;
    while ((seam = chop_one_blob(boxes, blob_choices, word_res, &blob_number)) != nullptr) {
      word_res->InsertSeam(blob_number, seam);
      BLOB_CHOICE *left_choice = blob_choices[blob_number];
      rating = left_choice->rating() / e;
      left_choice->set_rating(rating);
      left_choice->set_certainty(-rating);
      // combine confidence w/ serial #
      auto *right_choice = new BLOB_CHOICE(++right_chop_index, rating - 0.125f, -rating, -1, 0.0f,
                                           0.0f, 0.0f, BCC_FAKE);
      blob_choices.insert(blob_choices.begin() + blob_number + 1, right_choice);
    }
  }
  word_res->CloneChoppedToRebuild();
  word_res->FakeClassifyWord(blob_choices.size(), &blob_choices[0]);
}

/// Helper to compute the dispute resolution metric.
/// Disputed blob resolution. The aim is to give the blob to the most
/// appropriate boxfile box. Most of the time it is obvious, but if
/// two boxfile boxes overlap significantly it is not. If a small boxfile
/// box takes most of the blob, and a large boxfile box does too, then
/// we want the small boxfile box to get it, but if the small box
/// is much smaller than the blob, we don't want it to get it.
/// Details of the disputed blob resolution:
/// Given a box with area A, and a blob with area B, with overlap area C,
/// then the miss metric is (A-C)(B-C)/(AB) and the box with minimum
/// miss metric gets the blob.
static double BoxMissMetric(const TBOX &box1, const TBOX &box2) {
  const int overlap_area = box1.intersection(box2).area();
  const int a = box1.area();
  const int b = box2.area();
  ASSERT_HOST(a != 0 && b != 0);
  return 1.0 * (a - overlap_area) * (b - overlap_area) / a / b;
}

/// Gather consecutive blobs that match the given box into the best_state
/// and corresponding correct_text.
///
/// Fights over which box owns which blobs are settled by pre-chopping and
/// applying the blobs to box or next_box with the least non-overlap.
/// @return false if the box was in error, which can only be caused by
/// failing to find an appropriate blob for a box.
///
/// This means that occasionally, blobs may be incorrectly segmented if the
/// chopper fails to find a suitable chop point.
bool Tesseract::ResegmentCharBox(PAGE_RES *page_res, const TBOX *prev_box, const TBOX &box,
                                 const TBOX *next_box, const char *correct_text) {
  if (applybox_debug > 1) {
    tprintf("\nAPPLY_BOX: in ResegmentCharBox() for %s\n", correct_text);
  }
  PAGE_RES_IT page_res_it(page_res);
  WERD_RES *word_res;
  for (word_res = page_res_it.word(); word_res != nullptr; word_res = page_res_it.forward()) {
    if (!word_res->box_word->bounding_box().major_overlap(box)) {
      continue;
    }
    if (applybox_debug > 1) {
      tprintf("Checking word box:");
      word_res->box_word->bounding_box().print();
    }
    int word_len = word_res->box_word->length();
    for (int i = 0; i < word_len; ++i) {
      TBOX char_box = TBOX();
      int blob_count = 0;
      for (blob_count = 0; i + blob_count < word_len; ++blob_count) {
        TBOX blob_box = word_res->box_word->BlobBox(i + blob_count);
        if (!blob_box.major_overlap(box)) {
          break;
        }
        if (word_res->correct_text[i + blob_count].length() > 0) {
          break; // Blob is claimed already.
        }
        if (next_box != nullptr) {
          const double current_box_miss_metric = BoxMissMetric(blob_box, box);
          const double next_box_miss_metric = BoxMissMetric(blob_box, *next_box);
          if (applybox_debug > 2) {
            tprintf("Checking blob:");
            blob_box.print();
            tprintf("Current miss metric = %g, next = %g\n", current_box_miss_metric,
                    next_box_miss_metric);
          }
          if (current_box_miss_metric > next_box_miss_metric) {
            break; // Blob is a better match for next box.
          }
        }
        char_box += blob_box;
      }
      if (blob_count > 0) {
        if (applybox_debug > 1) {
          tprintf("Index [%d, %d) seem good.\n", i, i + blob_count);
        }
        if (!char_box.almost_equal(box, 3) &&
            ((next_box != nullptr && box.x_gap(*next_box) < -3) ||
             (prev_box != nullptr && prev_box->x_gap(box) < -3))) {
          return false;
        }
        // We refine just the box_word, best_state and correct_text here.
        // The rebuild_word is made in TidyUp.
        // blob_count blobs are put together to match the box. Merge the
        // box_word boxes, save the blob_count in the state and the text.
        word_res->box_word->MergeBoxes(i, i + blob_count);
        word_res->best_state[i] = blob_count;
        word_res->correct_text[i] = correct_text;
        if (applybox_debug > 2) {
          tprintf("%d Blobs match: blob box:", blob_count);
          word_res->box_word->BlobBox(i).print();
          tprintf("Matches box:");
          box.print();
          if (next_box != nullptr) {
            tprintf("With next box:");
            next_box->print();
          }
        }
        // Eliminated best_state and correct_text entries for the consumed
        // blobs.
        for (int j = 1; j < blob_count; ++j) {
          word_res->best_state.erase(word_res->best_state.begin() + i + 1);
          word_res->correct_text.erase(word_res->correct_text.begin() + i + 1);
        }
        // Assume that no box spans multiple source words, so we are done with
        // this box.
        if (applybox_debug > 1) {
          tprintf("Best state = ");
          for (auto best_state : word_res->best_state) {
            tprintf("%d ", best_state);
          }
          tprintf("\n");
          tprintf("Correct text = [[ ");
          for (auto &it : word_res->correct_text) {
            tprintf("%s ", it.c_str());
          }
          tprintf("]]\n");
        }
        return true;
      }
    }
  }
  if (applybox_debug > 0) {
    tprintf("FAIL!\n");
  }
  return false; // Failure.
}

/// Consume all source blobs that strongly overlap the given box,
/// putting them into a new word, with the correct_text label.
/// Fights over which box owns which blobs are settled by
/// applying the blobs to box or next_box with the least non-overlap.
/// @return false if the box was in error, which can only be caused by
/// failing to find an overlapping blob for a box.
bool Tesseract::ResegmentWordBox(BLOCK_LIST *block_list, const TBOX &box, const TBOX *next_box,
                                 const char *correct_text) {
  if (applybox_debug > 1) {
    tprintf("\nAPPLY_BOX: in ResegmentWordBox() for %s\n", correct_text);
  }
  WERD *new_word = nullptr;
  BLOCK_IT b_it(block_list);
  for (b_it.mark_cycle_pt(); !b_it.cycled_list(); b_it.forward()) {
    BLOCK *block = b_it.data();
    if (!box.major_overlap(block->pdblk.bounding_box())) {
      continue;
    }
    ROW_IT r_it(block->row_list());
    for (r_it.mark_cycle_pt(); !r_it.cycled_list(); r_it.forward()) {
      ROW *row = r_it.data();
      if (!box.major_overlap(row->bounding_box())) {
        continue;
      }
      WERD_IT w_it(row->word_list());
      for (w_it.mark_cycle_pt(); !w_it.cycled_list(); w_it.forward()) {
        WERD *word = w_it.data();
        if (applybox_debug > 2) {
          tprintf("Checking word:");
          word->bounding_box().print();
        }
        if (word->text() != nullptr && word->text()[0] != '\0') {
          continue; // Ignore words that are already done.
        }
        if (!box.major_overlap(word->bounding_box())) {
          continue;
        }
        C_BLOB_IT blob_it(word->cblob_list());
        for (blob_it.mark_cycle_pt(); !blob_it.cycled_list(); blob_it.forward()) {
          C_BLOB *blob = blob_it.data();
          TBOX blob_box = blob->bounding_box();
          if (!blob_box.major_overlap(box)) {
            continue;
          }
          if (next_box != nullptr) {
            const double current_box_miss_metric = BoxMissMetric(blob_box, box);
            const double next_box_miss_metric = BoxMissMetric(blob_box, *next_box);
            if (applybox_debug > 2) {
              tprintf("Checking blob:");
              blob_box.print();
              tprintf("Current miss metric = %g, next = %g\n", current_box_miss_metric,
                      next_box_miss_metric);
            }
            if (current_box_miss_metric > next_box_miss_metric) {
              continue; // Blob is a better match for next box.
            }
          }
          if (applybox_debug > 2) {
            tprintf("Blob match: blob:");
            blob_box.print();
            tprintf("Matches box:");
            box.print();
            if (next_box != nullptr) {
              tprintf("With next box:");
              next_box->print();
            }
          }
          if (new_word == nullptr) {
            // Make a new word with a single blob.
            new_word = word->shallow_copy();
            new_word->set_text(correct_text);
            w_it.add_to_end(new_word);
          }
          C_BLOB_IT new_blob_it(new_word->cblob_list());
          new_blob_it.add_to_end(blob_it.extract());
        }
      }
    }
  }
  if (new_word == nullptr && applybox_debug > 0) {
    tprintf("FAIL!\n");
  }
  return new_word != nullptr;
}

/// Resegments the words by running the classifier in an attempt to find the
/// correct segmentation that produces the required string.
void Tesseract::ReSegmentByClassification(PAGE_RES *page_res) {
  PAGE_RES_IT pr_it(page_res);
  WERD_RES *word_res;
  for (; (word_res = pr_it.word()) != nullptr; pr_it.forward()) {
    const WERD *word = word_res->word;
    if (word->text() == nullptr || word->text()[0] == '\0') {
      continue; // Ignore words that have no text.
    }
    // Convert the correct text to a vector of UNICHAR_ID
    std::vector<UNICHAR_ID> target_text;
    if (!ConvertStringToUnichars(word->text(), &target_text)) {
      tprintf("APPLY_BOX: FAILURE: can't find class_id for '%s'\n", word->text());
      pr_it.DeleteCurrentWord();
      continue;
    }
    if (!FindSegmentation(target_text, word_res)) {
      tprintf("APPLY_BOX: FAILURE: can't find segmentation for '%s'\n", word->text());
      pr_it.DeleteCurrentWord();
      continue;
    }
  }
}

/// Converts the space-delimited string of utf8 text to a vector of UNICHAR_ID.
/// @return false if an invalid UNICHAR_ID is encountered.
bool Tesseract::ConvertStringToUnichars(const char *utf8, std::vector<UNICHAR_ID> *class_ids) {
  for (int step = 0; *utf8 != '\0'; utf8 += step) {
    const char *next_space = strchr(utf8, ' ');
    if (next_space == nullptr) {
      next_space = utf8 + strlen(utf8);
    }
    step = next_space - utf8;
    UNICHAR_ID class_id = unicharset.unichar_to_id(utf8, step);
    if (class_id == INVALID_UNICHAR_ID) {
      return false;
    }
    while (utf8[step] == ' ') {
      ++step;
    }
    class_ids->push_back(class_id);
  }
  return true;
}

/// Resegments the word to achieve the target_text from the classifier.
/// Returns false if the re-segmentation fails.
/// Uses brute-force combination of up to #kMaxGroupSize adjacent blobs, and
/// applies a full search on the classifier results to find the best classified
/// segmentation. As a compromise to obtain better recall, 1-1 ambiguity
/// substitutions ARE used.
bool Tesseract::FindSegmentation(const std::vector<UNICHAR_ID> &target_text, WERD_RES *word_res) {
  // Classify all required combinations of blobs and save results in choices.
  const int word_length = word_res->box_word->length();
  auto *choices = new std::vector<BLOB_CHOICE_LIST *>[word_length];
  for (int i = 0; i < word_length; ++i) {
    for (int j = 1; j <= kMaxGroupSize && i + j <= word_length; ++j) {
      BLOB_CHOICE_LIST *match_result =
          classify_piece(word_res->seam_array, i, i + j - 1, "Applybox", word_res->chopped_word,
                         word_res->blamer_bundle);
      if (applybox_debug > 2) {
        tprintf("%d+%d:", i, j);
        print_ratings_list("Segment:", match_result, unicharset);
      }
      choices[i].push_back(match_result);
    }
  }
  // Search the segmentation graph for the target text. Must be an exact
  // match. Using wildcards makes it difficult to find the correct
  // segmentation even when it is there.
  word_res->best_state.clear();
  std::vector<int> search_segmentation;
  float best_rating = 0.0f;
  SearchForText(choices, 0, word_length, target_text, 0, 0.0f, &search_segmentation, &best_rating,
                &word_res->best_state);
  for (int i = 0; i < word_length; ++i) {
    for (auto choice : choices[i]) {
      delete choice;
    }
  }
  delete[] choices;
  if (word_res->best_state.empty()) {
    // Build the original segmentation and if it is the same length as the
    // truth, assume it will do.
    int blob_count = 1;
    for (auto s : word_res->seam_array) {
      SEAM *seam = s;
      if (!seam->HasAnySplits()) {
        word_res->best_state.push_back(blob_count);
        blob_count = 1;
      } else {
        ++blob_count;
      }
    }
    word_res->best_state.push_back(blob_count);
    if (word_res->best_state.size() != target_text.size()) {
      word_res->best_state.clear(); // No good. Original segmentation bad size.
      return false;
    }
  }
  word_res->correct_text.clear();
  for (auto &text : target_text) {
    word_res->correct_text.emplace_back(unicharset.id_to_unichar(text));
  }
  return true;
}

/// Recursive helper to find a match to the target_text (from text_index
/// position) in the choices (from choices_pos position).
/// @param choices is an array of vectors of length choices_length,
/// with each element representing a starting position in the word, and the
/// #vector holding classification results for a sequence of consecutive
/// blobs, with index 0 being a single blob, index 1 being 2 blobs etc.
/// @param choices_pos
/// @param choices_length
/// @param target_text
/// @param text_index
/// @param rating
/// @param segmentation
/// @param best_rating
/// @param best_segmentation
void Tesseract::SearchForText(const std::vector<BLOB_CHOICE_LIST *> *choices, int choices_pos,
                              unsigned choices_length, const std::vector<UNICHAR_ID> &target_text,
                              unsigned text_index, float rating, std::vector<int> *segmentation,
                              float *best_rating, std::vector<int> *best_segmentation) {
  const UnicharAmbigsVector &table = getDict().getUnicharAmbigs().dang_ambigs();
  for (unsigned length = 1; length <= choices[choices_pos].size(); ++length) {
    // Rating of matching choice or worst choice if no match.
    float choice_rating = 0.0f;
    // Find the corresponding best BLOB_CHOICE.
    BLOB_CHOICE_IT choice_it(choices[choices_pos][length - 1]);
    for (choice_it.mark_cycle_pt(); !choice_it.cycled_list(); choice_it.forward()) {
      const BLOB_CHOICE *choice = choice_it.data();
      choice_rating = choice->rating();
      auto class_id = choice->unichar_id();
      if (class_id == target_text[text_index]) {
        break;
      }
      // Search ambigs table.
      if (static_cast<size_t>(class_id) < table.size() && table[class_id] != nullptr) {
        AmbigSpec_IT spec_it(table[class_id]);
        for (spec_it.mark_cycle_pt(); !spec_it.cycled_list(); spec_it.forward()) {
          const AmbigSpec *ambig_spec = spec_it.data();
          // We'll only do 1-1.
          if (ambig_spec->wrong_ngram[1] == INVALID_UNICHAR_ID &&
              ambig_spec->correct_ngram_id == target_text[text_index]) {
            break;
          }
        }
        if (!spec_it.cycled_list()) {
          break; // Found an ambig.
        }
      }
    }
    if (choice_it.cycled_list()) {
      continue; // No match.
    }
    segmentation->push_back(length);
    if (choices_pos + length == choices_length && text_index + 1 == target_text.size()) {
      // This is a complete match. If the rating is good record a new best.
      if (applybox_debug > 2) {
        tprintf("Complete match, rating = %g, best=%g, seglength=%zu, best=%zu\n",
                rating + choice_rating, *best_rating, segmentation->size(),
                best_segmentation->size());
      }
      if (best_segmentation->empty() || rating + choice_rating < *best_rating) {
        *best_segmentation = *segmentation;
        *best_rating = rating + choice_rating;
      }
    } else if (choices_pos + length < choices_length && text_index + 1 < target_text.size()) {
      if (applybox_debug > 3) {
        tprintf("Match found for %d=%s:%s, at %d+%d, recursing...\n", target_text[text_index],
                unicharset.id_to_unichar(target_text[text_index]),
                choice_it.data()->unichar_id() == target_text[text_index] ? "Match" : "Ambig",
                choices_pos, length);
      }
      SearchForText(choices, choices_pos + length, choices_length, target_text, text_index + 1,
                    rating + choice_rating, segmentation, best_rating, best_segmentation);
      if (applybox_debug > 3) {
        tprintf("End recursion for %d=%s\n", target_text[text_index],
                unicharset.id_to_unichar(target_text[text_index]));
      }
    }
    segmentation->resize(segmentation->size() - 1);
  }
}

/// - Counts up the labelled words and the blobs within.
/// - Deletes all unused or emptied words, counting the unused ones.
/// - Resets W_BOL and W_EOL flags correctly.
/// - Builds the rebuild_word and rebuilds the box_word and the best_choice.
void Tesseract::TidyUp(PAGE_RES *page_res) {
  int ok_blob_count = 0;
  int bad_blob_count = 0;
  int ok_word_count = 0;
  int unlabelled_words = 0;
  PAGE_RES_IT pr_it(page_res);
  WERD_RES *word_res;
  for (; (word_res = pr_it.word()) != nullptr; pr_it.forward()) {
    int ok_in_word = 0;
    int blob_count = word_res->correct_text.size();
    auto *word_choice = new WERD_CHOICE(word_res->uch_set, blob_count);
    word_choice->set_permuter(TOP_CHOICE_PERM);
    for (int c = 0; c < blob_count; ++c) {
      if (word_res->correct_text[c].length() > 0) {
        ++ok_in_word;
      }
      // Since we only need a fake word_res->best_choice, the actual
      // unichar_ids do not matter. Which is fortunate, since TidyUp()
      // can be called while training Tesseract, at the stage where
      // unicharset is not meaningful yet.
      word_choice->append_unichar_id_space_allocated(INVALID_UNICHAR_ID, word_res->best_state[c],
                                                     1.0f, -1.0f);
    }
    if (ok_in_word > 0) {
      ok_blob_count += ok_in_word;
      bad_blob_count += word_res->correct_text.size() - ok_in_word;
      word_res->LogNewRawChoice(word_choice);
      word_res->LogNewCookedChoice(1, false, word_choice);
    } else {
      ++unlabelled_words;
      if (applybox_debug > 0) {
        tprintf("APPLY_BOXES: Unlabelled word at :");
        word_res->word->bounding_box().print();
      }
      pr_it.DeleteCurrentWord();
      delete word_choice;
    }
  }
  pr_it.restart_page();
  for (; (word_res = pr_it.word()) != nullptr; pr_it.forward()) {
    // Denormalize back to a BoxWord.
    word_res->RebuildBestState();
    word_res->SetupBoxWord();
    word_res->word->set_flag(W_BOL, pr_it.prev_row() != pr_it.row());
    word_res->word->set_flag(W_EOL, pr_it.next_row() != pr_it.row());
  }
  if (applybox_debug > 0) {
    tprintf("   Found %d good blobs.\n", ok_blob_count);
    if (bad_blob_count > 0) {
      tprintf("   Leaving %d unlabelled blobs in %d words.\n", bad_blob_count, ok_word_count);
    }
    if (unlabelled_words > 0) {
      tprintf("   %d remaining unlabelled words deleted.\n", unlabelled_words);
    }
  }
}

/** Logs a bad box by line in the box file and box coords.*/
void Tesseract::ReportFailedBox(int boxfile_lineno, TBOX box, const char *box_ch,
                                const char *err_msg) {
  tprintf("APPLY_BOXES: boxfile line %d/%s ((%d,%d),(%d,%d)): %s\n", boxfile_lineno + 1, box_ch,
          box.left(), box.bottom(), box.right(), box.top(), err_msg);
}

/// Calls #LearnWord to extract features for labelled blobs within each word.
/// Features are stored in an internal buffer.
void Tesseract::ApplyBoxTraining(const std::string &fontname, PAGE_RES *page_res) {
  PAGE_RES_IT pr_it(page_res);
  int word_count = 0;
  for (WERD_RES *word_res = pr_it.word(); word_res != nullptr; word_res = pr_it.forward()) {
    LearnWord(fontname.c_str(), word_res);
    ++word_count;
  }
  tprintf("Generated training data for %d words\n", word_count);
}

#endif // ndef DISABLED_LEGACY_ENGINE

/** Creates a fake best_choice entry in each WERD_RES with the correct text.*/
void Tesseract::CorrectClassifyWords(PAGE_RES *page_res) {
  PAGE_RES_IT pr_it(page_res);
  for (WERD_RES *word_res = pr_it.word(); word_res != nullptr; word_res = pr_it.forward()) {
    auto *choice = new WERD_CHOICE(word_res->uch_set, word_res->correct_text.size());
    for (auto &correct_text : word_res->correct_text) {
      // The part before the first space is the real ground truth, and the
      // rest is the bounding box location and page number.
      std::vector<std::string> tokens = split(correct_text, ' ');
      UNICHAR_ID char_id = unicharset.unichar_to_id(tokens[0].c_str());
      choice->append_unichar_id_space_allocated(char_id, word_res->best_state[&correct_text - &word_res->correct_text[0]], 0.0f, 0.0f);
    }
    word_res->ClearWordChoices();
    word_res->LogNewRawChoice(choice);
    word_res->LogNewCookedChoice(1, false, choice);
  }
}

} // namespace tesseract
