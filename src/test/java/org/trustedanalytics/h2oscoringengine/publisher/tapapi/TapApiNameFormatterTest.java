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

import static org.junit.Assert.assertEquals;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class TapApiNameFormatterTest {
  
  @Rule
  public final ExpectedException thrown = ExpectedException.none();

  @Test
  public void format_validStringProvided_theSameStringReturned() throws Exception {
    // given
    String validNameString = "some-valid-offering-name";
    String expectedString = validNameString;
    
    // when
    String actualString = TapApiNameFormatter.format(validNameString);

    // then
    assertEquals(expectedString, actualString);
  }
  
  @Test
  public void format_invalidStringProvided_validStringReturned() throws Exception {
    // given
    String stringWithForbiddenCharacters = "-&$()@$%some_String_*&with_^%$#@invaliD___characters)(*&^%$#@!";
    String expectedString = "some-string-with-invalid-characters";

    // when
    String actualString = TapApiNameFormatter.format(stringWithForbiddenCharacters);

    // then
    assertEquals(expectedString, actualString);
  }
  
  @Test
  public void format_nullStringProvided_exceptionThrown() throws Exception {
    // given
    String nameToFormat = null;
    
    // when
    // then
    thrown.expect(IllegalArgumentException.class);
    TapApiNameFormatter.format(nameToFormat);
  }
}
