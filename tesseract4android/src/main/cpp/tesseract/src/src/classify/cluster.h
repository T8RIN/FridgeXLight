/******************************************************************************
 ** Filename:   cluster.h
 ** Purpose:    Definition of feature space clustering routines
 ** Author:     Dan Johnson
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
 *****************************************************************************/

#ifndef CLUSTER_H
#define CLUSTER_H

#include "kdtree.h"
#include "oldlist.h"

namespace tesseract {

struct BUCKETS;

#define MINBUCKETS 5
#define MAXBUCKETS 39

/*----------------------------------------------------------------------
          Types
----------------------------------------------------------------------*/
struct CLUSTER {
  CLUSTER(size_t n) : Mean(n) {
  }

  ~CLUSTER() {
    delete Left;
    delete Right;
  }

  bool Clustered : 1;        // true if included in a higher cluster
  bool Prototype : 1;        // true if cluster represented by a proto
  unsigned SampleCount : 30; // number of samples in this cluster
  CLUSTER *Left;       // ptr to left sub-cluster
  CLUSTER *Right;      // ptr to right sub-cluster
  int32_t CharID;            // identifier of char sample came from
  std::vector<float> Mean;   // mean of cluster - SampleSize floats
};
using SAMPLE = CLUSTER; // can refer to as either sample or cluster

typedef enum { spherical, elliptical, mixed, automatic } PROTOSTYLE;

struct CLUSTERCONFIG {   // parameters to control clustering
  PROTOSTYLE ProtoStyle; // specifies types of protos to be made
  float MinSamples;      // min # of samples per proto - % of total
  float MaxIllegal;      // max percentage of samples in a cluster which
                         // have more than 1 feature in that cluster
  float Independence;    // desired independence between dimensions
  double Confidence;     // desired confidence in prototypes created
  int MagicSamples;      // Ideal number of samples in a cluster.
};

typedef enum { normal, uniform, D_random, DISTRIBUTION_COUNT } DISTRIBUTION;

union FLOATUNION {
  float Spherical;
  float *Elliptical;
};

struct PROTOTYPE {
  bool Significant : 1;     // true if prototype is significant
  bool Merged : 1;          // Merged after clustering so do not output
                            // but kept for display purposes. If it has no
                            // samples then it was actually merged.
                            // Otherwise it matched an already significant
                            // cluster.
  unsigned Style : 2;       // spherical, elliptical, or mixed
  unsigned NumSamples : 28; // number of samples in the cluster
  CLUSTER *Cluster;         // ptr to cluster which made prototype
  std::vector<DISTRIBUTION> Distrib; // different distribution for each dimension
  std::vector<float> Mean;  // prototype mean
  float TotalMagnitude;     // total magnitude over all dimensions
  float LogMagnitude;       // log base e of TotalMagnitude
  FLOATUNION Variance;      // prototype variance
  FLOATUNION Magnitude;     // magnitude of density function
  FLOATUNION Weight;        // weight of density function
};

struct CLUSTERER {
  int16_t SampleSize;      // number of parameters per sample
  PARAM_DESC *ParamDesc;   // description of each parameter
  int32_t NumberOfSamples; // total number of samples being clustered
  KDTREE *KDTree;          // for optimal nearest neighbor searching
  CLUSTER *Root;           // ptr to root cluster of cluster tree
  LIST ProtoList;          // list of prototypes
  uint32_t NumChar;        // # of characters represented by samples
  // cache of reusable histograms by distribution type and number of buckets.
  BUCKETS *bucket_cache[DISTRIBUTION_COUNT][MAXBUCKETS + 1 - MINBUCKETS];
};

struct SAMPLELIST {
  int32_t NumSamples;    // number of samples in list
  int32_t MaxNumSamples; // maximum size of list
  SAMPLE *Sample[1];     // array of ptrs to sample data structures
};

// low level cluster tree analysis routines.
#define InitSampleSearch(S, C) (((C) == nullptr) ? (S = NIL_LIST) : (S = push(NIL_LIST, (C))))

/*--------------------------------------------------------------------------
        Public Function Prototypes
--------------------------------------------------------------------------*/
TESS_API
CLUSTERER *MakeClusterer(int16_t SampleSize, const PARAM_DESC ParamDesc[]);

TESS_API
SAMPLE *MakeSample(CLUSTERER *Clusterer, const float *Feature, uint32_t CharID);

TESS_API
LIST ClusterSamples(CLUSTERER *Clusterer, CLUSTERCONFIG *Config);

TESS_API
void FreeClusterer(CLUSTERER *Clusterer);

TESS_API
void FreeProtoList(LIST *ProtoList);

void FreePrototype(void *arg); // PROTOTYPE *Prototype);

CLUSTER *NextSample(LIST *SearchState);

float Mean(PROTOTYPE *Proto, uint16_t Dimension);

float StandardDeviation(PROTOTYPE *Proto, uint16_t Dimension);

TESS_API
int32_t MergeClusters(int16_t N, PARAM_DESC ParamDesc[], int32_t n1, int32_t n2, float m[],
                      float m1[], float m2[]);

} // namespace tesseract

#endif
