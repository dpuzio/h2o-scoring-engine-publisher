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

import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.trustedanalytics.h2oscoringengine.publisher.EnginePublishingException;
import org.trustedanalytics.h2oscoringengine.publisher.enginename.EngineNameSupplier;
import org.trustedanalytics.h2oscoringengine.publisher.restapi.ScoringEngineData;
import org.trustedanalytics.h2oscoringengine.publisher.tapapi.ServiceCreator;

public class OfferingInstanceCreationStep {

  private static final Logger LOGGER = LoggerFactory.getLogger(OfferingInstanceCreationStep.class);

  private final String offeringId;
  private final String planId;

  public OfferingInstanceCreationStep(String offeringId, String planId) {
    this.offeringId = offeringId;
    this.planId = planId;
  }

  public void createOfferingInstance(ServiceCreator serviceCreator,
      ScoringEngineData scoringEngineData, EngineNameSupplier nameSupplier)
      throws EnginePublishingException {
    LOGGER.info("Creating offering instance: offeringId={}, planId={}, modelId={}, artifactId={}",
        offeringId, planId, scoringEngineData.getModelId(), scoringEngineData.getArtifactId());
    try {
      serviceCreator.createServiceInstance(
          nameSupplier.generateName(scoringEngineData.getModelName(),
              scoringEngineData.getModelId().toString()),
          offeringId, planId, scoringEngineData.getModelId().toString(),
          scoringEngineData.getArtifactId().toString());
    } catch (IOException e) {
      throw new EnginePublishingException("Unable to create service instance", e);
    }
  }
}
