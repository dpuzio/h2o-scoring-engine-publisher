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

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.empty;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.trustedanalytics.h2oscoringengine.publisher.tapapi.OfferingsFetcher.TAP_API_SERVICE_OFFERINGS_PATH;
import static org.trustedanalytics.h2oscoringengine.publisher.tapapi.TestTapApiResponses.offeringReady;
import static org.trustedanalytics.h2oscoringengine.publisher.tapapi.TestTapApiResponses.oneOfferingJson;
import static org.trustedanalytics.h2oscoringengine.publisher.tapapi.TestTapApiResponses.oneOfferingString;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.List;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

public class OfferingsFetcherTest {

  private final String testModelId = "model-test-1";
  private final String testArtifactId = "artifact-test-1";
  private final String testOfferingId = "offering-test-1";
  private final String testTapApiUrl = "http://tap-api";
  private List<JsonNode> jsonWithOneOffering;

  private RestTemplate restTemplateMock = mock(RestTemplate.class);
  private ResponseEntity<String> responseMock = mock(ResponseEntity.class);
  private ObjectMapper jsonMapper = new ObjectMapper();

  @Rule
  public final ExpectedException thrown = ExpectedException.none();

  @Before
  public void setUp() throws IOException {
    jsonWithOneOffering = oneOfferingJson(testModelId, testArtifactId, "some-offering-id");

    when(restTemplateMock.exchange(eq(testTapApiUrl + TAP_API_SERVICE_OFFERINGS_PATH),
        eq(HttpMethod.GET), eq(new HttpEntity<>(new HttpHeaders())), eq(String.class)))
            .thenReturn(responseMock);
    when(restTemplateMock.exchange(
        eq(testTapApiUrl + OfferingsFetcher.pathForOffering(testOfferingId)), eq(HttpMethod.GET),
        eq(new HttpEntity<>(new HttpHeaders())), eq(String.class))).thenReturn(responseMock);
  }

  @Test
  public void fetchModelOfferings_oneOfferingFromTapApi_offeringNodeReturned() throws Exception {
    // given
    OfferingsFetcher sut = new OfferingsFetcher(restTemplateMock, testTapApiUrl, jsonMapper);
    when(responseMock.getBody())
        .thenReturn(oneOfferingString(testModelId, testArtifactId, "some-offering-id"));
    when(responseMock.getStatusCode()).thenReturn(HttpStatus.OK);

    // when
    List<JsonNode> actualJson = sut.fetchModelOfferings(testModelId, testArtifactId);

    // then
    assertEquals(jsonWithOneOffering, actualJson);
  }

  @Test
  public void fetchModelOfferings_InvalidJsonFromTapApi_ExceptionThrown() throws Exception {
    // given
    OfferingsFetcher sut = new OfferingsFetcher(restTemplateMock, testTapApiUrl, jsonMapper);
    when(responseMock.getBody()).thenReturn("{\"some-key\":\"some-value\"}");
    when(responseMock.getStatusCode()).thenReturn(HttpStatus.OK);

    // when
    // then
    thrown.expect(IOException.class);
    sut.fetchModelOfferings(testModelId, testArtifactId);
  }

  @Test
  public void fetchModelOfferings_offeringForDifferentModelFromTapApi_EmpytListReturned()
      throws Exception {
    // given
    OfferingsFetcher sut = new OfferingsFetcher(restTemplateMock, testTapApiUrl, jsonMapper);
    when(responseMock.getBody())
        .thenReturn(oneOfferingString("some-model-id", testArtifactId, testOfferingId));
    when(responseMock.getStatusCode()).thenReturn(HttpStatus.OK);

    // when
    List<JsonNode> actualOfferings = sut.fetchModelOfferings(testModelId, testArtifactId);

    // then
    assertThat(actualOfferings, empty());
  }

  @Test
  public void fetchModelOfferings_offeringForDifferentArtifactFromTapApi_EmpytListReturned()
      throws Exception {
    // given
    OfferingsFetcher sut = new OfferingsFetcher(restTemplateMock, testTapApiUrl, jsonMapper);
    when(responseMock.getBody())
        .thenReturn(oneOfferingString(testModelId, "some-artifact-id", testOfferingId));
    when(responseMock.getStatusCode()).thenReturn(HttpStatus.OK);

    // when
    List<JsonNode> actualOfferings = sut.fetchModelOfferings(testModelId, testArtifactId);

    // then
    assertThat(actualOfferings, empty());
  }

  @Test
  public void fetchModelOfferings_tapApiRespondedWithError_ResponseBodyInExceptionMessage()
      throws Exception {
    // given
    OfferingsFetcher sut = new OfferingsFetcher(restTemplateMock, testTapApiUrl, jsonMapper);
    String testErrorMsg = "Some error message from tap-api-service";
    when(responseMock.getBody()).thenReturn(testErrorMsg);
    when(responseMock.getStatusCode()).thenReturn(HttpStatus.INTERNAL_SERVER_ERROR);

    // when
    // then
    thrown.expect(IOException.class);
    thrown.expectMessage(containsString(testErrorMsg));
    sut.fetchModelOfferings(testModelId, testArtifactId);
  }

