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

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.trustedanalytics.h2oscoringengine.publisher.tapapi.ServiceCreator
    .TAP_API_SERVICE_CREATE_SERVICE_INSTANCE_PATH;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

public class ServiceCreatorTest {

  private final RestTemplate tapApiRestTemplateMock = mock(RestTemplate.class);
  private final String tapApiTestUrl = "http://tap-api";

  private final String testName = "sample-instance";
  private final String testOfferingId = "offering-1";
  private final String testPlanId = "plan-1";
  private final String testModelId = "model-1";
  private final String testArtifactId = "artifact-1";

  @Rule
  public final ExpectedException thrown = ExpectedException.none();

  @Test
  public void createServiceInstance_TapApiCreatedServiceInstance_NoError()
      throws Exception {
    // given
    when(tapApiRestTemplateMock.exchange(eq(tapApiTestUrl + TAP_API_SERVICE_CREATE_SERVICE_INSTANCE_PATH),
        eq(HttpMethod.POST), any(), eq(String.class))).thenReturn(
            new ResponseEntity<String>("{}", HttpStatus.OK));

    ServiceCreator sut =
        new ServiceCreator(tapApiRestTemplateMock, tapApiTestUrl, new ObjectMapper());

    // when
    // then
    sut.createServiceInstance(testName, testOfferingId, testPlanId, testModelId, testArtifactId);
  }

  @Test
  public void createServiceInstance_TapApiRespondedWithError_ExceptionThrown() throws Exception {
    // given
    ServiceCreator sut =
        new ServiceCreator(tapApiRestTemplateMock, tapApiTestUrl, new ObjectMapper());

    String tapApiErrorMsg = "Some error message from tap-api-service";
    when(tapApiRestTemplateMock.exchange(
        eq(tapApiTestUrl + TAP_API_SERVICE_CREATE_SERVICE_INSTANCE_PATH),
        eq(HttpMethod.POST), any(), eq(String.class)))
            .thenThrow(new HttpServerErrorException(HttpStatus.INTERNAL_SERVER_ERROR, tapApiErrorMsg));

    // when
    // then
    thrown.expect(IOException.class);
    sut.createServiceInstance(testName, testOfferingId, testPlanId, testModelId, testArtifactId);
  }
}
