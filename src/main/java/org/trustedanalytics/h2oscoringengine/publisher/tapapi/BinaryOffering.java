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

import lombok.Getter;

@Getter
public class BinaryOffering {

  private final String name;
  private final String description;
  private final OfferingMetadata[] metadata;
  private final boolean bindable;
  private final String[] tags;
  private final OfferingPlan[] plans;

  public BinaryOffering(String offeringName, OfferingMetadata[] metadata, String modelName) {
    this.name = TapApiNameFormatter.format(offeringName);
    this.description = "Offering of h2o scoring engine based on model " + modelName;
    this.metadata = metadata;
    this.bindable = false;
    this.tags = new String[] {"k8s"};
    this.plans = new OfferingPlan[] {new OfferingPlan()};
  }
}
