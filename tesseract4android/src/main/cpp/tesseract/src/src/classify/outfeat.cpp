/******************************************************************************
 ** Filename:    outfeat.c
 ** Purpose:     Definition of outline-features.
 ** Author:      Dan Johnson
 **
 ** (c) Copyright Hewlett-Packard Company, 1988.
 ** Licensed under the Apache License, Version 2.0 (the "License");
 ** you may not use this file except in compliance with the License.
 ** You may obtain a copy of the License at
 ** http://www.apache.org/licenses/LICENSE-2.0
 ** Unless required by applicable law or agreed to in writing, software
 ** distributed under the License is distributed on an "AS IS" BASIS,
 ** WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 ** See the License for the specific language governing permissions and
 ** limitations under the License.
 ******************************************************************************/

#include "outfeat.h"

#include "classify.h"
#include "featdefs.h"
#include "mfoutline.h"
#include "ocrfeatures.h"

#include <cstdio>

namespace tesseract {

/*----------------------------------------------------------------------------
              Public Code
----------------------------------------------------------------------------*/

/**
 * Convert each segment in the outline to a feature
 * and return the features.
 * @param Blob blob to extract pico-features from
 * @return Outline-features for Blob.
 * @note Globals: none
 */
FEATURE_SET Classify::ExtractOutlineFeatures(TBLOB *Blob) {
  auto FeatureSet = new FEATURE_SET_STRUCT(MAX_OUTLINE_FEATURES);
  if (Blob == nullptr) {
    return (FeatureSet);
  }

  auto Outlines = ConvertBlob(Blob);

  float XScale, YScale;
  NormalizeOutlines(Outlines, &XScale, &YScale);
  auto RemainingOutlines = Outlines;
  iterate(RemainingOutlines) {
    auto Outline = static_cast<MFOUTLINE>(RemainingOutlines->first_node());
    ConvertToOutlineFeatures(Outline, FeatureSet);
  }
  if (classify_norm_method == baseline) {
    NormalizeOutlineX(FeatureSet);
  }
  FreeOutlines(Outlines);
  return (FeatureSet);
} /* ExtractOutlineFeatures */

/*----------------------------------------------------------------------------
              Private Code
----------------------------------------------------------------------------*/
/*---------------------------------------------------------------------------*/
/**
 * This routine computes the midpoint between Start and
 * End to obtain the x,y position of the outline-feature.  It
 * also computes the direction from Start to End as the
 * direction of the outline-feature and the distance from
 * Start to End as the length of the outline-feature.
 * This feature is then
 * inserted into the next feature slot in FeatureSet.
 * @param Start starting point of outline-feature
 * @param End ending point of outline-feature
 * @param FeatureSet set to add outline-feature to
 */
void AddOutlineFeatureToSet(FPOINT *Start, FPOINT *End, FEATURE_SET FeatureSet) {
  auto Feature = new FEATURE_STRUCT(&OutlineFeatDesc);
  Feature->Params[OutlineFeatDir] = NormalizedAngleFrom(Start, End, 1.0);
  Feature->Params[OutlineFeatX] = AverageOf(Start->x, End->x);
  Feature->Params[OutlineFeatY] = AverageOf(Start->y, End->y);
  Feature->Params[OutlineFeatLength] = DistanceBetween(*Start, *End);
  AddFeature(FeatureSet, Feature);

} /* AddOutlineFeatureToSet */

/*---------------------------------------------------------------------------*/
/**
 * This routine steps converts each section in the specified
 * outline to a feature described by its x,y position, length
 * and angle.
 * Results are returned in FeatureSet.
 * @param Outline outline to extract outline-features from
 * @param FeatureSet set of features to add outline-features to
 */
void ConvertToOutlineFeatures(MFOUTLINE Outline, FEATURE_SET FeatureSet) {
  MFOUTLINE Next;
  MFOUTLINE First;
  FPOINT FeatureStart;
  FPOINT FeatureEnd;

  if (DegenerateOutline(Outline)) {
    return;
  }

  First = Outline;
  Next = First;
  do {
    FeatureStart = PointAt(Next)->Point;
    Next = NextPointAfter(Next);

    /* note that an edge is hidden if the ending point of the edge is
   marked as hidden.  This situation happens because the order of
   the outlines is reversed when they are converted from the old
   format.  In the old format, a hidden edge is marked by the
   starting point for that edge. */
    if (!PointAt(Next)->Hidden) {
      FeatureEnd = PointAt(Next)->Point;
      AddOutlineFeatureToSet(&FeatureStart, &FeatureEnd, FeatureSet);
    }
  } while (Next != First);
} /* ConvertToOutlineFeatures */

/*---------------------------------------------------------------------------*/
/**
 * This routine computes the weighted average x position
 * over all of the outline-features in FeatureSet and then
 * renormalizes the outline-features to force this average
 * to be the x origin (i.e. x=0).
 * FeatureSet is changed.
 * @param FeatureSet outline-features to be normalized
 */
void NormalizeOutlineX(FEATURE_SET FeatureSet) {
  int i;
  FEATURE Feature;
  float Length;
  float TotalX = 0.0;
  float TotalWeight = 0.0;
  float Origin;

  if (FeatureSet->NumFeatures <= 0) {
    return;
  }

  for (i = 0; i < FeatureSet->NumFeatures; i++) {
    Feature = FeatureSet->Features[i];
    Length = Feature->Params[OutlineFeatLength];
    TotalX += Feature->Params[OutlineFeatX] * Length;
    TotalWeight += Length;
  }
  Origin = TotalX / TotalWeight;

  for (i = 0; i < FeatureSet->NumFeatures; i++) {
    Feature = FeatureSet->Features[i];
    Feature->Params[OutlineFeatX] -= Origin;
  }
} /* NormalizeOutlineX */

} // namespace tesseract
