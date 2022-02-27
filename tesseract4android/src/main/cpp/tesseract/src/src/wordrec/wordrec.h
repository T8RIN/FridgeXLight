///////////////////////////////////////////////////////////////////////
// File:        wordrec.h
// Description: wordrec class.
// Author:      Samuel Charron
//
// (C) Copyright 2006, Google Inc.
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
// http://www.apache.org/licenses/LICENSE-2.0
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
//
///////////////////////////////////////////////////////////////////////

#ifndef TESSERACT_WORDREC_WORDREC_H_
#define TESSERACT_WORDREC_WORDREC_H_

#ifdef HAVE_CONFIG_H
#  include "config_auto.h" // DISABLED_LEGACY_ENGINE
#endif

#ifdef DISABLED_LEGACY_ENGINE

#  include <cstdint>    // for int16_t, int32_t
#  include "classify.h" // for Classify
#  include "params.h"   // for INT_VAR_H, IntParam, BOOL_VAR_H, BoolP...
#  include "ratngs.h"   // for WERD_CHOICE

namespace tesseract {
class TessdataManager;
}

namespace tesseract {

/* ccmain/tstruct.cpp */

class TESS_API Wordrec : public Classify {
public:
  // config parameters

  BOOL_VAR_H(wordrec_debug_blamer);
  BOOL_VAR_H(wordrec_run_blamer);

  // methods
  Wordrec();
  virtual ~Wordrec() = default;

  // tface.cpp
  void program_editup(const std::string &textbase, TessdataManager *init_classifier,
                      TessdataManager *init_dict);
  void program_editdown(int32_t elasped_time);
  int end_recog();
  int dict_word(const WERD_CHOICE &word);

  // Member variables
  WERD_CHOICE *prev_word_best_choice_;
};

} // namespace tesseract

#else // DISABLED_LEGACY_ENGINE not defined

#  include <memory>
#  include "associate.h"
#  include "chop.h"     // for PointHeap, MAX_NUM_POINTS
#  include "classify.h" // for Classify
#  include "dict.h"
#  include "elst.h"     // for ELIST_ITERATOR, ELISTIZEH, ELIST_LINK
#  include "findseam.h" // for SeamQueue, SeamPile
#  include "language_model.h"
#  include "matrix.h"
#  include "oldlist.h" // for LIST
#  include "params.h"  // for INT_VAR_H, IntParam, BOOL_VAR_H, BoolP...
#  include "points.h"  // for ICOORD
#  include "ratngs.h"  // for BLOB_CHOICE_LIST (ptr only), BLOB_CHOI...
#  include "seam.h"    // for SEAM (ptr only), PRIORITY
#  include "stopper.h" // for DANGERR

#  include <cstdint> // for int16_t, int32_t

namespace tesseract {

class EDGEPT_CLIST;
class MATRIX;
class TBOX;
class UNICHARSET;
class WERD_RES;

class LMPainPoints;
class TessdataManager;
struct BestChoiceBundle;

struct BlamerBundle;
struct EDGEPT;
struct MATRIX_COORD;
struct SPLIT;
struct TBLOB;
struct TESSLINE;
struct TWERD;

// A class for storing which nodes are to be processed by the segmentation
// search. There is a single SegSearchPending for each column in the ratings
// matrix, and it indicates whether the segsearch should combine all
// BLOB_CHOICES in the column, or just the given row with the parents
// corresponding to *this SegSearchPending, and whether only updated parent
// ViterbiStateEntries should be combined, or all, with the BLOB_CHOICEs.
class SegSearchPending {
public:
  SegSearchPending()
      : classified_row_(-1), revisit_whole_column_(false), column_classified_(false) {}

  // Marks the whole column as just classified. Used to start a search on
  // a newly initialized ratings matrix.
  void SetColumnClassified() {
    column_classified_ = true;
  }
  // Marks the matrix entry at the given row as just classified.
  // Used after classifying a new matrix cell.
  // Additional to, not overriding a previous RevisitWholeColumn.
  void SetBlobClassified(int row) {
    classified_row_ = row;
  }
  // Marks the whole column as needing work, but not just classified.
  // Used when the parent vse list is updated.
  // Additional to, not overriding a previous SetBlobClassified.
  void RevisitWholeColumn() {
    revisit_whole_column_ = true;
  }

