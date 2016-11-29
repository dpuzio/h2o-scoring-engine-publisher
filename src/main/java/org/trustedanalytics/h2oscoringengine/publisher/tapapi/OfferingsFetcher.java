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

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

public class OfferingsFetcher {

  private static final Logger LOGGER = LoggerFactory.getLogger(OfferingsFetcher.class);

  public static final String TAP_API_SERVICE_OFFERINGS_PATH = "/api/v2/offerings";

  private final RestTemplate tapApiRestTemplate;
  private final String tapApiUrl;
  private final ObjectMapper jsonMapper;

  public OfferingsFetcher(
      RestTemplate tapApiRestTemplate, String tapApiUrl, ObjectMapper jsonMapper) {
    this.tapApiRestTemplate = tapApiRestTemplate;
    this.tapApiUrl = tapApiUrl;
    this.jsonMapper = jsonMapper;
  }

  public static String pathForOffering(String offeringId) {
    return TAP_API_SERVICE_OFFERINGS_PATH + "/" + offeringId;
  }

  public JsonNode fetchModelOffering(String offeringId) throws IOException {
    LOGGER.debug("Fetching offering from tap-api-service: " + offeringId);
    try {
      ResponseEntity<String> responseEntity =
          tapApiRestTemplate.exchange(
              tapApiUrl + pathForOffering(offeringId),
              HttpMethod.GET,
              new HttpEntity<>(new HttpHeaders()), String.class);
      JsonNode offeringJson = jsonMapper.readTree(responseEntity.getBody());
      if (!offeringJson.isObject()) {
        throw new IOException(
            "JSON fetched from tap-api-service is not an object: " + offeringJson);
      }

      return offeringJson;
    } catch (HttpStatusCodeException e) {
      LOGGER.error("tap-api-service responded with http status '" + e.getStatusCode() + " "
          + e.getStatusText() + "' and body: " + e.getResponseBodyAsString());
      throw e;
    }
  }

  public List<JsonNode> fetchModelOfferings(String modelId, String artifactId) throws IOException {
    JsonNode allOfferings = fetchAllOfferingsJsonArray();
    return pickModelOfferings(allOfferings, modelId, artifactId);
  }

  private JsonNode fetchAllOfferingsJsonArray() throws IOException {
    LOGGER.debug("Fetching list of offerings from tap-api-service.");
    try {
      ResponseEntity<String> responseEntity =
          tapApiRestTemplate.exchange(tapApiUrl + TAP_API_SERVICE_OFFERINGS_PATH, HttpMethod.GET,
              new HttpEntity<>(new HttpHeaders()), String.class);
      JsonNode allOfferingsJson = jsonMapper.readTree(responseEntity.getBody());
      if (allOfferingsJson.isArray()) {
        return allOfferingsJson;
      } else {
        throw new IOException(
            "JSON fetched from tap-api-service is not an array: " + allOfferingsJson);
      }
    } catch (HttpStatusCodeException e) {
      LOGGER.error("tap-api-service responded with http status '" + e.getStatusCode() + " "
          + e.getStatusText() + "' and body: " + e.getResponseBodyAsString());
      throw e;
    }
  }

  private List<JsonNode> pickModelOfferings(JsonNode offeringsListJson, String modelId,
      String artifactId) {
    return StreamSupport.stream(offeringsListJson.spliterator(), true)
        .filter(offering -> isModelOffering(offering, modelId, artifactId))
        .collect(Collectors.toList());
  }

  private boolean isModelOffering(JsonNode offeringJson, String modelId, String artifactId) {
    JsonNode modelIdNode = offeringJson.at("/metadata/MODEL_ID");
    JsonNode artifactIdNode = offeringJson.at("/metadata/ARTIFACT_ID");
    return modelIdNode.isTextual() && artifactIdNode.isTextual()
        && modelIdNode.textValue().equals(modelId) && artifactIdNode.textValue().equals(artifactId);
  }
}
