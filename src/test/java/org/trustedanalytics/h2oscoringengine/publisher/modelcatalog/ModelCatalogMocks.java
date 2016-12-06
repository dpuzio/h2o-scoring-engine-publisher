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
package org.trustedanalytics.h2oscoringengine.publisher.modelcatalog;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.UUID;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpRequestBase;
import org.trustedanalytics.modelcatalog.rest.client.ModelCatalogReaderClient;
import org.trustedanalytics.modelcatalog.rest.client.http.HttpFileResource;

public class ModelCatalogMocks {

  public static ModelCatalogReaderClient mockThatReturnsArtifact(UUID modelId, UUID artifactId) {
    ModelCatalogReaderClient modelCatalogClientMock = mock(ModelCatalogReaderClient.class);
    HttpEntity entityMock = mock(HttpEntity.class);
    CloseableHttpResponse responseMock = mock(CloseableHttpResponse.class);
    try {
      when(entityMock.getContent())
          .thenReturn(IOUtils.toInputStream("some-bytes", Charset.defaultCharset()));
    } catch (IllegalStateException | IOException e) {
      // It's a mock - it will not throw an exception;
      e.printStackTrace();
    }
    when(responseMock.getEntity()).thenReturn(entityMock);
    HttpFileResource modelCatalogAnswer =
        new HttpFileResource(mock(HttpRequestBase.class), responseMock, "some-name");

    when(modelCatalogClientMock.retrieveArtifactFile(modelId, artifactId))
        .thenReturn(modelCatalogAnswer);
    return modelCatalogClientMock;
  }

  public static ModelCatalogReaderClient mockThatReturnsFileWithError(UUID modelId,
      UUID artifactId) throws IllegalStateException, IOException {
    ModelCatalogReaderClient modelCatalogClientMock = mock(ModelCatalogReaderClient.class);
    HttpEntity entityMock = mock(HttpEntity.class);
    CloseableHttpResponse responseMock = mock(CloseableHttpResponse.class);
    when(entityMock.getContent()).thenThrow(new IOException("Problem when reading artifact file"));
    when(responseMock.getEntity()).thenReturn(entityMock);
    HttpFileResource modelCatalogAnswer =
        new HttpFileResource(mock(HttpRequestBase.class), responseMock, "some-name");
    when(modelCatalogClientMock.retrieveArtifactFile(modelId, artifactId))
        .thenReturn(modelCatalogAnswer);
    return modelCatalogClientMock;
  }
}