  // Clears *this to indicate no work to do.
  void Clear() {
    classified_row_ = -1;
    revisit_whole_column_ = false;
    column_classified_ = false;
  }

  // Returns true if there are updates to do in the column that *this
  // represents.
  bool WorkToDo() const {
    return revisit_whole_column_ || column_classified_ || classified_row_ >= 0;
  }
  // Returns true if the given row was just classified.
  bool IsRowJustClassified(int row) const {
    return row == classified_row_ || column_classified_;
  }
  // Returns the single row to process if there is only one, otherwise -1.
  int SingleRow() const {
    return revisit_whole_column_ || column_classified_ ? -1 : classified_row_;
  }

private:
  // If non-negative, indicates the single row in the ratings matrix that has
  // just been classified, and so should be combined with all the parents in the
  // column that this SegSearchPending represents.
  // Operates independently of revisit_whole_column.
  int classified_row_;
  // If revisit_whole_column is true, then all BLOB_CHOICEs in this column will
  // be processed, but classified_row can indicate a row that is newly
  // classified. Overridden if column_classified is true.
  bool revisit_whole_column_;
  // If column_classified is true, parent vses are processed with all rows
  // regardless of whether they are just updated, overriding
  // revisit_whole_column and classified_row.
  bool column_classified_;
};

/* ccmain/tstruct.cpp *********************************************************/
class FRAGMENT : public ELIST_LINK {
public:
  FRAGMENT() { // constructor
  }
  FRAGMENT(EDGEPT *head_pt,  // start
           EDGEPT *tail_pt); // end

  ICOORD head;    // coords of start
  ICOORD tail;    // coords of end
  EDGEPT *headpt; // start point
  EDGEPT *tailpt; // end point
};
ELISTIZEH(FRAGMENT)

class TESS_API Wordrec : public Classify {
public:
  // config parameters *******************************************************
  BOOL_VAR_H(merge_fragments_in_matrix);
  BOOL_VAR_H(wordrec_enable_assoc);
  BOOL_VAR_H(force_word_assoc);
  INT_VAR_H(repair_unchopped_blobs);
  double_VAR_H(tessedit_certainty_threshold);
  INT_VAR_H(chop_debug);
  BOOL_VAR_H(chop_enable);
  BOOL_VAR_H(chop_vertical_creep);
  INT_VAR_H(chop_split_length);
  INT_VAR_H(chop_same_distance);
  INT_VAR_H(chop_min_outline_points);
  INT_VAR_H(chop_seam_pile_size);
  BOOL_VAR_H(chop_new_seam_pile);
  INT_VAR_H(chop_inside_angle);
  INT_VAR_H(chop_min_outline_area);
  double_VAR_H(chop_split_dist_knob);
  double_VAR_H(chop_overlap_knob);
  double_VAR_H(chop_center_knob);
  INT_VAR_H(chop_centered_maxwidth);
  double_VAR_H(chop_sharpness_knob);
  double_VAR_H(chop_width_change_knob);
  double_VAR_H(chop_ok_split);
  double_VAR_H(chop_good_split);
  INT_VAR_H(chop_x_y_weight);
  BOOL_VAR_H(assume_fixed_pitch_char_segment);
  INT_VAR_H(wordrec_debug_level);
  INT_VAR_H(wordrec_max_join_chunks);
  BOOL_VAR_H(wordrec_skip_no_truth_words);
  BOOL_VAR_H(wordrec_debug_blamer);
  BOOL_VAR_H(wordrec_run_blamer);
  INT_VAR_H(segsearch_debug_level);
  INT_VAR_H(segsearch_max_pain_points);
  INT_VAR_H(segsearch_max_futile_classifications);
  double_VAR_H(segsearch_max_char_wh_ratio);
  BOOL_VAR_H(save_alt_choices);

