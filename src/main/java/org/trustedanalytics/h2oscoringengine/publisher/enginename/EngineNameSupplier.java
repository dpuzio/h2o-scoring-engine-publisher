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

import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpStatusCodeException;
import org.trustedanalytics.h2oscoringengine.publisher.http.HttpCommunication;
import org.trustedanalytics.h2oscoringengine.publisher.tapapi.TapApiNameFormatter;

public class EngineNameSupplier {

  private static final Logger LOGGER = LoggerFactory.getLogger(EngineNameSupplier.class);

  public static final String KEY_COUNTER_ENDPOINT = "/api/v1/counter/";

  private final KeyCounterConnectionData keyCounter;

  public EngineNameSupplier(KeyCounterConnectionData keyCounter) {
    this.keyCounter = keyCounter;
  }

  public String generateName(String modelName, String modelId) throws IOException {
    // Model name is not unique so using part of modelId to make collision less likely while
    // keeping human-readability.
    String modelNameAdjusted = TapApiNameFormatter.format(modelName);
    String randomPart = modelId.substring(0, 4);

    return (modelNameAdjusted + "-" + generateSuffix(modelNameAdjusted + randomPart) + "-"
        + randomPart).toLowerCase();
  }

  private String generateSuffix(String modelIdentifier) throws IOException {
    LOGGER.info("Fetching suffix for scoring engine name from key-counter");

    // padding with zeros to length=3 to meet business requirements (se00x suffix).
    return String.format("se%03d", fetchCount(modelIdentifier));
  }

  private Integer fetchCount(String key) throws IOException {
    String url = keyCounter.getCredentials().getUrl() + KEY_COUNTER_ENDPOINT + key;
    HttpHeaders headers =
        HttpCommunication.basicAuthJsonHeaders(keyCounter.getCredentials().getBasicAuthToken());
    HttpEntity<String> request = new HttpEntity<String>(headers);

    try {
      ResponseEntity<Integer> response =
          keyCounter.getRestTemplate().exchange(url, HttpMethod.POST, request, Integer.class);
      return response.getBody();
    } catch (HttpStatusCodeException e) {
      throw new IOException(
          "Fetching counter failed. Key-counter responded with http status '" + e.getStatusCode()
              + " " + e.getStatusText() + "' and body: " + e.getResponseBodyAsString(),
          e);
    }
  }
}
