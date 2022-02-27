/**********************************************************************
 * File:        polyaprx.cpp
 * Description: Code for polygonal approximation from old edgeprog.
 * Author:      Ray Smith
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

#include "polyaprx.h"

#include "blobs.h"   // for EDGEPT, TPOINT, VECTOR, TESSLINE
#include "coutln.h"  // for C_OUTLINE
#include "errcode.h" // for ASSERT_HOST
#include "mod128.h"  // for DIR128
#include "params.h"  // for BoolParam, BOOL_VAR
#include "points.h"  // for ICOORD
#include "rect.h"    // for TBOX
#include "tprintf.h" // for tprintf

#include <cstdint> // for INT16_MAX, int8_t

namespace tesseract {

#define FASTEDGELENGTH 256

static BOOL_VAR(poly_debug, false, "Debug old poly");
static BOOL_VAR(poly_wide_objects_better, true,
                "More accurate approx on wide things");

#define fixed_dist 20  // really an int_variable
#define approx_dist 15 // really an int_variable

const int par1 = 4500 / (approx_dist * approx_dist);
const int par2 = 6750 / (approx_dist * approx_dist);

/**********************************************************************
 *cutline(first,last,area) straightens out a line by partitioning
 *and joining the ends by a straight line*
 **********************************************************************/

static void cutline(       // recursive refine
    EDGEPT *first,         // ends of line
    EDGEPT *last, int area // area of object
) {
  EDGEPT *edge;     // current edge
  TPOINT vecsum;    // vector sum
  int vlen;         // approx length of vecsum
  TPOINT vec;       // accumulated vector
  EDGEPT *maxpoint; // worst point
  int maxperp;      // max deviation
  int perp;         // perp distance
  int ptcount;      // no of points
  int squaresum;    // sum of perps

  edge = first; // start of line
  if (edge->next == last) {
    return; // simple line
  }

  // vector sum
  vecsum.x = last->pos.x - edge->pos.x;
  vecsum.y = last->pos.y - edge->pos.y;
  if (vecsum.x == 0 && vecsum.y == 0) {
    // special case
    vecsum.x = -edge->prev->vec.x;
    vecsum.y = -edge->prev->vec.y;
  }
  // absolute value
  vlen = vecsum.x > 0 ? vecsum.x : -vecsum.x;
  if (vecsum.y > vlen) {
    vlen = vecsum.y; // maximum
  } else if (-vecsum.y > vlen) {
    vlen = -vecsum.y; // absolute value
  }

  vec.x = edge->vec.x; // accumulated vector
  vec.y = edge->vec.y;
  maxperp = 0; // none yet
  squaresum = ptcount = 0;
  edge = edge->next; // move to actual point
  maxpoint = edge;   // in case there isn't one
  do {
    perp = vec.cross(vecsum); // get perp distance
    if (perp != 0) {
      perp *= perp; // squared deviation
    }
    squaresum += perp; // sum squares
    ptcount++;         // count points
    if (poly_debug) {
      tprintf("Cutline:Final perp=%d\n", perp);
    }
    if (perp > maxperp) {
      maxperp = perp;
      maxpoint = edge; // find greatest deviation
    }
    vec.x += edge->vec.x; // accumulate vectors
    vec.y += edge->vec.y;
    edge = edge->next;
  } while (edge != last); // test all line

  perp = vecsum.length();
  ASSERT_HOST(perp != 0);

  if (maxperp < 256 * INT16_MAX) {
    maxperp <<= 8;
    maxperp /= perp; // true max perp
  } else {
    maxperp /= perp;
    maxperp <<= 8; // avoid overflow
  }
  if (squaresum < 256 * INT16_MAX) {
    // mean squared perp
    perp = (squaresum << 8) / (perp * ptcount);
  } else {
    // avoid overflow
    perp = (squaresum / perp << 8) / ptcount;
  }

  if (poly_debug) {
    tprintf("Cutline:A=%d, max=%.2f(%.2f%%), msd=%.2f(%.2f%%)\n", area,
            maxperp / 256.0, maxperp * 200.0 / area, perp / 256.0,
            perp * 300.0 / area);
  }
  if (maxperp * par1 >= 10 * area || perp * par2 >= 10 * area || vlen >= 126) {
    maxpoint->fixed = true;
    // partitions
    cutline(first, maxpoint, area);
    cutline(maxpoint, last, area);
  }
}

/**********************************************************************
 * edgesteps_to_edgepts
 *
 * Convert a C_OUTLINE to EDGEPTs.
 **********************************************************************/

