/**
 * Copyright (c) 2015 Intel Corporation
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
package org.trustedanalytics.h2oscoringengine.publisher.restapi;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.nio.file.Paths;

import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.trustedanalytics.h2oscoringengine.publisher.EngineBuildingException;
import org.trustedanalytics.h2oscoringengine.publisher.Publisher;
import org.trustedanalytics.h2oscoringengine.publisher.http.BasicAuthServerCredentials;
import org.trustedanalytics.h2oscoringengine.publisher.restapi.validation.DownloadRequestValidationRules;
import org.trustedanalytics.h2oscoringengine.publisher.restapi.validation.ValidationException;

public class PublisherControllerTest {

  private Publisher publisherMock = mock(Publisher.class);
  private String testHost = "http://example.com";
  private String testUsername = "username";
  private String testPassword = "password";
  private String testModelName = "some-model-name";
  private MultiValueMap<String, String> testPostRequest = new LinkedMultiValueMap<>();
  private String testExceptionMesage = "Some test message";

  @Test
  public void downloadEngine_callsPublisher() throws Exception {
    // given
    PublisherController controller =
        new PublisherController(publisherMock, new DownloadRequestValidationRules());
    testPostRequest.add("host", testHost);
    testPostRequest.add("username", testUsername);
    testPostRequest.add("password", testPassword);
    ArgumentCaptor<BasicAuthServerCredentials> credentialsCaptor =
        ArgumentCaptor.forClass(BasicAuthServerCredentials.class);

    // when
    when(publisherMock.getScoringEngineJar(any(), any())).thenReturn(Paths.get("/tmp/"));
    controller.downloadEngine(new MockHttpServletResponse(), testPostRequest, testModelName);

    // then
    verify(publisherMock).getScoringEngineJar(credentialsCaptor.capture(), eq(testModelName));
    assertEquals(testHost, credentialsCaptor.getValue().getHost());
    assertEquals(testUsername, credentialsCaptor.getValue().getUsername());
    assertEquals(testPassword, credentialsCaptor.getValue().getPassword());
  }

  @Test
  public void handleIllegalArgumentException_returnsExceptionMessage() {
    // given
    PublisherController controller =
        new PublisherController(publisherMock, new DownloadRequestValidationRules());

    // when
    ValidationException testException = new ValidationException(new Exception(testExceptionMesage));
    String message =
        controller.handleIllegalArgumentException(testException);

    // then
    assertThat(message, is(equalTo(testException.getMessage())));
  }

  @Test
  public void handleEngineBuildingException_returnsExceptionMessage()
      throws EngineBuildingException {
    // given
    PublisherController controller =
        new PublisherController(publisherMock, new DownloadRequestValidationRules());

    // when
    EngineBuildingException testException = new EngineBuildingException(testExceptionMesage);
    when(publisherMock.getScoringEngineJar(any(), any())).thenThrow(testException);

    String message =
        controller.handleEngineBuildingException(testException);

    // then
    assertThat(message, is(equalTo(testException.getMessage())));
  }
}