  // methods from wordrec/*.cpp ***********************************************
  Wordrec();
  ~Wordrec() override = default;

  // Fills word->alt_choices with alternative paths found during
  // chopping/segmentation search that are kept in best_choices.
  void SaveAltChoices(const LIST &best_choices, WERD_RES *word);

  // Fills character choice lattice in the given BlamerBundle
  // using the given ratings matrix and best choice list.
  void FillLattice(const MATRIX &ratings, const WERD_CHOICE_LIST &best_choices,
                   const UNICHARSET &unicharset, BlamerBundle *blamer_bundle);

  // Calls fill_lattice_ member function
  // (assumes that fill_lattice_ is not nullptr).
  void CallFillLattice(const MATRIX &ratings, const WERD_CHOICE_LIST &best_choices,
                       const UNICHARSET &unicharset, BlamerBundle *blamer_bundle) {
    (this->*fill_lattice_)(ratings, best_choices, unicharset, blamer_bundle);
  }

  // tface.cpp
  void program_editup(const std::string &textbase, TessdataManager *init_classifier,
                      TessdataManager *init_dict);
  void cc_recog(WERD_RES *word);
  void program_editdown(int32_t elasped_time);
  void set_pass1();
  void set_pass2();
  int end_recog();
  BLOB_CHOICE_LIST *call_matcher(TBLOB *blob);
  int dict_word(const WERD_CHOICE &word);
  // wordclass.cpp
  BLOB_CHOICE_LIST *classify_blob(TBLOB *blob, const char *string, ScrollView::Color color,
                                  BlamerBundle *blamer_bundle);

  // segsearch.cpp
  // SegSearch works on the lower diagonal matrix of BLOB_CHOICE_LISTs.
  // Each entry in the matrix represents the classification choice
  // for a chunk, i.e. an entry in row 2, column 1 represents the list
  // of ratings for the chunks 1 and 2 classified as a single blob.
  // The entries on the diagonal of the matrix are classifier choice lists
  // for a single chunk from the maximal segmentation.
  //
  // The ratings matrix given to SegSearch represents the segmentation
  // graph / trellis for the current word. The nodes in the graph are the
  // individual BLOB_CHOICEs in each of the BLOB_CHOICE_LISTs in the ratings
  // matrix. The children of each node (nodes connected by outgoing links)
  // are the entries in the column that is equal to node's row+1. The parents
  // (nodes connected by the incoming links) are the entries in the row that
  // is equal to the node's column-1. Here is an example ratings matrix:
  //
  //    0    1    2   3   4
  //  -------------------------
  // 0| c,(                   |
  // 1| d    l,1              |
  // 2|           o           |
  // 3|              c,(      |
  // 4|              g,y  l,1 |
  //  -------------------------
  //
  // In the example above node "o" has children (outgoing connection to nodes)
  // "c","(","g","y" and parents (incoming connections from nodes) "l","1","d".
  //
  // The objective of the search is to find the least cost path, where the cost
  // is determined by the language model components and the properties of the
  // cut between the blobs on the path. SegSearch starts by populating the
  // matrix with the all the entries that were classified by the chopper and
  // finding the initial best path. Based on the classifier ratings, language
  // model scores and the properties of each cut, a list of "pain points" is
  // constructed - those are the points on the path where the choices do not
  // look consistent with the neighboring choices, the cuts look particularly
  // problematic, or the certainties of the blobs are low. The most troublesome
  // "pain point" is picked from the list and the new entry in the ratings
  // matrix corresponding to this "pain point" is filled in. Then the language
  // model state is updated to reflect the new classification and the new
  // "pain points" are added to the list and the next most troublesome
  // "pain point" is determined. This continues until either the word choice
  // composed from the best paths in the segmentation graph is "good enough"
  // (e.g. above a certain certainty threshold, is an unambiguous dictionary
  // word, etc) or there are no more "pain points" to explore.
  //
  // If associate_blobs is set to false no new classifications will be done
  // to combine blobs. Segmentation search will run only one "iteration"
  // on the classifications already recorded in chunks_record.ratings.
  //
  // Note: this function assumes that word_res, best_choice_bundle arguments
  // are not nullptr.
  void SegSearch(WERD_RES *word_res, BestChoiceBundle *best_choice_bundle,
                 BlamerBundle *blamer_bundle);

