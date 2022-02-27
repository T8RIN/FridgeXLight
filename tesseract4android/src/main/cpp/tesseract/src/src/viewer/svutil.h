///////////////////////////////////////////////////////////////////////
// File:        svutil.h
// Description: ScrollView Utilities
// Author:      Joern Wanke
//
// (C) Copyright 2007, Google Inc.
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
// http://www.apache.org/licenses/LICENSE-2.0
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
//
///////////////////////////////////////////////////////////////////////
//
// SVUtil contains the SVSync, SVSemaphore and SVNetwork
// classes, which are used for thread/process creation & synchronization
// and network connection.

#ifndef TESSERACT_VIEWER_SVUTIL_H_
#define TESSERACT_VIEWER_SVUTIL_H_

#ifdef _WIN32
#  include "host.h" // also includes windows.h
#else
#  include <semaphore.h>
#endif

#include <mutex>
#include <string>

namespace tesseract {

/// The SVSync class provides functionality for Thread & Process Creation
class SVSync {
public:
  /// Starts a new process.
  static void StartProcess(const char *executable, const char *args);
};

/// A semaphore class which encapsulates the main signaling
/// and wait abilities of semaphores for windows and unix.
class SVSemaphore {
public:
  /// Sets up a semaphore.
  SVSemaphore();
  /// Cleans up the mutex
  ~SVSemaphore();
  /// Signal a semaphore.
  void Signal();
  /// Wait on a semaphore.
  void Wait();

private:
#ifdef _WIN32
  HANDLE semaphore_;
#elif defined(__APPLE__)
  sem_t *semaphore_;
#else
  sem_t semaphore_;
#endif
};

/// The SVNetwork class takes care of the remote connection for ScrollView
/// This means setting up and maintaining a remote connection, sending and
/// receiving messages and closing the connection.
/// It is designed to work on both Linux and Windows.
class SVNetwork {
public:
  /// Set up a connection to hostname on port.
  SVNetwork(const char *hostname, int port);

  /// Destructor.
  ~SVNetwork();

  /// Put a message in the messagebuffer to the server and try to send it.
  void Send(const char *msg);

  /// Receive a message from the server.
  /// This will always return one line of char* (denoted by \\n).
  char *Receive();

  /// Close the connection to the server.
  void Close();

  /// Flush the buffer.
  void Flush();

private:
  /// The mutex for access to Send() and Flush().
  std::mutex mutex_send_;
  /// The actual stream_ to the server.
  int stream_;
  /// Stores the last received message-chunk from the server.
  char *msg_buffer_in_;

  /// Stores the messages which are supposed to go out.
  std::string msg_buffer_out_;

  /// Where we are at in our msg_buffer_in_
  char *buffer_ptr_; // strtok_r, strtok_s
};

} // namespace tesseract

#endif // TESSERACT_VIEWER_SVUTIL_H_
