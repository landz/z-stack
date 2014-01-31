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

public class FieldData {
  public int off;
  public int len;

  public FieldData(){}

  public FieldData(int off, int len){
    this.off = off;
    this.len = len;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    FieldData fieldData = (FieldData) o;

    if (len != fieldData.len) return false;
    if (off != fieldData.off) return false;

    return true;
  }

  @Override
  public int hashCode() {
    int result = off;
    result = 31 * result + len;
    return result;
  }

  @Override
  public String toString() {
    return "FieldData{" +
        "off=" + off +
        ", len=" + len +
        '}';
  }
}
