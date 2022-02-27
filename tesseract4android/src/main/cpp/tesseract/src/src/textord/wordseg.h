/**********************************************************************
 * File:        wordseg.h  (Formerly wspace.h)
 * Description: Code to segment the blobs into words.
 * Author:      Ray Smith
 *
 * (C) Copyright 1992, Hewlett-Packard Ltd.
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

#ifndef WORDSEG_H
#define WORDSEG_H

#include "blobbox.h"
#include "params.h"
#include "textord.h"

namespace tesseract {
class Tesseract;

extern BOOL_VAR_H(textord_force_make_prop_words);
extern BOOL_VAR_H(textord_chopper_test);

void make_single_word(bool one_blob, TO_ROW_LIST *rows, ROW_LIST *real_rows);
void make_words(tesseract::Textord *textord,
                ICOORD page_tr,              // top right
                float gradient,              // page skew
                BLOCK_LIST *blocks,          // block list
                TO_BLOCK_LIST *port_blocks); // output list
void set_row_spaces(                         // find space sizes
    TO_BLOCK *block,                         // block to do
    FCOORD rotation,                         // for drawing
    bool testing_on                          // correct orientation
);
int32_t row_words(    // compute space size
    TO_BLOCK *block,  // block it came from
    TO_ROW *row,      // row to operate on
    int32_t maxwidth, // max expected space size
    FCOORD rotation,  // for drawing
    bool testing_on   // for debug
);
int32_t row_words2(   // compute space size
    TO_BLOCK *block,  // block it came from
    TO_ROW *row,      // row to operate on
    int32_t maxwidth, // max expected space size
    FCOORD rotation,  // for drawing
    bool testing_on   // for debug
);
void make_real_words(tesseract::Textord *textord,
                     TO_BLOCK *block, // block to do
                     FCOORD rotation  // for drawing
);
ROW *make_rep_words( // make a row
    TO_ROW *row,     // row to convert
    TO_BLOCK *block  // block it lives in
);
WERD *make_real_word(    // make a WERD
    BLOBNBOX_IT *box_it, // iterator
    int32_t blobcount,   // no of blobs to use
    bool bol,            // start of line
    uint8_t blanks       // no of blanks
);

} // namespace tesseract

#endif
