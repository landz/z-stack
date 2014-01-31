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

/**
 */
public enum Property {

  SOMETHING("something");

  private final String key;

  private Property(String key) {
    this.key = key;
  }

  /**
   * Returns the key used to lookup this system property.
   */
  public String key() {
    return key;
  }

  /**
   * Returns the current value for this system property by delegating to
   * {@link System#getProperty(String)}.
   */
  public String value() {
    return System.getProperty(key);
  }

  /**
   * Sets the system property indicated by the specified key.
   * <p>
   * this method now delegates to {@link System#setProperty(String, String)}.
   * <p>
   *
   * @param      value the value of the system property.
   * @return     the previous value of the system property,
   *             or <code>null</code> if it did not have one.

   */
  public String setValue(String value) {
    return System.setProperty(key, value);
  }

  /**
   * Returns a string representation of this system property.
   */
  @Override public String toString() {
    return key() + "=" + value();
  }
}
