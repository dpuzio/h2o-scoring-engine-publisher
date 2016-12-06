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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.UUID;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.ArgumentCaptor;
import org.trustedanalytics.h2oscoringengine.publisher.EnginePublishingException;
import org.trustedanalytics.h2oscoringengine.publisher.enginename.EngineNameSupplier;
import org.trustedanalytics.h2oscoringengine.publisher.restapi.ScoringEngineData;
import org.trustedanalytics.h2oscoringengine.publisher.tapapi.ServiceCreator;

public class OfferingInstanceCreationStepTest {

  private final String testModelName = "sample-name";
  private final String testOfferingId = "offering-1";
  private final String testPlanId = "plan-1";
  private final UUID testModelId = UUID.randomUUID();
  private final UUID testArtifactId = UUID.randomUUID();
  private final ScoringEngineData testScoringEngineData =
      new ScoringEngineData(testModelId, testArtifactId, testModelName);

  private final ServiceCreator serviceCreatorMock = mock(ServiceCreator.class);
  private final EngineNameSupplier engineNameSupplierMock = mock(EngineNameSupplier.class);
  private final OfferingInstanceCreationStep sut =
      new OfferingInstanceCreationStep(testOfferingId, testPlanId);

  @Rule
  public final ExpectedException thrown = ExpectedException.none();

  @Before
  public void setUp() throws IOException {
    when(engineNameSupplierMock.generateName(eq(testModelName), any())).thenReturn(testModelName);
  }

  @Test
  public void createOfferingInstance_TapApiCreatedServiceInstance_NoError() throws Exception {
    // given

    // when
    sut.createOfferingInstance(serviceCreatorMock, testScoringEngineData, engineNameSupplierMock);

    // then
    ArgumentCaptor<String> nameCaptor = ArgumentCaptor.forClass(String.class);
    ArgumentCaptor<String> offeringCaptor = ArgumentCaptor.forClass(String.class);
    ArgumentCaptor<String> planCaptor = ArgumentCaptor.forClass(String.class);
    ArgumentCaptor<String> modelCaptor = ArgumentCaptor.forClass(String.class);
    ArgumentCaptor<String> artifactCaptor = ArgumentCaptor.forClass(String.class);
    verify(serviceCreatorMock).createServiceInstance(nameCaptor.capture(), offeringCaptor.capture(),
        planCaptor.capture(), modelCaptor.capture(), artifactCaptor.capture());

    assertTrue(nameCaptor.getValue().contains(testModelName));
    assertEquals(testOfferingId, offeringCaptor.getValue());
    assertEquals(testPlanId, planCaptor.getValue());
    assertEquals(testModelId.toString(), modelCaptor.getValue());
    assertEquals(testArtifactId.toString(), artifactCaptor.getValue());
  }

  @Test
  public void createOfferingInstance_TapApiReturnedError_ExceptionThrown() throws Exception {
    // given

    doThrow(new IOException("Some error message from tap-api-service")).when(serviceCreatorMock)
        .createServiceInstance(any(), any(), any(), any(), any());

    // when
    // then
    thrown.expect(EnginePublishingException.class);
    sut.createOfferingInstance(serviceCreatorMock, testScoringEngineData, engineNameSupplierMock);
  }
}