  @Test
  public void fetchModelOfferings_tapApiReturnedInvalidModelNode_EmptyListReturned()
      throws Exception {
    // given
    OfferingsFetcher sut = new OfferingsFetcher(restTemplateMock, testTapApiUrl, jsonMapper);
    when(responseMock.getBody()).thenReturn(prepareJsonWithoutModelNode());
    when(responseMock.getStatusCode()).thenReturn(HttpStatus.OK);

    // when
    List<JsonNode> actualOfferings = sut.fetchModelOfferings(testModelId, testArtifactId);

    // then
    assertThat(actualOfferings, empty());
  }

  @Test
  public void fetchModelOfferings_tapApiReturnedInvalidArtifactNode_EmptyListReturned()
      throws Exception {
    // given
    OfferingsFetcher sut = new OfferingsFetcher(restTemplateMock, testTapApiUrl, jsonMapper);
    when(responseMock.getBody()).thenReturn(prepareJsonWithoutArtifactNode());
    when(responseMock.getStatusCode()).thenReturn(HttpStatus.OK);

    // when
    List<JsonNode> actualOfferings = sut.fetchModelOfferings(testModelId, testArtifactId);

    // then
    assertThat(actualOfferings, empty());
  }

  @Test
  public void fetchModelOfferings_tapApiReturnedInvalidMetadataNode_EmptyListReturned()
      throws Exception {
    // given
    OfferingsFetcher sut = new OfferingsFetcher(restTemplateMock, testTapApiUrl, jsonMapper);
    when(responseMock.getBody()).thenReturn(prepareJsonWithInvalidMetadataNodes());
    when(responseMock.getStatusCode()).thenReturn(HttpStatus.OK);

    // when
    List<JsonNode> actualOfferings = sut.fetchModelOfferings(testModelId, testArtifactId);

    // then
    assertThat(actualOfferings, empty());
  }

  @Test
  public void fetchModelOfferings_tapApiReturnedMetadataNodeThatIsNotArray_EmptyListReturned()
      throws Exception {
    // given
    OfferingsFetcher sut = new OfferingsFetcher(restTemplateMock, testTapApiUrl, jsonMapper);
    when(responseMock.getBody()).thenReturn(prepareJsonWithMetadataNodeNotArray());
    when(responseMock.getStatusCode()).thenReturn(HttpStatus.OK);

    // when
    List<JsonNode> actualOfferings = sut.fetchModelOfferings(testModelId, testArtifactId);

    // then
    assertThat(actualOfferings, empty());
  }

  @Test
  public void fetchModelOffering_oneOfferingFromTapApi_offeringNodeReturned() throws Exception {
    // given
    OfferingsFetcher sut = new OfferingsFetcher(restTemplateMock, testTapApiUrl, jsonMapper);
    when(responseMock.getBody()).thenReturn(offeringReady());
    when(responseMock.getStatusCode()).thenReturn(HttpStatus.OK);

    // when
    JsonNode actualJson = sut.fetchModelOffering(testOfferingId);

    // then
    assertEquals("READY", actualJson.at("/state").textValue());
  }

  @Test
  public void fetchModelOffering_InvalidJsonFromTapApi_ExceptionThrown() throws Exception {
    // given
    OfferingsFetcher sut = new OfferingsFetcher(restTemplateMock, testTapApiUrl, jsonMapper);
    when(responseMock.getBody()).thenReturn("{[\"bad_json_structure\"");
    when(responseMock.getStatusCode()).thenReturn(HttpStatus.OK);

    // when
    // then
    thrown.expect(IOException.class);
    sut.fetchModelOffering(testOfferingId);
  }

  @Test
  public void fetchModelOffering_tapApiRespondedWithError_ResponseBodyInExceptionMessage()
      throws Exception {
    // given
    OfferingsFetcher sut = new OfferingsFetcher(restTemplateMock, testTapApiUrl, jsonMapper);
    String testErrorMsg = "Some error message from tap-api-service";
    when(responseMock.getBody()).thenReturn(testErrorMsg);
    when(responseMock.getStatusCode()).thenReturn(HttpStatus.INTERNAL_SERVER_ERROR);

    // when
    // then
    thrown.expect(IOException.class);
    thrown.expectMessage(containsString(testErrorMsg));
    sut.fetchModelOffering(testOfferingId);
  }

  private String prepareJsonWithoutArtifactNode() {
    return "[{\"metadata\": [{\"key\":\"MODEL_ID\",\"value\":\"" + testModelId + "\"}]}]";
  }

  private String prepareJsonWithoutModelNode() {
    return "[{\"metadata\": [{\"key\":\"ARTIFACT_ID\",\"value\":\"" + testArtifactId + "\"}]}]";
  }

  private String prepareJsonWithInvalidMetadataNodes() {
    return "[{\"metadata\":[{\"MODEL_ID\":[1,2], \"ARTIFACT_ID\":[1,2]}]}]";
  }

  private String prepareJsonWithMetadataNodeNotArray() {
    return "[{\"metadata\":{\"MODEL_ID\":\"" + testModelId + "\", \"ARTIFACT_ID\":\""
        + testArtifactId + "\"}}]";
  }
}