  // Setup and run just the initial segsearch on an established matrix,
  // without doing any additional chopping or joining.
  // (Internal factored version that can be used as part of the main SegSearch.)
  void InitialSegSearch(WERD_RES *word_res, LMPainPoints *pain_points,
                        std::vector<SegSearchPending> *pending,
                        BestChoiceBundle *best_choice_bundle, BlamerBundle *blamer_bundle);

  // chop.cpp
  PRIORITY point_priority(EDGEPT *point);
  void add_point_to_list(PointHeap *point_heap, EDGEPT *point);
  // Returns true if the edgept supplied as input is an inside angle.  This
  // is determined by the angular change of the vectors from point to point.
  bool is_inside_angle(EDGEPT *pt);
  int angle_change(EDGEPT *point1, EDGEPT *point2, EDGEPT *point3);
  EDGEPT *pick_close_point(EDGEPT *critical_point, EDGEPT *vertical_point, int *best_dist);
  void prioritize_points(TESSLINE *outline, PointHeap *points);
  void new_min_point(EDGEPT *local_min, PointHeap *points);
  void new_max_point(EDGEPT *local_max, PointHeap *points);
  void vertical_projection_point(EDGEPT *split_point, EDGEPT *target_point, EDGEPT **best_point,
                                 EDGEPT_CLIST *new_points);

  // chopper.cpp
  SEAM *attempt_blob_chop(TWERD *word, TBLOB *blob, int32_t blob_number, bool italic_blob,
                          const std::vector<SEAM *> &seams);
  SEAM *chop_numbered_blob(TWERD *word, int32_t blob_number, bool italic_blob,
                           const std::vector<SEAM *> &seams);
  SEAM *chop_overlapping_blob(const std::vector<TBOX> &boxes, bool italic_blob, WERD_RES *word_res,
                              unsigned *blob_number);
  SEAM *improve_one_blob(const std::vector<BLOB_CHOICE *> &blob_choices, DANGERR *fixpt,
                         bool split_next_to_fragment, bool italic_blob, WERD_RES *word,
                         unsigned *blob_number);
  SEAM *chop_one_blob(const std::vector<TBOX> &boxes,
                      const std::vector<BLOB_CHOICE *> &blob_choices, WERD_RES *word_res,
                      unsigned *blob_number);
  void chop_word_main(WERD_RES *word);
  void improve_by_chopping(float rating_cert_scale, WERD_RES *word,
                           BestChoiceBundle *best_choice_bundle, BlamerBundle *blamer_bundle,
                           LMPainPoints *pain_points, std::vector<SegSearchPending> *pending);
  int select_blob_to_split(const std::vector<BLOB_CHOICE *> &blob_choices, float rating_ceiling,
                           bool split_next_to_fragment);
  int select_blob_to_split_from_fixpt(DANGERR *fixpt);

  // findseam.cpp
  void add_seam_to_queue(float new_priority, SEAM *new_seam, SeamQueue *seams);
  void choose_best_seam(SeamQueue *seam_queue, const SPLIT *split, PRIORITY priority,
                        SEAM **seam_result, TBLOB *blob, SeamPile *seam_pile);
  void combine_seam(const SeamPile &seam_pile, const SEAM *seam, SeamQueue *seam_queue);
  SEAM *pick_good_seam(TBLOB *blob);
  void try_point_pairs(EDGEPT *points[MAX_NUM_POINTS], int16_t num_points, SeamQueue *seam_queue,
                       SeamPile *seam_pile, SEAM **seam, TBLOB *blob);
  void try_vertical_splits(EDGEPT *points[MAX_NUM_POINTS], int16_t num_points,
                           EDGEPT_CLIST *new_points, SeamQueue *seam_queue, SeamPile *seam_pile,
                           SEAM **seam, TBLOB *blob);

