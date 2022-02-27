// (C) Copyright 2017, Google Inc.
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
// http://www.apache.org/licenses/LICENSE-2.0
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

#include <tesseract/baseapi.h>

// Dummy enum in the global namespace that checks for collision with awkward
// names.
// If this test fails to compile, clean up the includes in tesseract/baseapi.h!
// They are not supposed to drag in definitions of any of the tesseract
// types included in this enum!
enum NameTester { ABORT, OKAY, LOG, BLOB, ELIST, TBOX, TPOINT, WORD };

#include "gtest/gtest.h"

namespace tesseract {

// Verifies that the global namespace is clean.
TEST(CleanNamespaceTess, DummyTest) {
  tesseract::TessBaseAPI api;
}

} // namespace tesseract
