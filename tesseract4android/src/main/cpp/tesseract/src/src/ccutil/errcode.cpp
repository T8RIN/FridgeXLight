/**********************************************************************
 * File:        errcode.cpp  (Formerly error.c)
 * Description: Generic error handler function
 * Author:      Ray Smith
 *
 * (C) Copyright 1989, Hewlett-Packard Ltd.
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

#include "errcode.h"

#include <cstdarg>
#include <cstdio>
#include <cstdlib>
#include <cstring>

namespace tesseract {

constexpr ERRCODE BADERRACTION("Illegal error action");
#define MAX_MSG 1024

/**********************************************************************
 * error
 *
 * Print an error message and continue, exit or abort according to action.
 * Makes use of error messages and numbers in a common place.
 *
 **********************************************************************/
void ERRCODE::error(         // handle error
    const char *caller,      // name of caller
    TessErrorLogCode action, // action to take
    const char *format, ...  // special message
    ) const {
  va_list args; // variable args
  char msg[MAX_MSG];
  char *msgptr = msg;

  if (caller != nullptr) {
    // name of caller
    msgptr += sprintf(msgptr, "%s:", caller);
  }
  // actual message
  msgptr += sprintf(msgptr, "Error:%s", message);
  if (format != nullptr) {
    msgptr += sprintf(msgptr, ":");
    va_start(args, format); // variable list
#ifdef _WIN32
                            // print remainder
    msgptr += _vsnprintf(msgptr, MAX_MSG - 2 - (msgptr - msg), format, args);
    msg[MAX_MSG - 2] = '\0'; // ensure termination
    strcat(msg, "\n");
#else
                            // print remainder
    msgptr += vsprintf(msgptr, format, args);
    // no specific
    msgptr += sprintf(msgptr, "\n");
#endif
    va_end(args);
  } else {
    // no specific
    msgptr += sprintf(msgptr, "\n");
  }

  // %s is needed here so msg is printed correctly!
  fprintf(stderr, "%s", msg);

  switch (action) {
    case DBG:
    case TESSLOG:
      return; // report only
    case TESSEXIT:
    case ABORT:
#if !defined(NDEBUG)
      // Create a deliberate abnormal exit as the stack trace is more useful
      // that way. This is done only in debug builds, because the
      // error message "segmentation fault" confuses most normal users.
#  if defined(__GNUC__)
      __builtin_trap();
#  else
      *reinterpret_cast<int *>(0) = 0;
#  endif
#endif
      abort();
    default:
      BADERRACTION.error("error", ABORT);
  }
}

void ERRCODE::error(const char *caller, TessErrorLogCode action) const {
  error(caller, action, nullptr);
}

} // namespace tesseract
