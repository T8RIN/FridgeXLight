///////////////////////////////////////////////////////////////////////
// File:        dotproduct.cpp
// Description: Native dot product function.
//
// (C) Copyright 2018, Google Inc.
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
// http://www.apache.org/licenses/LICENSE-2.0
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
///////////////////////////////////////////////////////////////////////

#include "dotproduct.h"

namespace tesseract {

// Computes and returns the dot product of the two n-vectors u and v.
TFloat DotProductNative(const TFloat *u, const TFloat *v, int n) {
  TFloat total = 0;
#if defined(OPENMP_SIMD) || defined(_OPENMP)
#pragma omp simd reduction(+:total)
#endif
  for (int k = 0; k < n; k++) {
    total += u[k] * v[k];
  }
  return total;
}

} // namespace tesseract
