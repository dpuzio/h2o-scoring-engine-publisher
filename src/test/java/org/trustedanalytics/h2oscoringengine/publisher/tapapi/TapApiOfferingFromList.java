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

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.Getter;

@Getter
public class TapApiOfferingFromList {
  private final String id;
  private final TapApiOfferingMetadata[] metadata;

  public TapApiOfferingFromList(String id, Map<String, String> metadataMap) {
    this.id = id;
    List<TapApiOfferingMetadata> metadataList = metadataMap.entrySet().stream()
        .map(entry -> new TapApiOfferingMetadata(entry.getKey(), entry.getValue()))
        .collect(Collectors.toList());
    this.metadata = new TapApiOfferingMetadata[metadataList.size()];
    metadataList.toArray(this.metadata);
  }
}
