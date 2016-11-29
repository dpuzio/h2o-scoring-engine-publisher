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

import com.fasterxml.jackson.databind.JsonNode;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.List;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.trustedanalytics.h2oscoringengine.publisher.EnginePublishingException;
import org.trustedanalytics.h2oscoringengine.publisher.restapi.ScoringEngineData;
import org.trustedanalytics.h2oscoringengine.publisher.tapapi.OfferingCreationException;
import org.trustedanalytics.h2oscoringengine.publisher.tapapi.OfferingCreator;
import org.trustedanalytics.h2oscoringengine.publisher.tapapi.OfferingData;
import org.trustedanalytics.h2oscoringengine.publisher.tapapi.OfferingsFetcher;

public class AssureOfferingPresenceStep {

  public static final String EXAMPLE_SCORING_ENGINE_RESOURCE_PATH = "/example-scoring-engine.jar";

  private static final Logger LOGGER = LoggerFactory.getLogger(AssureOfferingPresenceStep.class);

  private final AssureOfferingPresenceStepConfig config;
  private final OfferingCreator offeringCreator;
  private final OfferingsFetcher offeringsFetcher;

  public AssureOfferingPresenceStep(
      OfferingsFetcher offeringsFetcher,
      OfferingCreator offeringCreator) {
    this(AssureOfferingPresenceStepConfig.defaultConfig(), offeringsFetcher, offeringCreator);
  }

  public AssureOfferingPresenceStep(
      AssureOfferingPresenceStepConfig config,
      OfferingsFetcher offeringsFetcher,
      OfferingCreator offeringCreator) {
    this.config = config;
    this.offeringsFetcher = offeringsFetcher;
    this.offeringCreator = offeringCreator;
  }

  public OfferingInstanceCreationStep ensureOfferingExists(ScoringEngineData scoringEngineData)
      throws EnginePublishingException {

    List<JsonNode> modelOfferings = fetchModelOfferings(scoringEngineData);
    OfferingData modelOffering;
    if (modelOfferings.size() == 0) {
      // TODO: get scoring engine bytes from model-catalog DPNG-12233
      byte[] scoringEngineJar = getExampleScoringEngine();
      try {
        modelOffering =
            offeringCreator.createJavaScoringEngineOffering(scoringEngineData, scoringEngineJar);
        waitUntilOfferingIsReady(modelOffering.getOfferingId());

        LOGGER.info("Created offering: " + modelOffering);
      } catch (OfferingCreationException e) {
        throw new EnginePublishingException("Unable to create scoring engine offering: ", e);
      }
    } else if (modelOfferings.size() == 1) {
      modelOffering = getOfferingDataFromJson(modelOfferings.get(0));
    } else {
      throw new EnginePublishingException("Unable to return model offering. Found "
          + modelOfferings.size() + "offerings for model " + scoringEngineData.getModelId());
    }

    LOGGER.debug("Model offering " + modelOffering);

    return new OfferingInstanceCreationStep(modelOffering.getOfferingId(), modelOffering.getPlanId());
  }

  private List<JsonNode> fetchModelOfferings(ScoringEngineData scoringEngineData)
      throws EnginePublishingException {
    try {
      return offeringsFetcher.fetchModelOfferings(scoringEngineData.getModelId(),
          scoringEngineData.getArtifactId());
    } catch (IOException e) {
      throw new EnginePublishingException("Unable to fetch model offerings from tap-api-service: ",
          e);
    }
  }

  private byte[] getExampleScoringEngine() throws EnginePublishingException {
    URL resource = this.getClass().getResource(EXAMPLE_SCORING_ENGINE_RESOURCE_PATH);
    if (null == resource) {
      throw new EnginePublishingException(
          "Scoring engine resource " + EXAMPLE_SCORING_ENGINE_RESOURCE_PATH + " not found");
    }

    InputStream inputStream =
        this.getClass().getResourceAsStream(EXAMPLE_SCORING_ENGINE_RESOURCE_PATH);
    try {
      return IOUtils.toByteArray(inputStream);
    } catch (IOException e) {
      throw new EnginePublishingException("Unable to read example scoring engine: ", e);
    } finally {
      try {
        inputStream.close();
      } catch (IOException e) {
        LOGGER.warn("Problem while closing input stream with example scoring engine: ", e);
      }
    }
  }

  private OfferingData getOfferingDataFromJson(JsonNode modelOffering) {
    String offeringId = modelOffering.at("/entity/unique_id").textValue();
    String planId = modelOffering.at("/entity/service_plans/0/metadata/guid").textValue();

    return new OfferingData(offeringId, planId);
  }

  private void waitUntilOfferingIsReady(String offeringId) throws OfferingCreationException {
    for (int i = 0; i < config.getRetryCount(); i++) {
      try {
        JsonNode offeringJson = offeringsFetcher.fetchModelOffering(offeringId);
        String offeringState = offeringJson.at("/entity/state").textValue();
        LOGGER.info("Offering {} state: {}", offeringId, offeringState);
        if (offeringState.equalsIgnoreCase(config.getDesiredState())) {
          LOGGER.info("Check OK!");
          return;
        }
        LOGGER.info("Check failed.");
        Thread.sleep(config.getRetryIntervalMs());
      } catch (Exception e) {
        LOGGER.error("Error when checking offering state, will retry in a while.", e);
      }
    }

    throw new OfferingCreationException("Problem with creating offering (not running)");
  }
}
