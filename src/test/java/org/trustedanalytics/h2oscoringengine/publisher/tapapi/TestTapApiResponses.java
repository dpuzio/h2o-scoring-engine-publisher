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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class TestTapApiResponses {

  public static String oneOfferingString(String modelId, String artifactId, String offeringId)
      throws JsonProcessingException {
    Map<String, String> metadata = new HashMap<>();
    metadata.put("MODEL_ID", modelId);
    metadata.put("ARTIFACT_ID", artifactId);
    metadata.put("guid", "d3bbb208-6817-6817-6817-aaaaaaaaaaaa");
    TapApiOfferingFromList offering = new TapApiOfferingFromList(offeringId, metadata);

    List<TapApiOfferingFromList> offeringsList = Arrays.asList(offering);
    ObjectMapper mapper = new ObjectMapper();

    return mapper.writeValueAsString(offeringsList);
  }

  public static List<JsonNode> oneOfferingJson(String modelId, String artifactId, String offeringId)
      throws IOException {
    ObjectMapper mapper = new ObjectMapper();
    JsonNode jsonArrayNode = mapper.readTree(oneOfferingString(modelId, artifactId, offeringId));
        
    return StreamSupport.stream(jsonArrayNode.spliterator(), true).collect(Collectors.toList());
  }

  public static List<JsonNode> twoOfferingsJson(String modelId, String artifactId,
      String offeringId) throws IOException {
    List<JsonNode> json = oneOfferingJson(modelId, artifactId, offeringId);
    json.addAll(oneOfferingJson(modelId, artifactId, "some-other-offering-id"));
    return json;
  }

  public static String offeringCreated(String offeringId) {
    return "{\"id\":\"" + offeringId + "\"}";
  }

  public static String offeringReady() {
    return "{\"state\":\"READY\"}";
  }
}
