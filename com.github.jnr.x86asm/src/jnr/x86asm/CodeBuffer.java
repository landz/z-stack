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

package jnr.x86asm;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 *
 */
final class CodeBuffer {
    private ByteBuffer buf;

    public CodeBuffer() {
        buf = ByteBuffer.allocate(128).order(ByteOrder.LITTLE_ENDIAN);
    }

    public final void ensureSpace() {
        if (buf.remaining() < 16) {
            grow();
        }
    }

    public void grow() {
        int newSize = buf.capacity() * 2;
        ByteBuffer newBuffer = ByteBuffer.allocate(newSize).order(ByteOrder.LITTLE_ENDIAN);

        // Copy the data over
        buf.flip();
        newBuffer.put(buf);
        buf = newBuffer;
    }

    final void copyTo(ByteBuffer dst) {
        ByteBuffer dup = buf.duplicate();
        dup.flip();
        dst.put(dup);
    }

    public final int offset() {
        return buf.position();
    }
    
    public int capacity() {
        return buf.capacity();
    }
    
    public final void emitByte(byte x) {
        buf.put(x);
    }
    
    public final void emitWord(short x) {
        buf.putShort(x);
    }
    
    public final void emitDWord(int x) {
        buf.putInt(x);
    }
    
    public final void emitQWord(long x) {
        buf.putLong(x);
    }
    
    public final void emitData(ByteBuffer data, int len) {

        ByteBuffer dup = data.duplicate();
        if (dup.remaining() > len) {
            dup.limit(dup.position() + len);
        }
        
        buf.put(dup);
    }
    
    public final byte getByteAt(int pos) {
        return buf.get(pos);
    }

    public final short getWordAt(int pos) {
        return buf.getShort(pos);
    }
    
    public final int getDWordAt(int pos) {
        return buf.getInt(pos);
    }
    
    public final long getQWordAt(int pos) {
        return buf.getLong(pos);
    }
    
    public final void setByteAt(int pos, byte x) {
        buf.put(pos, x);
    }

    public final void setWordAt(int pos, short x) {
        buf.putShort(pos, x);
    }

    public final void setDWordAt(int pos, int x) {
        buf.putInt(pos, x);
    }
    
    public final void setQWordAt(int pos, long x) {
        buf.putLong(pos, x);
    }

}
