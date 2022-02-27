/**********************************************************************
 * File:        oldbasel.h  (Formerly oldbl.h)
 * Description: A re-implementation of the old baseline algorithm.
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

#ifndef OLDBASEL_H
#define OLDBASEL_H

#include "blobbox.h"
#include "params.h"

namespace tesseract {

extern BOOL_VAR_H(textord_oldbl_debug);

int get_blob_coords(    // get boxes
    TO_ROW *row,        // row to use
    int32_t lineheight, // block level
    TBOX *blobcoords,   // output boxes
    bool &holed_line,   // lost a lot of blobs
    int &outcount       // no of real blobs
);
void make_first_baseline( // initial approximation
    TBOX blobcoords[],    /*blob bounding boxes */
    int blobcount,        /*no of blobcoords */
    int xcoords[],        /*coords for spline */
    int ycoords[],        /*approximator */
    QSPLINE *spline,      /*initial spline */
    QSPLINE *baseline,    /*output spline */
    float jumplimit       /*guess half descenders */
);
void make_holed_baseline( // initial approximation
    TBOX blobcoords[],    /*blob bounding boxes */
    int blobcount,        /*no of blobcoords */
    QSPLINE *spline,      /*initial spline */
    QSPLINE *baseline,    /*output spline */
    float gradient        // of line
);
int partition_line(    // partition blobs
    TBOX blobcoords[], // bounding boxes
    int blobcount,     /*no of blobs on row */
    int *numparts,     /*number of partitions */
    char partids[],    /*partition no of each blob */
    int partsizes[],   /*no in each partition */
    QSPLINE *spline,   /*curve to fit to */
    float jumplimit,   /*allowed delta change */
    float ydiffs[]     /*diff from spline */
);
void merge_oldbl_parts( // partition blobs
    TBOX blobcoords[],  // bounding boxes
    int blobcount,      /*no of blobs on row */
    char partids[],     /*partition no of each blob */
    int partsizes[],    /*no in each partition */
    int biggestpart,    // major partition
    float jumplimit     /*allowed delta change */
);
int get_ydiffs(        // evaluate differences
    TBOX blobcoords[], // bounding boxes
    int blobcount,     /*no of blobs */
    QSPLINE *spline,   /*approximating spline */
    float ydiffs[]     /*output */
);
int choose_partition(                               // select partition
    float diff,                                     /*diff from spline */
    float partdiffs[],                              /*diff on all parts */
    int lastpart,                                   /*last assigned partition */
    float jumplimit,                                /*new part threshold */
    float *drift, float *last_delta, int *partcount /*no of partitions */
);
int partition_coords(  // find relevant coords
    TBOX blobcoords[], // bounding boxes
    int blobcount,     /*no of blobs in row */
    char partids[],    /*partition no of each blob */
    int bestpart,      /*best new partition */
    int xcoords[],     /*points to work on */
    int ycoords[]      /*points to work on */
);
int segment_spline(             // make xstarts
    TBOX blobcoords[],          // boundign boxes
    int blobcount,              /*no of blobs in row */
    int xcoords[],              /*points to work on */
    int ycoords[],              /*points to work on */
    int degree, int pointcount, /*no of points */
    int xstarts[]               // result
);
bool split_stepped_spline( // make xstarts
    QSPLINE *baseline,     // current shot
    float jumplimit,       // max step function
    int *xcoords,          /*points to work on */
    int *xstarts,          // result
    int &segments          // no of segments
);
void insert_spline_point(     // get descenders
    int xstarts[],            // starts to shuffle
    int segment,              // insertion pt
    int coord1,               // coords to add
    int coord2, int &segments // total segments
);
void find_lesser_parts( // get descenders
    TO_ROW *row,        // row to process
    TBOX blobcoords[],  // bounding boxes
    int blobcount,      /*no of blobs */
    char partids[],     /*partition of each blob */
    int partsizes[],    /*size of each part */
    int partcount,      /*no of partitions */
    int bestpart        /*biggest partition */
);

void old_first_xheight( // the wiseowl way
    TO_ROW *row,        /*current row */
    TBOX blobcoords[],  /*blob bounding boxes */
    int initialheight,  // initial guess
    int blobcount,      /*blobs in blobcoords */
    QSPLINE *baseline,  /*established */
    float jumplimit     /*min ascender height */
);

void make_first_xheight( // find xheight
    TO_ROW *row,         /*current row */
    TBOX blobcoords[],   /*blob bounding boxes */
    int lineheight,      // initial guess
    int init_lineheight, // block level guess
    int blobcount,       /*blobs in blobcoords */
    QSPLINE *baseline,   /*established */
    float jumplimit      /*min ascender height */
);

int *make_height_array( // get array of heights
    TBOX blobcoords[],  /*blob bounding boxes */
    int blobcount,      /*blobs in blobcoords */
    QSPLINE *baseline   /*established */
);

void find_top_modes(            // get modes
    STATS *stats,               // stats to hack
    int statnum,                // no of piles
    int modelist[], int modenum // no of modes to get
);

void pick_x_height(TO_ROW *row, // row to do
                   int modelist[], int lefts[], int rights[], STATS *heightstat,
                   int mode_threshold);

} // namespace tesseract

#endif
