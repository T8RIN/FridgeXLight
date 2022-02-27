/******************************************************************************
 *
 * File:         gradechop.cpp  (Formerly gradechop.c)
 * Description:
 * Author:       Mark Seaman, OCR Technology
 *
 * (c) Copyright 1987, Hewlett-Packard Company.
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
 *****************************************************************************/
/*----------------------------------------------------------------------
              I n c l u d e s
----------------------------------------------------------------------*/

#include <algorithm>
#include <cmath>
#include "chop.h"
#include "wordrec.h"

/*----------------------------------------------------------------------
              M a c r o s
----------------------------------------------------------------------*/

namespace tesseract {

/*----------------------------------------------------------------------
              F u n c t i o n s
----------------------------------------------------------------------*/

/**********************************************************************
 * grade_split_length
 *
 * Return a grade for the length of this split.
 *   0    =  "perfect"
 *   100  =  "no way jay"
 **********************************************************************/
PRIORITY Wordrec::grade_split_length(SPLIT *split) {
  PRIORITY grade;
  float split_length;

  split_length = split->point1->WeightedDistance(*split->point2, chop_x_y_weight);

  if (split_length <= 0) {
    grade = 0;
  } else {
    grade = std::sqrt(split_length) * chop_split_dist_knob;
  }

  return (std::max(0.0f, grade));
}

/**********************************************************************
 * grade_sharpness
 *
 * Return a grade for the sharpness of this split.
 *   0    =  "perfect"
 *   100  =  "no way jay"
 **********************************************************************/
PRIORITY Wordrec::grade_sharpness(SPLIT *split) {
  PRIORITY grade;

  grade = point_priority(split->point1) + point_priority(split->point2);

  if (grade < -360.0) {
    grade = 0;
  } else {
    grade += 360.0;
  }

  grade *= chop_sharpness_knob; /* Values 0 to -360 */

  return (grade);
}

} // namespace tesseract