static EDGEPT *edgesteps_to_edgepts( // convert outline
    C_OUTLINE *c_outline,            // input
    EDGEPT edgepts[]                 // output is array
) {
  int32_t length;    // steps in path
  ICOORD pos;        // current coords
  int32_t stepindex; // current step
  int32_t stepinc;   // increment
  int32_t epindex;   // current EDGEPT
  ICOORD vec;        // for this 8 step
  ICOORD prev_vec;
  int8_t epdir;   // of this step
  DIR128 prevdir; // previous dir
  DIR128 dir;     // of this step

  pos = c_outline->start_pos(); // start of loop
  length = c_outline->pathlength();
  stepindex = 0;
  epindex = 0;
  prevdir = -1;
  // repeated steps
  uint32_t count = 0;
  int prev_stepindex = 0;
  do {
    dir = c_outline->step_dir(stepindex);
    vec = c_outline->step(stepindex);
    if (stepindex < length - 1 &&
        c_outline->step_dir(stepindex + 1) - dir == -32) {
      dir += 128 - 16;
      vec += c_outline->step(stepindex + 1);
      stepinc = 2;
    } else {
      stepinc = 1;
    }
    if (count == 0) {
      prevdir = dir;
      prev_vec = vec;
    }
    if (prevdir.get_dir() != dir.get_dir()) {
      edgepts[epindex].pos.x = pos.x();
      edgepts[epindex].pos.y = pos.y();
      prev_vec *= count;
      edgepts[epindex].vec.x = prev_vec.x();
      edgepts[epindex].vec.y = prev_vec.y();
      pos += prev_vec;
      edgepts[epindex].runlength = count;
      edgepts[epindex].prev = &edgepts[epindex - 1];
      // TODO: reset is_hidden, too?
      edgepts[epindex].fixed = false;
      edgepts[epindex].next = &edgepts[epindex + 1];
      prevdir += 64;
      epdir = DIR128(0) - prevdir;
      epdir >>= 4;
      epdir &= 7;
      edgepts[epindex].dir = epdir;
      edgepts[epindex].src_outline = c_outline;
      edgepts[epindex].start_step = prev_stepindex;
      edgepts[epindex].step_count = stepindex - prev_stepindex;
      epindex++;
      prevdir = dir;
      prev_vec = vec;
      count = 1;
      prev_stepindex = stepindex;
    } else {
      count++;
    }
    stepindex += stepinc;
  } while (stepindex < length);
  edgepts[epindex].pos.x = pos.x();
  edgepts[epindex].pos.y = pos.y();
  prev_vec *= count;
  edgepts[epindex].vec.x = prev_vec.x();
  edgepts[epindex].vec.y = prev_vec.y();
  pos += prev_vec;
  edgepts[epindex].runlength = count;
  // TODO: reset is_hidden, too?
  edgepts[epindex].fixed = false;
  edgepts[epindex].src_outline = c_outline;
  edgepts[epindex].start_step = prev_stepindex;
  edgepts[epindex].step_count = stepindex - prev_stepindex;
  edgepts[epindex].prev = &edgepts[epindex - 1];
  edgepts[epindex].next = &edgepts[0];
  prevdir += 64;
  epdir = DIR128(0) - prevdir;
  epdir >>= 4;
  epdir &= 7;
  edgepts[epindex].dir = epdir;
  edgepts[0].prev = &edgepts[epindex];
  ASSERT_HOST(pos.x() == c_outline->start_pos().x() &&
              pos.y() == c_outline->start_pos().y());
  return &edgepts[0];
}

/**********************************************************************
 *fix2(start,area) fixes points on the outline according to a trial method*
 **********************************************************************/

