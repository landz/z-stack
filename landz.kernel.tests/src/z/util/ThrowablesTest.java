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

package z.util;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.net.URI;
import java.net.URISyntaxException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

/**
 * Created by jin on 7/31/13.
 */
public class ThrowablesTest {

    @Rule
    public ExpectedException exception = ExpectedException.none();

    @Test
    public void uncheckTest() {
        Throwables.uncheck(() -> Thread.sleep(500));

        exception.expect(RuntimeException.class);
        Throwables.uncheck(() -> new URI("xxx://"));
    }

    @Test
    public void uncheckToTest() {
        URI uri = Throwables.uncheckTo(() -> new URI(""));

        exception.expect(RuntimeException.class);
        uri = Throwables.uncheckTo(() -> new URI("xxx://"));
    }

    @Test
    public void checkTestForURISyntaxException() {
        exception.expect(RuntimeException.class);
        Throwables.check(() -> new URI("xxx://"))
                .onException(URISyntaxException.class, e -> System.out.println("on URISyntaxException!"));
    }

    @Test
    public void checkTestForNPE() {
        exception.expect(RuntimeException.class);
        Throwables.check(() -> new URI(null))
                .onException(NullPointerException.class, e -> System.out.println("on NullPointerException!"));
    }

    @Test
    public void checkTestForNPE2() {
        StringBuilder sb = new StringBuilder();
        exception.expect(RuntimeException.class);
        Throwables.check(() -> {sb.append("NPE"); new URI(null);} )
                .onException(NullPointerException.class, e -> assertThat(sb.toString(), is("NPE")) );
    }

//    @Test
//    public void checkTestForTwoExceptions() {
//        StringBuilder sb = new StringBuilder();
//        exception.expect(RuntimeException.class);
//        Throwables.check(() -> {sb.append("NPE"); new URI(null);} )
//                .onException(NullPointerException.class, e -> assertThat(sb.toString(), is("NPE")) )
//                .onException(URISyntaxException.class,   e -> System.out.println("on URISyntaxException!"));
//
//    }
}
