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
package org.trustedanalytics.h2oscoringengine.publisher;

import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import java.io.IOException;
import java.nio.file.Files;

import org.junit.Before;
import org.junit.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;
import org.trustedanalytics.h2oscoringengine.publisher.http.BasicAuthServerCredentials;
import org.trustedanalytics.h2oscoringengine.publisher.steps.H2oResourcesDownloadingStep;


public class PublisherIntegrationTest {

  private MockRestServiceServer h2oServerMock;
  private RestTemplate h2oRestTemplate = new RestTemplate();
  private final String testH2oServerUrl = "h2o-server-lkkajjdk";
  private final String testH2oUser = "akljfashf";
  private final String testH2oPassword = "askjfsl";

  private String engineBaseResourcePath = "/runtime/h2o-scoring-engine-base-0.5.0.jar";

  private BasicAuthServerCredentials testH2oCredentials =
      new BasicAuthServerCredentials(testH2oServerUrl, testH2oUser, testH2oPassword);
  private final String testModelName = "some_model";

  // expected requests
  private final String getModelRequest =
      H2oResourcesDownloadingStep.H2O_SERVER_MODEL_PATH_PREFIX + testModelName;
  private final String getLibRequest = H2oResourcesDownloadingStep.H2O_SERVER_LIB_PATH;

  @Before
  public void setUp() {
    h2oServerMock = MockRestServiceServer.createServer(h2oRestTemplate);
  }

  @Test
  public void getScoringEngineJar_h2oRequestsOccured() throws Exception {
    // given
    Publisher publisher = new Publisher(h2oRestTemplate, engineBaseResourcePath);
    setH2oServerExpectedCalls();

    // when
    publisher.getScoringEngineJar(testH2oCredentials, testModelName);

    // then
    h2oServerMock.verify();
  }

  private void setH2oServerExpectedCalls() throws IOException {
    h2oServerMock.expect(requestTo(testH2oServerUrl + getModelRequest))
        .andExpect(method(HttpMethod.GET))
        .andRespond(withSuccess(prepareModelJavaFile(), MediaType.APPLICATION_OCTET_STREAM));
    h2oServerMock.expect(requestTo(testH2oServerUrl + getLibRequest))
        .andExpect(method(HttpMethod.GET))
        .andRespond(withSuccess(prepareGenModelLib(), MediaType.APPLICATION_OCTET_STREAM));
  }

  private byte[] prepareModelJavaFile() throws IOException {
    TestCompilationResourcesBuilder compilationResourcesBuilder =
        new TestCompilationResourcesBuilder();
    return Files.readAllBytes(compilationResourcesBuilder.prepareModelJavaFile(testModelName));
  }

  private byte[] prepareGenModelLib() throws IOException {
    TestCompilationResourcesBuilder compilationResourcesBuilder =
        new TestCompilationResourcesBuilder();
    return Files.readAllBytes(compilationResourcesBuilder.prepareLibraryFile());
  }
}
