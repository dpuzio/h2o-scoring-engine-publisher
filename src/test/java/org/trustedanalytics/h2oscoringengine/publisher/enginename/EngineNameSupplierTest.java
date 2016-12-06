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
package org.trustedanalytics.h2oscoringengine.publisher.enginename;

import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.trustedanalytics.h2oscoringengine.publisher.enginename.EngineNameSupplier.KEY_COUNTER_ENDPOINT;

import java.io.IOException;
import java.nio.charset.Charset;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;
import org.trustedanalytics.h2oscoringengine.publisher.http.BasicAuthServerCredentials;

public class EngineNameSupplierTest {

  private final RestTemplate keyCounterRestTemplateMock = mock(RestTemplate.class);

  private final String keyCounterTestUrl = "http://some-url";
  private final String keyCounterTestUser = "some-user";
  private final String keyCounterTestPassword = "some-password";
  private final KeyCounterConnectionData testKeyCounterConnectionData=
      new KeyCounterConnectionData(keyCounterRestTemplateMock, new BasicAuthServerCredentials(
          keyCounterTestUrl, keyCounterTestUser, keyCounterTestPassword));
  private final EngineNameSupplier sut = new EngineNameSupplier(testKeyCounterConnectionData);

  private final String testModelName = "some-name";
  private final String testModelId = "some-id";
  private final String keyCounterModelIdentifier = testModelName + testModelId.substring(0, 4);
  private final String keyCounterEndpoint =
      keyCounterTestUrl + KEY_COUNTER_ENDPOINT + keyCounterModelIdentifier;

  @Rule
  public final ExpectedException thrown = ExpectedException.none();

  @Test
  public void generateName_KeyCounterRespondendWithSuffix_ProperNameGenerated() throws Exception {
    // given
    when(keyCounterRestTemplateMock.exchange(eq(keyCounterEndpoint), eq(HttpMethod.POST), any(),
        eq(Integer.class))).thenReturn(new ResponseEntity<>(1, HttpStatus.OK));

    // when
    String actualName = sut.generateName(testModelName, testModelId);

    // then
    assertThat(actualName, containsString(testModelName));
    assertThat(actualName, containsString("se001"));
  }

  @Test
  public void generateName_KeyCounterThrownException_IOExceptionThrownWithBodyInMessage()
      throws Exception {
    // given
    String expectedMessage = "Error from key-counter";
    when(keyCounterRestTemplateMock.exchange(eq(keyCounterEndpoint), eq(HttpMethod.POST), any(),
        eq(Integer.class)))
            .thenThrow(new HttpServerErrorException(HttpStatus.INTERNAL_SERVER_ERROR,
                "Internal Server Error", expectedMessage.getBytes(), Charset.defaultCharset()));

    // when
    // then
    thrown.expect(IOException.class);
    thrown.expectMessage(containsString(expectedMessage));
    sut.generateName(testModelName, testModelId);
  }
}
