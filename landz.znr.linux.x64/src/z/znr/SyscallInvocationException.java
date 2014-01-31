/**
 * Copyright 2013, Landz and its contributors. All rights reserved.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package z.znr;

/**
 *
 */
public class SyscallInvocationException extends RuntimeException {
  public SyscallInvocationException() {
    super();
  }

  public SyscallInvocationException(String message) {
    super(message);
  }

//  public SyscallInvocationException(String message, Throwable cause) {
//    super(message, cause);
//  }
//
//  public SyscallInvocationException(Throwable cause) {
//    super(cause);
//  }
//
//  protected SyscallInvocationException(String message, Throwable cause,
//                      boolean enableSuppression,
//                      boolean writableStackTrace) {
//    super(message, cause, enableSuppression, writableStackTrace);
//  }
}
