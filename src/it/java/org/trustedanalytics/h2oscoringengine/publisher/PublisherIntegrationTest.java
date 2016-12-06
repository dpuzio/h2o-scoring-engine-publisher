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


import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;
import static org.trustedanalytics.h2oscoringengine.publisher.tapapi.TestTapApiResponses.offeringCreated;
import static org.trustedanalytics.h2oscoringengine.publisher.tapapi.TestTapApiResponses.offeringReady;
import static org.trustedanalytics.h2oscoringengine.publisher.tapapi.TestTapApiResponses.oneOfferingString;

import com.fasterxml.jackson.core.JsonProcessingException;
import java.io.IOException;
import java.nio.file.Files;
import java.util.UUID;
import org.junit.Before;
import org.junit.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;
import org.trustedanalytics.h2oscoringengine.publisher.enginename.EngineNameSupplier;
import org.trustedanalytics.h2oscoringengine.publisher.http.BasicAuthServerCredentials;
import org.trustedanalytics.h2oscoringengine.publisher.modelcatalog.ModelCatalogMocks;
import org.trustedanalytics.h2oscoringengine.publisher.restapi.ScoringEngineData;
import org.trustedanalytics.h2oscoringengine.publisher.steps.H2oResourcesDownloadingStep;
import org.trustedanalytics.h2oscoringengine.publisher.tapapi.OfferingCreator;
import org.trustedanalytics.h2oscoringengine.publisher.tapapi.OfferingsFetcher;
import org.trustedanalytics.h2oscoringengine.publisher.tapapi.ServiceCreator;
import org.trustedanalytics.modelcatalog.rest.client.ModelCatalogReaderClient;


public class PublisherIntegrationTest {

  private MockRestServiceServer h2oServerMock;
  private MockRestServiceServer tapApiServerMock;
  private ModelCatalogReaderClient modelCatalogMock;
  private EngineNameSupplier engineNameSupplierMock;
  private RestTemplate h2oRestTemplate = new RestTemplate();
  private RestTemplate tapApiRestTemplate = new RestTemplate();
  private String testTapApiServiceUrl = "tap-api-kajdhf";
  private final String testH2oServerUrl = "h2o-server-lkkajjdk";
  private final String testH2oUser = "akljfashf";
  private final String testH2oPassword = "askjfsl";
  private final UUID testModelId = UUID.randomUUID();
  private final UUID testArtifactId = UUID.randomUUID();

  private String engineBaseResourcePath = "/runtime/h2o-scoring-engine-base-0.5.0.jar";

  private BasicAuthServerCredentials testH2oCredentials =
      new BasicAuthServerCredentials(testH2oServerUrl, testH2oUser, testH2oPassword);
  private final String testModelName = "some_model";

  // expected requests
  private final String getModelRequest =
      H2oResourcesDownloadingStep.H2O_SERVER_MODEL_PATH_PREFIX + testModelName;
  private final String getLibRequest = H2oResourcesDownloadingStep.H2O_SERVER_LIB_PATH;
  private final String getOfferingsRequest = OfferingsFetcher.TAP_API_SERVICE_OFFERINGS_PATH;
  private final String createOfferingRequest = OfferingCreator.TAP_API_SERVICE_CREATE_OFFERING_PATH;
  private final String createServiceInstanceRequest =
      ServiceCreator.TAP_API_SERVICE_CREATE_SERVICE_INSTANCE_PATH;

  @Before
  public void setUp() throws Exception {
    h2oServerMock = MockRestServiceServer.createServer(h2oRestTemplate);
    tapApiServerMock = MockRestServiceServer.createServer(tapApiRestTemplate);
    engineNameSupplierMock = mock(EngineNameSupplier.class);
    when(engineNameSupplierMock.generateName(eq(testModelName), any()))
        .thenReturn("some-engine-name");
    modelCatalogMock = ModelCatalogMocks.mockThatReturnsArtifact(testModelId, testArtifactId);
  }

  @Test
  public void getScoringEngineJar_h2oRequestsOccured() throws Exception {
    // given
    Publisher publisher = new Publisher(h2oRestTemplate, new RestTemplate(), "tap-api-host",
        engineBaseResourcePath, modelCatalogMock, engineNameSupplierMock);
    setH2oServerExpectedCalls();

    // when
    publisher.getScoringEngineJar(testH2oCredentials, testModelName);

    // then
    h2oServerMock.verify();
  }

  @Test
  public void publishScoringEngine_tapApiServiceRequestsOccured() throws Exception {
    // given
    Publisher sut = new Publisher(h2oRestTemplate, tapApiRestTemplate, testTapApiServiceUrl,
        engineBaseResourcePath, modelCatalogMock, engineNameSupplierMock);
    setTapApiServiceExpectedCalls();

    // when
    sut.publishScoringEngine(
        new ScoringEngineData(testModelId, testArtifactId, "some-scoring-engine"));

    // then
    tapApiServerMock.verify();
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

  private void setTapApiServiceExpectedCalls() throws JsonProcessingException {
    tapApiServerMock.expect(requestTo(testTapApiServiceUrl + getOfferingsRequest))
        .andExpect(method(HttpMethod.GET))
        .andRespond(withSuccess(oneOfferingString("", "", ""), MediaType.APPLICATION_JSON));
    tapApiServerMock.expect(requestTo(testTapApiServiceUrl + createOfferingRequest))
        .andExpect(method(HttpMethod.POST))
        .andRespond(withSuccess(offeringCreated("some-offering-id"), MediaType.APPLICATION_JSON));
    tapApiServerMock
        .expect(
            requestTo(testTapApiServiceUrl + OfferingsFetcher.pathForOffering("some-offering-id")))
        .andExpect(method(HttpMethod.GET))
        .andRespond(withSuccess(offeringReady(), MediaType.APPLICATION_JSON));
    tapApiServerMock.expect(requestTo(testTapApiServiceUrl + createServiceInstanceRequest))
        .andExpect(method(HttpMethod.POST))
        .andRespond(withSuccess(offeringCreated("some-offering-id"), MediaType.APPLICATION_JSON));
  }
}
