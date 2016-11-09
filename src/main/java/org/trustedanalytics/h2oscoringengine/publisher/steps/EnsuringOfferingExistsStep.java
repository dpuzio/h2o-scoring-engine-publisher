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
package org.trustedanalytics.h2oscoringengine.publisher.steps;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import org.trustedanalytics.h2oscoringengine.publisher.EnginePublishingException;

public class EnsuringOfferingExistsStep {

  private static final Logger LOGGER = LoggerFactory.getLogger(EnsuringOfferingExistsStep.class);
  public static final String TAP_API_SERVICE_OFFERINGS_PATH = "/api/v2/offerings";
  private RestTemplate tapApiServiceRestTemplate;
  private String tapApiServiceUrl;

  public EnsuringOfferingExistsStep(RestTemplate tapApiServiceRestTemplate,
      String tapApiServiceUrl) {
    this.tapApiServiceRestTemplate = tapApiServiceRestTemplate;
    this.tapApiServiceUrl = tapApiServiceUrl;
  }

  public void ensureOfferingExists(String modelId, String artifactId)
      throws EnginePublishingException {
    /* TODO:
     * 1. call tap-api-service for all offerings (GET /api/v2/offerings)
     * 2. find offerings with MODEL_ID = modelId and ARTIFACT_ID=artifactId in metadata
     * 3. if nothing found - create new offering 
     */
    String offeringsListJson = fetchOfferingsListJson();
    LOGGER.info("Got response from tap-api-service: " + offeringsListJson);

  }
  
  private String fetchOfferingsListJson() {
    ResponseEntity<String> responseEntity =
        tapApiServiceRestTemplate.exchange(tapApiServiceUrl + TAP_API_SERVICE_OFFERINGS_PATH,
            HttpMethod.GET, new HttpEntity<>(new HttpHeaders()), String.class);
    
    return responseEntity.getBody();
  }

}