  // gradechop.cpp
  PRIORITY grade_split_length(SPLIT *split);
  PRIORITY grade_sharpness(SPLIT *split);

  // outlines.cpp
  bool near_point(EDGEPT *point, EDGEPT *line_pt_0, EDGEPT *line_pt_1, EDGEPT **near_pt);

  // pieces.cpp
  virtual BLOB_CHOICE_LIST *classify_piece(const std::vector<SEAM *> &seams, int16_t start,
                                           int16_t end, const char *description, TWERD *word,
                                           BlamerBundle *blamer_bundle);

  // Member variables.

  std::unique_ptr<LanguageModel> language_model_;
  PRIORITY pass2_ok_split;
  // Stores the best choice for the previous word in the paragraph.
  // This variable is modified by PAGE_RES_IT when iterating over
  // words to OCR on the page.
  WERD_CHOICE *prev_word_best_choice_;

  // Function used to fill char choice lattices.
  void (Wordrec::*fill_lattice_)(const MATRIX &ratings, const WERD_CHOICE_LIST &best_choices,
                                 const UNICHARSET &unicharset, BlamerBundle *blamer_bundle);

protected:
  inline bool SegSearchDone(int num_futile_classifications) {
    return (language_model_->AcceptableChoiceFound() ||
            num_futile_classifications >= segsearch_max_futile_classifications);
  }

  // Updates the language model state recorded for the child entries specified
  // in pending[starting_col]. Enqueues the children of the updated entries
  // into pending and proceeds to update (and remove from pending) all the
  // remaining entries in pending[col] (col >= starting_col). Upon termination
  // of this function all the pending[col] lists will be empty.
  //
  // The arguments:
  //
  // starting_col: index of the column in chunks_record->ratings from
  // which the update should be started
  //
  // pending: list of entries listing chunks_record->ratings entries
  // that should be updated
  //
  // pain_points: priority heap listing the pain points generated by
  // the language model
  //
  // temp_pain_points: temporary storage for tentative pain points generated
  // by the language model after a single call to LanguageModel::UpdateState()
  // (the argument is passed in rather than created before each
  // LanguageModel::UpdateState() call to avoid dynamic memory re-allocation)
  //
  // best_choice_bundle: a collection of variables that should be updated
  // if a new best choice is found
  //
  void UpdateSegSearchNodes(float rating_cert_scale, int starting_col,
                            std::vector<SegSearchPending> *pending, WERD_RES *word_res,
                            LMPainPoints *pain_points, BestChoiceBundle *best_choice_bundle,
                            BlamerBundle *blamer_bundle);

  // Process the given pain point: classify the corresponding blob, enqueue
  // new pain points to join the newly classified blob with its neighbors.
  void ProcessSegSearchPainPoint(float pain_point_priority, const MATRIX_COORD &pain_point,
                                 const char *pain_point_type,
                                 std::vector<SegSearchPending> *pending, WERD_RES *word_res,
                                 LMPainPoints *pain_points, BlamerBundle *blamer_bundle);
  // Resets enough of the results so that the Viterbi search is re-run.
  // Needed when the n-gram model is enabled, as the multi-length comparison
  // implementation will re-value existing paths to worse values.
  void ResetNGramSearch(WERD_RES *word_res, BestChoiceBundle *best_choice_bundle,
                        std::vector<SegSearchPending> &pending);

  // Add pain points for classifying blobs on the correct segmentation path
  // (so that we can evaluate correct segmentation path and discover the reason
  // for incorrect result).
  void InitBlamerForSegSearch(WERD_RES *word_res, LMPainPoints *pain_points,
                              BlamerBundle *blamer_bundle, std::string &blamer_debug);
};

} // namespace tesseract

#endif // DISABLED_LEGACY_ENGINE

#endif // TESSERACT_WORDREC_WORDREC_H_
