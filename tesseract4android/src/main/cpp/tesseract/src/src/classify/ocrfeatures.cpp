/******************************************************************************
 ** Filename:    ocrfeatures.cpp
 ** Purpose:     Generic definition of a feature.
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

#include "ocrfeatures.h"

#include "scanutils.h"

#include <cassert>
#include <cmath>

namespace tesseract {

/*----------------------------------------------------------------------------
              Public Code
----------------------------------------------------------------------------*/
/**
 * Add a feature to a feature set.  If the feature set is
 * already full, false is returned to indicate that the
 * feature could not be added to the set; otherwise, true is
 * returned.
 * @param FeatureSet set of features to add Feature to
 * @param Feature feature to be added to FeatureSet
 * @return  true if feature added to set, false if set is already full.
 */
bool AddFeature(FEATURE_SET FeatureSet, FEATURE Feature) {
  if (FeatureSet->NumFeatures >= FeatureSet->MaxNumFeatures) {
    delete Feature;
    return false;
  }

  FeatureSet->Features[FeatureSet->NumFeatures++] = Feature;
  return true;
} /* AddFeature */

/**
 * Create a new feature of the specified type and read in
 * the value of its parameters from File.  The extra penalty
 * for the feature is also computed by calling the appropriate
 * function for the specified feature type.  The correct text
 * representation for a feature is a list of N floats where
 * N is the number of parameters in the feature.
 * @param File open text file to read feature from
 * @param FeatureDesc specifies type of feature to read from File
 * @return New #FEATURE read from File.
 */
static FEATURE ReadFeature(FILE *File, const FEATURE_DESC_STRUCT *FeatureDesc) {
  auto Feature = new FEATURE_STRUCT(FeatureDesc);
  for (int i = 0; i < Feature->Type->NumParams; i++) {
    ASSERT_HOST(tfscanf(File, "%f", &(Feature->Params[i])) == 1);
#ifndef _WIN32
    assert(!std::isnan(Feature->Params[i]));
#endif
  }
  return Feature;
}

/**
 * Create a new feature set of the specified type and read in
 * the features from File.  The correct text representation
 * for a feature set is an integer which specifies the number (N)
 * of features in a set followed by a list of N feature
 * descriptions.
 * @param File open text file to read new feature set from
 * @param FeatureDesc specifies type of feature to read from File
 * @return New feature set read from File.
 */
FEATURE_SET ReadFeatureSet(FILE *File, const FEATURE_DESC_STRUCT *FeatureDesc) {
  int NumFeatures;
  ASSERT_HOST(tfscanf(File, "%d", &NumFeatures) == 1);
  ASSERT_HOST(NumFeatures >= 0);

  auto FeatureSet = new FEATURE_SET_STRUCT(NumFeatures);
  for (int i = 0; i < NumFeatures; i++) {
    AddFeature(FeatureSet, ReadFeature(File, FeatureDesc));
  }

  return FeatureSet;
}

/**
 * Appends a textual representation of Feature to str.
 * This representation is simply a list of the N parameters
 * of the feature, terminated with a newline.  It is assumed
 * that the ExtraPenalty field can be reconstructed from the
 * parameters of the feature.  It is also assumed that the
 * feature type information is specified or assumed elsewhere.
 * @param Feature feature to write out to str
 * @param str string to write Feature to
 */
static void WriteFeature(FEATURE Feature, std::string &str) {
  for (int i = 0; i < Feature->Type->NumParams; i++) {
#ifndef WIN32
    assert(!std::isnan(Feature->Params[i]));
#endif
    str += " " + std::to_string(Feature->Params[i]);
  }
  str += "\n";
} /* WriteFeature */

/**
 * Write a textual representation of FeatureSet to File.
 * This representation is an integer specifying the number of
 * features in the set, followed by a newline, followed by
 * text representations for each feature in the set.
 * @param FeatureSet feature set to write to File
 * @param str string to write Feature to
 */
void WriteFeatureSet(FEATURE_SET FeatureSet, std::string &str) {
  if (FeatureSet) {
    str += "" + std::to_string(FeatureSet->NumFeatures);
    str += "\n";
    for (int i = 0; i < FeatureSet->NumFeatures; i++) {
      WriteFeature(FeatureSet->Features[i], str);
    }
  }
} /* WriteFeatureSet */

} // namespace tesseract
