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

import lombok.Getter;

public class AssureOfferingPresenceStepConfig {

  private static final int OFFERING_CHECK_RETRY_COUNT = 24;
  private static final long OFFERING_CHECK_RETRY_DELAY_MS = 5000;
  private static final String OFFERING_CHECK_DESIRED_STATE = "READY";

  @Getter
  private final int retryCount;

  @Getter
  private final long retryIntervalMs;

  @Getter
  private final String desiredState;

  public AssureOfferingPresenceStepConfig(
      int retryCount, long retryIntervalMs, String desiredState) {
    this.retryCount = retryCount;
    this.retryIntervalMs = retryIntervalMs;
    this.desiredState = desiredState;
  }

  public static AssureOfferingPresenceStepConfig defaultConfig() {
    return new AssureOfferingPresenceStepConfig(
        OFFERING_CHECK_RETRY_COUNT,
        OFFERING_CHECK_RETRY_DELAY_MS,
        OFFERING_CHECK_DESIRED_STATE);
  }
}