static void fix2(  // polygonal approx
    EDGEPT *start, // loop to approximate
    int area) {
  EDGEPT *edgept; // current point
  EDGEPT *edgept1;
  EDGEPT *loopstart; // modified start of loop
  EDGEPT *linestart; // start of line segment
  int fixed_count;   // no of fixed points
  int8_t dir;
  int d01, d12, d23, gapmin;
  TPOINT d01vec, d12vec, d23vec;
  EDGEPT *edgefix, *startfix;
  EDGEPT *edgefix0, *edgefix1, *edgefix2, *edgefix3;

  edgept = start; // start of loop
  while (((edgept->dir - edgept->prev->dir + 1) & 7) < 3 &&
         (dir = (edgept->prev->dir - edgept->next->dir) & 7) != 2 && dir != 6) {
    edgept = edgept->next; // find suitable start
  }
  loopstart = edgept; // remember start

  // completed flag
  bool stopped = false;
  edgept->fixed = true; // fix it
  do {
    linestart = edgept;      // possible start of line
    auto dir1 = edgept->dir; // first direction
    // length of dir1
    auto sum1 = edgept->runlength;
    edgept = edgept->next;
    auto dir2 = edgept->dir; // 2nd direction
    // length in dir2
    auto sum2 = edgept->runlength;
    if (((dir1 - dir2 + 1) & 7) < 3) {
      while (edgept->prev->dir == edgept->next->dir) {
        edgept = edgept->next; // look at next
        if (edgept->dir == dir1) {
          // sum lengths
          sum1 += edgept->runlength;
        } else {
          sum2 += edgept->runlength;
        }
      }

      if (edgept == loopstart) {
        // finished
        stopped = true;
      }
      if (sum2 + sum1 > 2 && linestart->prev->dir == dir2 &&
          (linestart->prev->runlength > linestart->runlength || sum2 > sum1)) {
        // start is back one
        linestart = linestart->prev;
        linestart->fixed = true;
      }

      if (((edgept->next->dir - edgept->dir + 1) & 7) >= 3 ||
          (edgept->dir == dir1 && sum1 >= sum2) ||
          ((edgept->prev->runlength < edgept->runlength ||
            (edgept->dir == dir2 && sum2 >= sum1)) &&
           linestart->next != edgept)) {
        edgept = edgept->next;
      }
    }
    // sharp bend
    edgept->fixed = true;
  }
  // do whole loop
  while (edgept != loopstart && !stopped);

  edgept = start;
  do {
    if (((edgept->runlength >= 8) && (edgept->dir != 2) &&
         (edgept->dir != 6)) ||
        ((edgept->runlength >= 8) &&
         ((edgept->dir == 2) || (edgept->dir == 6)))) {
      edgept->fixed = true;
      edgept1 = edgept->next;
      edgept1->fixed = true;
    }
    edgept = edgept->next;
  } while (edgept != start);

  edgept = start;
  do {
    // single fixed step
    if (edgept->fixed &&
        edgept->runlength == 1
        // and neighbours free
        && edgept->next->fixed &&
        !edgept->prev->fixed
        // same pair of dirs
        && !edgept->next->next->fixed &&
        edgept->prev->dir == edgept->next->dir &&
        edgept->prev->prev->dir == edgept->next->next->dir &&
        ((edgept->prev->dir - edgept->dir + 1) & 7) < 3) {
      // unfix it
      edgept->fixed = false;
      edgept->next->fixed = false;
    }
    edgept = edgept->next;   // do all points
  } while (edgept != start); // until finished

  stopped = false;
  if (area < 450) {
    area = 450;
  }

  gapmin = area * fixed_dist * fixed_dist / 44000;

  edgept = start;
  fixed_count = 0;
  do {
    if (edgept->fixed) {
      fixed_count++;
    }
    edgept = edgept->next;
  } while (edgept != start);
  while (!edgept->fixed) {
    edgept = edgept->next;
  }
  edgefix0 = edgept;

  edgept = edgept->next;
  while (!edgept->fixed) {
    edgept = edgept->next;
  }
  edgefix1 = edgept;

  edgept = edgept->next;
  while (!edgept->fixed) {
    edgept = edgept->next;
  }
  edgefix2 = edgept;

  edgept = edgept->next;
  while (!edgept->fixed) {
    edgept = edgept->next;
  }
  edgefix3 = edgept;

  startfix = edgefix2;

  do {
    if (fixed_count <= 3) {
      break; // already too few
    }
    d12vec.diff(edgefix1->pos, edgefix2->pos);
    d12 = d12vec.length();
    // TODO(rays) investigate this change:
    // Only unfix a point if it is part of a low-curvature section
    // of outline and the total angle change of the outlines is
    // less than 90 degrees, ie the scalar product is positive.
    // if (d12 <= gapmin && edgefix0->vec.dot(edgefix2->vec) > 0) {
    if (d12 <= gapmin) {
      d01vec.diff(edgefix0->pos, edgefix1->pos);
      d01 = d01vec.length();
      d23vec.diff(edgefix2->pos, edgefix3->pos);
      d23 = d23vec.length();
      if (d01 > d23) {
        edgefix2->fixed = false;
        fixed_count--;
      } else {
        edgefix1->fixed = false;
        fixed_count--;
        edgefix1 = edgefix2;
      }
    } else {
      edgefix0 = edgefix1;
      edgefix1 = edgefix2;
    }
    edgefix2 = edgefix3;
    edgept = edgept->next;
    while (!edgept->fixed) {
      if (edgept == startfix) {
        stopped = true;
      }
      edgept = edgept->next;
    }
    edgefix3 = edgept;
    edgefix = edgefix2;
  } while ((edgefix != startfix) && (!stopped));
}

/**********************************************************************
 *poly2(startpt,area,path) applies a second approximation to the outline
 *using the points which have been fixed by the first approximation*
 **********************************************************************/

