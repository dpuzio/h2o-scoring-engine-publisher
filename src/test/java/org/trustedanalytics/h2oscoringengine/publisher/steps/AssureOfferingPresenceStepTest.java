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

import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;
import static org.trustedanalytics.h2oscoringengine.publisher.tapapi.TestTapApiResponses.oneOfferingJson;
import static org.trustedanalytics.h2oscoringengine.publisher.tapapi.TestTapApiResponses.twoOfferingsJson;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.io.IOException;
import java.util.Arrays;
import java.util.UUID;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.trustedanalytics.h2oscoringengine.publisher.EnginePublishingException;
import org.trustedanalytics.h2oscoringengine.publisher.modelcatalog.ModelCatalogMocks;
import org.trustedanalytics.h2oscoringengine.publisher.restapi.ScoringEngineData;
import org.trustedanalytics.h2oscoringengine.publisher.tapapi.OfferingCreationException;
import org.trustedanalytics.h2oscoringengine.publisher.tapapi.OfferingCreator;
import org.trustedanalytics.h2oscoringengine.publisher.tapapi.OfferingData;
import org.trustedanalytics.h2oscoringengine.publisher.tapapi.OfferingsFetcher;
import org.trustedanalytics.modelcatalog.rest.client.ModelCatalogReaderClient;

public class AssureOfferingPresenceStepTest {

  private final OfferingsFetcher offeringsFetcherMock = mock(OfferingsFetcher.class);
  private final OfferingCreator offeringCreatorMock = mock(OfferingCreator.class);
  private final AssureOfferingPresenceStepConfig config =
      new AssureOfferingPresenceStepConfig(1, 0, "READY");
  private final UUID testModelId = UUID.randomUUID();
  private final UUID testArtifactId = UUID.randomUUID();
  private final ModelCatalogReaderClient modelCatalogClientMock =
      ModelCatalogMocks.mockThatReturnsArtifact(testModelId, testArtifactId);
  private final AssureOfferingPresenceStep sut = new AssureOfferingPresenceStep(config,
      offeringsFetcherMock, offeringCreatorMock, modelCatalogClientMock);

  private final String testModelName = "some-model-name";
  private final ScoringEngineData testScoringEngineData =
      new ScoringEngineData(testModelId, testArtifactId, testModelName);
  private final String testOfferingId = "some-offering-id";
  private final OfferingData testOffering = new OfferingData(testOfferingId, "test-plan-id");

  @Rule
  public final ExpectedException thrown = ExpectedException.none();

  @Test
  public void ensureOfferingExists_OfferingsFetcherReturnedOneOffering_NextStepReturned()
      throws Exception {
    // given
    when(
        offeringsFetcherMock.fetchModelOfferings(testModelId.toString(), testArtifactId.toString()))
            .thenReturn(
                oneOfferingJson(testModelId.toString(), testArtifactId.toString(), testOfferingId));

    // when
    OfferingInstanceCreationStep nextStep = sut.ensureOfferingExists(testScoringEngineData);

    // then
    assertNotNull(nextStep);
    verifyZeroInteractions(offeringCreatorMock);
  }

  @Test
  public void ensureOfferingExist_OfferingsFetcherReturnedNoOfferings_OfferingCreatedAndNextStepReturned()
      throws Exception {
    // given
    when(offeringsFetcherMock.fetchModelOfferings(eq(testModelId.toString()),
        eq(testArtifactId.toString()))).thenReturn(Arrays.asList());
    when(offeringCreatorMock.createJavaScoringEngineOffering(any(), any()))
        .thenReturn(testOffering);
    ObjectMapper mapper = new ObjectMapper();
    ObjectNode offeringInfo = mapper.createObjectNode();
    offeringInfo.put("state", "READY");
    when(offeringsFetcherMock.fetchModelOffering(eq(testOfferingId))).thenReturn(offeringInfo);

    // when
    OfferingInstanceCreationStep nextStep = sut.ensureOfferingExists(testScoringEngineData);

    // then
    assertNotNull(nextStep);
    verify(offeringCreatorMock).createJavaScoringEngineOffering(any(), any());
  }

  @Test
  public void ensureOfferingExists_OfferingsFetcherReturnedMoreThanOne_ExceptionThrown()
      throws Exception {
    // given
    when(offeringsFetcherMock.fetchModelOfferings(eq(testModelId.toString()),
        eq(testArtifactId.toString()))).thenReturn(
            twoOfferingsJson(testModelId.toString(), testArtifactId.toString(), testOfferingId));

    // when
    // then
    thrown.expect(EnginePublishingException.class);
    thrown.expectMessage(containsString("Unable to return model offering."));
    sut.ensureOfferingExists(testScoringEngineData);
  }

  @Test
  public void ensureOfferingExists_OfferingsFetcherThrewException_ExceptionThrown()
      throws Exception {
    // given
    when(offeringsFetcherMock.fetchModelOfferings(eq(testModelId.toString()),
        eq(testArtifactId.toString()))).thenThrow(new IOException());

    // when
    // then
    thrown.expect(EnginePublishingException.class);
    thrown.expectMessage(containsString("Unable to fetch model offerings from tap-api-service"));
    sut.ensureOfferingExists(testScoringEngineData);
  }

  @Test
  public void ensureOfferingExists_OfferingCreatorThrewException_ExceptionThrown()
      throws Exception {
    // given
    when(offeringsFetcherMock.fetchModelOfferings(eq(testModelId.toString()),
        eq(testArtifactId.toString()))).thenReturn(Arrays.asList());
    when(offeringCreatorMock.createJavaScoringEngineOffering(any(), any()))
        .thenThrow(new OfferingCreationException("", new IOException()));

    // when
    // then
    thrown.expect(EnginePublishingException.class);
    thrown.expectMessage(containsString("Unable to create scoring engine offering"));
    sut.ensureOfferingExists(testScoringEngineData);
  }

  @Test
  public void ensureOfferingExists_OfferingCreatedButNotReady_ExceptionThrown() throws Exception {
    // given
    when(offeringsFetcherMock.fetchModelOfferings(eq(testModelId.toString()),
        eq(testArtifactId.toString()))).thenReturn(Arrays.asList());
    when(offeringCreatorMock.createJavaScoringEngineOffering(any(), any()))
        .thenReturn(testOffering);
    ObjectMapper mapper = new ObjectMapper();
    ObjectNode offeringInfo = mapper.createObjectNode();
    offeringInfo.putObject("entity").put("state", "DEPLOYING");
    when(offeringsFetcherMock.fetchModelOffering(eq(testOfferingId))).thenReturn(offeringInfo);

    // when
    // then
    thrown.expect(EnginePublishingException.class);
    thrown.expectMessage(containsString("Unable to create scoring engine offering"));
    sut.ensureOfferingExists(testScoringEngineData);
  }

  @Test
  public void ensureOfferingExists_ErrorWhenReadingFileFromModelCatalog_ExceptionThrown()
      throws Exception {
    // given
    ModelCatalogReaderClient modelCatalogMock =
        ModelCatalogMocks.mockThatReturnsFileWithError(testModelId, testArtifactId);
    AssureOfferingPresenceStep sut =
        new AssureOfferingPresenceStep(offeringsFetcherMock, offeringCreatorMock, modelCatalogMock);

    // when
    // then
    thrown.expect(EnginePublishingException.class);
    sut.ensureOfferingExists(testScoringEngineData);
  }
}
