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
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.FormHttpMessageConverter;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.ResourceHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;
import org.trustedanalytics.h2oscoringengine.publisher.restapi.ScoringEngineData;

public class OfferingCreator {

  public static final String TAP_API_SERVICE_CREATE_OFFERING_PATH = "/api/v2/offerings/binary";

  private static final Logger LOGGER = LoggerFactory.getLogger(OfferingCreator.class);

  private final RestTemplate tapApiRestTemplate;
  private final String tapApiUrl;
  private final ObjectMapper jsonMapper;

  public OfferingCreator(RestTemplate tapApiRestTemplate, String tapApiUrl,
      ObjectMapper jsonMapper) {
    this.tapApiRestTemplate = tapApiRestTemplate;
    this.tapApiUrl = tapApiUrl;
    this.jsonMapper = jsonMapper;
  }

  public OfferingData createJavaScoringEngineOffering(ScoringEngineData scoringEngineData,
      InputStream scoringEngineJar) throws OfferingCreationException {
    HttpEntity<MultiValueMap<String, Object>> request;
    try {
      request = prepareMultiPartRequest(scoringEngineData, scoringEngineJar);
      prepareRestTemplateForInpuStreamResource(tapApiRestTemplate);
      ResponseEntity<String> response = tapApiRestTemplate.exchange(
          tapApiUrl + TAP_API_SERVICE_CREATE_OFFERING_PATH, HttpMethod.POST, request, String.class);
      return fetchOfferingId(response.getBody());
    } catch (HttpStatusCodeException e) {
      throw new OfferingCreationException(
          "Offering creation failed. Tap-api-service responded with http status '"
              + e.getStatusCode() + " " + e.getStatusText() + "' and body: "
              + e.getResponseBodyAsString(),
          e);
    } catch (IOException e) {
      throw new OfferingCreationException("Offering creation failed: ", e);
    } finally {
      try {
        scoringEngineJar.close();
      } catch (IOException e) {
        LOGGER.warn("Problem while closing input stream with scoring engine: ", e);
      }
    }
  }

  private void prepareRestTemplateForInpuStreamResource(RestTemplate tapApiRestTemplate) {
    List<HttpMessageConverter<?>> converters =
        new ArrayList<>(Arrays.asList(new ResourceHttpMessageConverter(),
            new FormHttpMessageConverter(), new MappingJackson2HttpMessageConverter()));
    tapApiRestTemplate.getMessageConverters().addAll(converters);
  }

  private HttpEntity<MultiValueMap<String, Object>> prepareMultiPartRequest(
      ScoringEngineData scoringEngineData, InputStream blobBytes) throws JsonProcessingException {
    MultiValueMap<String, Object> multiPartRequest = new LinkedMultiValueMap<>();
    multiPartRequest.add("blob",
        prepareBlobRequestPart(blobBytes, scoringEngineData.getModelName() + ".jar"));
    multiPartRequest.add("manifest", prepareManifestRequestPart());
    multiPartRequest.add("offering",
        prepareOfferingRequestPart(scoringEngineData.getModelId().toString(),
            scoringEngineData.getArtifactId().toString(), scoringEngineData.getModelName()));

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.MULTIPART_FORM_DATA);

    return new HttpEntity<>(multiPartRequest, headers);
  }

  private HttpEntity<Resource> prepareOfferingRequestPart(String modelId, String artifactId,
      String modelName) throws JsonProcessingException {
    OfferingMetadata[] metadata = {new OfferingMetadata("MODEL_ID", modelId),
        new OfferingMetadata("ARTIFACT_ID", artifactId)};
    BinaryOffering binaryOffering =
        new BinaryOffering("h2o-" + modelName + "-" + modelId, metadata, modelName);

    byte[] offeringJsonBytes = jsonMapper.writeValueAsBytes(binaryOffering);

    LOGGER.info("Adding bytes to request: " + new String(offeringJsonBytes));
    return new HttpEntity<>(prepareResourceWithFilename(offeringJsonBytes, "binary_offering.json"));
  }


  private HttpEntity<Resource> prepareManifestRequestPart() {
    String binaryManifestJson = "{\"type\": \"JAVA\"}";
    byte[] bytesToBeSent = binaryManifestJson.getBytes();

    LOGGER.info("Adding bytes to request: " + new String(bytesToBeSent));

    return new HttpEntity<>(prepareResourceWithFilename(bytesToBeSent, "binary_manifest.json"));
  }

  private HttpEntity<Resource> prepareBlobRequestPart(InputStream blobBytes, String fileName) {
    LOGGER.info("Adding blob with scoring engine to request.");
    return new HttpEntity<>(prepareResourceWithFilename(blobBytes, fileName));
  }

  private ByteArrayResource prepareResourceWithFilename(byte[] byteArray, String filename) {
    return new ByteArrayResource(byteArray) {
      @Override
      public String getFilename() {
        return filename;
      }
    };
  }

  private Resource prepareResourceWithFilename(InputStream bytes, String filename) {
    return new InputStreamResource(bytes) {
      @Override
      public String getFilename() {
        return filename;
      }

      // dealing with Spring's ResourceHttpMessageConverter bug:
      // https://jira.spring.io/browse/SPR-13571
      @Override
      public long contentLength() throws IOException {
        return -1;
      }
    };
  }

  private OfferingData fetchOfferingId(String jsonBody) throws IOException {
    ObjectMapper mapper = new ObjectMapper();
    JsonNode offering = mapper.readTree(jsonBody);
    String offeringId = offering.at("/id").textValue();
    String planId = offering.at("/plans/0/id").textValue();

    return new OfferingData(offeringId, planId);
  }
}
