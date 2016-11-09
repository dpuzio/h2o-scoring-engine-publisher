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
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.trustedanalytics.h2oscoringengine.publisher.tapapi.OfferingCreator.TAP_API_SERVICE_CREATE_OFFERING_PATH;
import static org.trustedanalytics.h2oscoringengine.publisher.tapapi.TestTapApiResponses.offeringCreated;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import org.trustedanalytics.h2oscoringengine.publisher.restapi.ScoringEngineData;

public class OfferingCreatorTest {

  private final RestTemplate tapApiRestTemplateMock = mock(RestTemplate.class);
  private final String tapApiTestUrl = "http://tap-api";
  private final ScoringEngineData testScoringEngineData =
      new ScoringEngineData("some-model-id", "some-artifact-od", "some-model-name");
  private final byte[] testEngineBytes = "some-string".getBytes();

  @Rule
  public final ExpectedException thrown = ExpectedException.none();

  @Test
  public void createJavaScoringEngineOffering_TapApiCreatedOffering_OfferingIdReturned()
      throws Exception {
    // given
    String expectedOfferingId = "some-id";
    when(tapApiRestTemplateMock.exchange(eq(tapApiTestUrl + TAP_API_SERVICE_CREATE_OFFERING_PATH),
        eq(HttpMethod.POST), any(), eq(String.class))).thenReturn(
            new ResponseEntity<String>(offeringCreated(expectedOfferingId), HttpStatus.OK));

    OfferingCreator sut =
        new OfferingCreator(tapApiRestTemplateMock, tapApiTestUrl, new ObjectMapper());

    // when
    OfferingData actualOffering =
        sut.createJavaScoringEngineOffering(testScoringEngineData, testEngineBytes);

    // then
    assertEquals(expectedOfferingId, actualOffering.getOfferingId());
  }

  @Test
  public void createJavaScoringEngineOffering_TapApiReturnedUnparsableJson_ExceptionOccured()
      throws Exception {
    // given
    when(tapApiRestTemplateMock.exchange(eq(tapApiTestUrl + TAP_API_SERVICE_CREATE_OFFERING_PATH),
        eq(HttpMethod.POST), any(), eq(String.class)))
            .thenReturn(new ResponseEntity<String>("not-a-json", HttpStatus.OK));

    OfferingCreator sut =
        new OfferingCreator(tapApiRestTemplateMock, tapApiTestUrl, new ObjectMapper());

    // when
    // then
    thrown.expect(OfferingCreationException.class);
    sut.createJavaScoringEngineOffering(testScoringEngineData, testEngineBytes);
  }

  @Test
  public void createJavaScoringEngine_TapApiRespondedWithError_ExceptionThrown() throws Exception {
    // given
    OfferingCreator sut =
        new OfferingCreator(tapApiRestTemplateMock, tapApiTestUrl, new ObjectMapper());

    String tapApiErrorMsg = "Some error message from tap-api-service";
    when(tapApiRestTemplateMock.exchange(eq(tapApiTestUrl + TAP_API_SERVICE_CREATE_OFFERING_PATH),
        eq(HttpMethod.POST), any(), eq(String.class)))
            .thenReturn(new ResponseEntity<String>(tapApiErrorMsg, HttpStatus.INTERNAL_SERVER_ERROR));

    // when
    // then
    thrown.expect(OfferingCreationException.class);
    sut.createJavaScoringEngineOffering(testScoringEngineData, testEngineBytes);
  }
}
