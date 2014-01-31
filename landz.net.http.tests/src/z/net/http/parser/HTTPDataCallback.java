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

package z.net.http.parser;

import java.nio.ByteBuffer;

@FunctionalInterface
public interface HTTPDataCallback {
	/*
		very raw and extremly foolhardy! DANGER!
		The whole Buffer concept is difficult enough to grasp as it is,
		we pass in a buffer with an arbitrary position.

		The interesting data is located at position pos and is len 
		bytes long.
		
		The contract of this callback is that the buffer is
		returned in the state that it was passed in, so implementing
		this require good citizenship, you'll need to remember the current
		position, change the position to get at the data you're interested 
		in and then set the position back to how you found it...

		//TODO: there should be an abstract implementation that implements
		cb as described above, marks it final an provides a new callback
		with signature cb(byte[], int, int)
	*/
	public int cb(HTTPParser p, ByteBuffer buf, int pos, int len);
}
