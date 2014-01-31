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

package z.net;

import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static z.znr.event.EPollEvents.*;
/**
 */
public class HelpersTest {

  @Test
  public void testHelpers() {
    int mask = EPOLLERR | EPOLLHUP | EPOLLIN | EPOLLOUT | EPOLLRDHUP;
    assertThat(isErrorEvent(mask),  is(true));
//    System.out.println(isErrorMask(mask));

    mask = EPOLLERR | EPOLLHUP;
    assertThat(isErrorEvent(mask), is(true));

    mask = EPOLLERR;
    assertThat(isErrorEvent(mask), is(true));

    mask = EPOLLHUP;
    assertThat(isErrorEvent(mask), is(true));
     

    mask = EPOLLIN | EPOLLOUT | EPOLLRDHUP;
    assertThat(isErrorEvent(mask), is(false));

    mask = EPOLLIN;
    assertThat(isErrorEvent(mask),  is(false));

    mask = EPOLLOUT;
    assertThat(isErrorEvent(mask),  is(false));

    //========================================

    mask = EPOLLRDHUP;
    assertThat(isCloseEvent(mask),  is(true));

    mask = EPOLLERR | EPOLLHUP | EPOLLIN | EPOLLOUT | EPOLLRDHUP;
    assertThat(isCloseEvent(mask),  is(true));

    mask = EPOLLERR | EPOLLHUP | EPOLLIN | EPOLLOUT;
    assertThat(isCloseEvent(mask),  is(false));

    mask = EPOLLERR;
    assertThat(isCloseEvent(mask),  is(false));

    mask = EPOLLIN;
    assertThat(isCloseEvent(mask),  is(false));


  }


  private static final boolean isErrorEvent(int mask) {
    return (mask & ( EPOLLERR | EPOLLHUP ) )!=0;
  }

  private static final boolean isCloseEvent(int mask) {
    return (mask & EPOLLRDHUP )!=0;
  }


}
