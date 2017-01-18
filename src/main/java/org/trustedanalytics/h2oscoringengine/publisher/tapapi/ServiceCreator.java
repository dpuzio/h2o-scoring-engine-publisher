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
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

public class ServiceCreator {

  public static final String TAP_API_SERVICE_CREATE_SERVICE_INSTANCE_PATH = "/api/v3/services";

  private static final Logger LOGGER = LoggerFactory.getLogger(ServiceCreator.class);

  private final RestTemplate tapApiRestTemplate;
  private final String tapApiUrl;
  private final ObjectMapper jsonMapper;

  public ServiceCreator(RestTemplate tapApiRestTemplate, String tapApiUrl,
                        ObjectMapper jsonMapper) {
    this.tapApiRestTemplate = tapApiRestTemplate;
    this.tapApiUrl = tapApiUrl;
    this.jsonMapper = jsonMapper;
  }

  public void createServiceInstance(String name, String offeringId, String servicePlanId,
                                    String modelId, String artifactId) throws IOException {
    try {
      JsonNode rootNode = prepareCreateServiceRequest(
          name, offeringId, servicePlanId, modelId, artifactId);
      HttpEntity<String> request = new HttpEntity<>(rootNode.toString(), new HttpHeaders());
      ResponseEntity<String> responseEntity =
          tapApiRestTemplate.exchange(tapApiUrl + TAP_API_SERVICE_CREATE_SERVICE_INSTANCE_PATH, HttpMethod.POST,
              request, String.class);
      String responseBody = responseEntity.getBody();
      LOGGER.info("Service instance created successfully: {}", name);
      LOGGER.debug("Service creation response body: {}", responseBody);
    } catch (HttpStatusCodeException e) {
      throw new IOException(
          "Service instance creation failed. Tap-api-service responded with http status '"
              + e.getStatusCode() + " " + e.getStatusText() + "' and body: "
              + e.getResponseBodyAsString(),
          e);
    } catch (RestClientException e) {
      throw new IOException("Unable to call API sevice", e);
    }
  }

  private JsonNode prepareCreateServiceRequest(String name, String offeringId, String servicePlanId,
                                               String modelId, String artifactId) {
    ObjectNode rootNode = jsonMapper.createObjectNode();
    rootNode.put("name", name);
    rootNode.put("type", "SERVICE");
    rootNode.put("offeringId", offeringId);
    rootNode.putArray("bindings");  // empty array as we have no bindings

    ArrayNode metadata = rootNode.putArray("metadata");
    addKeyValueObjectToArray(metadata, "PLAN_ID", servicePlanId);
    addKeyValueObjectToArray(metadata, "MODEL_ID", modelId);
    addKeyValueObjectToArray(metadata, "ARTIFACT_ID", artifactId);

    return rootNode;
  }

  private void addKeyValueObjectToArray(ArrayNode arrayNode, String key, String value) {
    ObjectNode objNode = arrayNode.addObject();
    objNode.put("key", key);
    objNode.put("value", value);
  }
}
