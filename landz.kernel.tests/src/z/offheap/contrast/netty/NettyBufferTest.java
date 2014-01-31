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

package z.offheap.contrast.netty;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.buffer.Unpooled;

import java.util.concurrent.TimeUnit;

/**
 * Created by jin on 9/29/13.
 */
public class NettyBufferTest {

    public static void main(String[] args) {
        // A new dynamic buffer is created.  Internally, the actual buffer is created
        // lazily to avoid potentially wasted memory space.
        ByteBuf b = PooledByteBufAllocator.DEFAULT.directBuffer();

        // When the first write attempt is made, the internal buffer is created with
        // the specified initial capacity (4).
        long s = System.nanoTime();
        for (int i = 0; i < 256; i++) {
            b.writeByte(i);
        }
        long duration = System.nanoTime() - s;
        System.out.println("initial 256:   " +duration+ " nanoseconds!");

        //
        s = System.nanoTime();
        b.writeByte(1);
        duration = System.nanoTime() - s;
        System.out.println("write to 257:  " +duration+ " nanoseconds!");

        //
        for (int i = 0; i < 255; i++) {
            b.writeByte(i);
        }

        s = System.nanoTime();
        b.writeByte(1);
        duration = System.nanoTime() - s;
        System.out.println("write to 513:  " +duration+ " nanoseconds!");
        //
        for (int i = 0; i < 511; i++) {
            b.writeByte(i);
        }

        s = System.nanoTime();
        b.writeByte(1);
        duration = System.nanoTime() - s;

        System.out.println("write to 1025: " +duration+ " nanoseconds!");


        //
        int total = 256*1024;
        ByteBuf b2 = PooledByteBufAllocator.DEFAULT.directBuffer();
        s = System.nanoTime();
        for (int i = 0; i < total; i++) {
            b2.writeByte(i);
        }
        duration = System.nanoTime() - s;

        System.out.println("write 256KB data into pool direct buffer dynamically: "
                + TimeUnit.NANOSECONDS.toMillis(duration)+ " millis!");

        total = 256*1024;
        ByteBuf b3 = Unpooled.buffer();
        s = System.nanoTime();
        for (int i = 0; i < total; i++) {
            b3.writeByte(i);
        }
        duration = System.nanoTime() - s;

        //the single shot performance is unstable for unpooled
        System.out.println("write 256KB data into unpooled buffer dynamically: "
                + TimeUnit.NANOSECONDS.toMillis(duration)+ " millis!");
    }

}
