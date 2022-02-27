/******************************************************************************
 ** Filename:    features.h
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

#ifndef FEATURES_H
#define FEATURES_H

#include "blobs.h"

#include <cstdio>
#include <string> // for std::string

namespace tesseract {

class DENORM;

#undef Min
#undef Max
#define FEAT_NAME_SIZE 80

// A character is described by multiple sets of extracted features.  Each
// set contains a number of features of a particular type, for example, a
// set of bays, or a set of closures, or a set of microfeatures.  Each
// feature consists of a number of parameters.  All features within a
// feature set contain the same number of parameters.  All circular
// parameters are required to be the first parameters in the feature.

struct PARAM_DESC {
  bool Circular;     // true if dimension wraps around
  bool NonEssential; // true if dimension not used in searches
  float Min;         // low end of range for circular dimensions
  float Max;         // high end of range for circular dimensions
  float Range;       // Max - Min
  float HalfRange;   // (Max - Min)/2
  float MidRange;    // (Max + Min)/2
};

struct FEATURE_DESC_STRUCT {
  uint16_t NumParams;          // total # of params
  const char *ShortName;       // short name for feature
  const PARAM_DESC *ParamDesc; // array - one per param
};
using FEATURE_DESC = FEATURE_DESC_STRUCT *;

struct FEATURE_STRUCT {
  /// Constructor for a new feature of the specified type.
  /// @param FeatureDesc description of feature to be created.
  FEATURE_STRUCT(const FEATURE_DESC_STRUCT *FeatureDesc) : Type(FeatureDesc), Params(FeatureDesc->NumParams) {
  }
  ~FEATURE_STRUCT() {
  }
  const FEATURE_DESC_STRUCT *Type; // points to description of feature type
  std::vector<float> Params;       // variable size array - params for feature
};
using FEATURE = FEATURE_STRUCT *;

struct FEATURE_SET_STRUCT {
  /// Creator for a new feature set large enough to
  /// hold the specified number of features.
  /// @param NumFeatures maximum # of features to be put in feature set
  FEATURE_SET_STRUCT(int numFeatures) : NumFeatures(0), MaxNumFeatures(numFeatures), Features(numFeatures) {
  }

  ~FEATURE_SET_STRUCT() {
    for (uint16_t i = 0; i < NumFeatures; i++) {
      delete Features[i];
    }
  }

  uint16_t NumFeatures;    // number of features in set
  uint16_t MaxNumFeatures; // maximum size of feature set
  std::vector<FEATURE_STRUCT *> Features; // variable size array of features
};
using FEATURE_SET = FEATURE_SET_STRUCT *;

// A generic character description as a char pointer. In reality, it will be
// a pointer to some data structure. Paired feature extractors/matchers need
// to agree on the data structure to be used, however, the high level
// classifier does not need to know the details of this data structure.
using CHAR_FEATURES = char *;

/*----------------------------------------------------------------------
    Macros for defining the parameters of a new features
----------------------------------------------------------------------*/
#define StartParamDesc(Name) const PARAM_DESC Name[] = {
#define DefineParam(Circular, NonEssential, Min, Max) \
  {Circular,                                          \
   NonEssential,                                      \
   Min,                                               \
   Max,                                               \
   (Max) - (Min),                                     \
   (((Max) - (Min)) / 2.0),                           \
   (((Max) + (Min)) / 2.0)},

#define EndParamDesc \
  }                  \
  ;

/*----------------------------------------------------------------------
Macro for describing a new feature.  The parameters of the macro
are as follows:

DefineFeature (Name, NumLinear, NumCircular, ShortName, ParamName)
----------------------------------------------------------------------*/
#define DefineFeature(Name, NL, NC, SN, PN) \
  const FEATURE_DESC_STRUCT Name = {((NL) + (NC)), SN, PN};

/*----------------------------------------------------------------------
        Generic routines that work for all feature types
----------------------------------------------------------------------*/
bool AddFeature(FEATURE_SET FeatureSet, FEATURE Feature);

FEATURE_SET ReadFeatureSet(FILE *File, const FEATURE_DESC_STRUCT *FeatureDesc);

void WriteFeatureSet(FEATURE_SET FeatureSet, std::string &str);

} // namespace tesseract

#endif
