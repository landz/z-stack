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

import java.util.LinkedList;
import java.util.List;
import static jnr.x86asm.LABEL_STATE.*;

/**
 *
 */
public final class Label extends Operand {

    /** Label Id (0 means unknown). */
    final int id;
    /** State of label, see {@link LABEL_STATE}. */
    LABEL_STATE state;
    /** Position (always positive, information depends to @c state). */
    int position;

    final List<LinkData> links = new LinkedList<LinkData>();

    public Label() {
        this(0);
    }

    public Label(int id) {
        super(OP.OP_LABEL, 4);
        this.id = id;
        this.state = LABEL_STATE_UNUSED;
        this.position = -1;
    }

    /** Returns @c true if label is unused (not bound or linked). */
    final boolean isUnused() {
        return state == LABEL_STATE_UNUSED;
    }

    /** Returns @c true if label is linked. */
    final boolean isLinked() {
        return state == LABEL_STATE_LINKED;
    }

    /** Returns @c true if label is bound. */
    final boolean isBound() {
        return state == LABEL_STATE_BOUND;
    }

    /** Returns the position of bound or linked labels, -1 if label
     * is unused.
     */
    final int position() {
        return position;
    }

    final void link(LinkData link) {
        links.add(link);
        state = LABEL_STATE_LINKED;
    }
}
