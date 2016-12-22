/**
 * Copyright (c) 2016 Intel Corporation
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package org.trustedanalytics.h2oscoringengine.publisher.tapapi;

public class TapApiNameFormatter {

  public static String format(String nameToFormat) {
    if (nameToFormat == null) {
      throw new IllegalArgumentException("String to be formatted cannot be null.");
    }

    // lowercasing and replacing all forbidden characters with dash
    String regex = "[^a-z0-9]+";
    String formattedName = nameToFormat.toLowerCase().replaceAll(regex, "-");

    // removing dashes at the beginning and at the end of the string
    String regex2 = "^[-]|[-]$";
    formattedName = formattedName.replaceAll(regex2, "");

    return formattedName;
  }
}