static EDGEPT *poly2( // second poly
    EDGEPT *startpt,  // start of loop
    int area          // area of blob box
) {
  EDGEPT *edgept;    // current outline point
  EDGEPT *loopstart; // starting point
  EDGEPT *linestart; // start of line
  int edgesum;       // correction count

  if (area < 1200) {
    area = 1200; // minimum value
  }

  loopstart = nullptr; // not found it yet
  edgept = startpt;    // start of loop

  do {
    // current point fixed and next not
    if (edgept->fixed && !edgept->next->fixed) {
      loopstart = edgept; // start of repoly
      break;
    }
    edgept = edgept->next;     // next point
  } while (edgept != startpt); // until found or finished

  if (loopstart == nullptr && !startpt->fixed) {
    // fixed start of loop
    startpt->fixed = true;
    loopstart = startpt; // or start of loop
  }
  if (loopstart) {
    do {
      edgept = loopstart; // first to do
      do {
        linestart = edgept;
        edgesum = 0; // sum of lengths
        do {
          // sum lengths
          edgesum += edgept->runlength;
          edgept = edgept->next; // move on
        } while (!edgept->fixed && edgept != loopstart && edgesum < 126);
        if (poly_debug) {
          tprintf("Poly2:starting at (%d,%d)+%d=(%d,%d),%d to (%d,%d)\n",
                  linestart->pos.x, linestart->pos.y, linestart->dir,
                  linestart->vec.x, linestart->vec.y, edgesum, edgept->pos.x,
                  edgept->pos.y);
        }
        // reapproximate
        cutline(linestart, edgept, area);

        while (edgept->next->fixed && edgept != loopstart) {
          edgept = edgept->next; // look for next non-fixed
        }
      }
      // do all the loop
      while (edgept != loopstart);
      edgesum = 0;
      do {
        if (edgept->fixed) {
          edgesum++;
        }
        edgept = edgept->next;
      }
      // count fixed pts
      while (edgept != loopstart);
      if (edgesum < 3) {
        area /= 2; // must have 3 pts
      }
    } while (edgesum < 3);
    do {
      linestart = edgept;
      do {
        edgept = edgept->next;
      } while (!edgept->fixed);
      linestart->next = edgept;
      edgept->prev = linestart;
      linestart->vec.x = edgept->pos.x - linestart->pos.x;
      linestart->vec.y = edgept->pos.y - linestart->pos.y;
    } while (edgept != loopstart);
  } else {
    edgept = startpt; // start of loop
  }

  loopstart = edgept; // new start
  return loopstart;   // correct exit
}

/**********************************************************************
 * tesspoly_outline
 *
 * Approximate an outline from chain codes form using the old tess algorithm.
 * If allow_detailed_fx is true, the EDGEPTs in the returned TBLOB
 * contain pointers to the input C_OUTLINEs that enable higher-resolution
 * feature extraction that does not use the polygonal approximation.
 **********************************************************************/

TESSLINE *ApproximateOutline(bool allow_detailed_fx, C_OUTLINE *c_outline) {
  EDGEPT stack_edgepts[FASTEDGELENGTH]; // converted path
  EDGEPT *edgepts = stack_edgepts;

  // Use heap memory if the stack buffer is not big enough.
  if (c_outline->pathlength() > FASTEDGELENGTH) {
    edgepts = new EDGEPT[c_outline->pathlength()];
  }

  // bounding box
  const auto &loop_box = c_outline->bounding_box();
  int32_t area = loop_box.height();
  if (!poly_wide_objects_better && loop_box.width() > area) {
    area = loop_box.width();
  }
  area *= area;
  edgesteps_to_edgepts(c_outline, edgepts);
  fix2(edgepts, area);
  EDGEPT *edgept = poly2(edgepts, area); // 2nd approximation.
  EDGEPT *startpt = edgept;
  EDGEPT *result = nullptr;
  EDGEPT *prev_result = nullptr;
  do {
    auto *new_pt = new EDGEPT;
    new_pt->pos = edgept->pos;
    new_pt->prev = prev_result;
    if (prev_result == nullptr) {
      result = new_pt;
    } else {
      prev_result->next = new_pt;
      new_pt->prev = prev_result;
    }
    if (allow_detailed_fx) {
      new_pt->src_outline = edgept->src_outline;
      new_pt->start_step = edgept->start_step;
      new_pt->step_count = edgept->step_count;
    }
    prev_result = new_pt;
    edgept = edgept->next;
  } while (edgept != startpt);
  prev_result->next = result;
  result->prev = prev_result;
  if (edgepts != stack_edgepts) {
    delete[] edgepts;
  }
  return TESSLINE::BuildFromOutlineList(result);
}

} // namespace tesseract
