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
package org.trustedanalytics.h2oscoringengine.publisher;

import com.jayway.restassured.RestAssured;
import java.io.IOException;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.IntegrationTest;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = Application.class)
@WebAppConfiguration
@IntegrationTest("server.port:0")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class SwaggerTest {
  
  @Value("${local.server.port}")
  private int port;

  @Value("localhost:${local.server.port}")
  private String swaggerHost;

  @Value("http://localhost:${local.server.port}/v2/api-docs")
  private String swaggerUrl;

  @Before
  public void setUp() {
    RestAssured.port = port;
  }

  @Test
  public void swagger_shouldGenerateJson() throws IOException {
    // Create HTTP client to read Swagger JSON from application
    CloseableHttpClient httpclient = HttpClients.createDefault();
    HttpGet httpGet = new HttpGet(swaggerUrl);
    CloseableHttpResponse response = httpclient.execute(httpGet);

    try {
      // Verify that there were no problem with getting JSON
      StatusLine statusLine = response.getStatusLine();
      Assert.assertEquals(HttpStatus.SC_OK, statusLine.getStatusCode());

      String swaggerJson = EntityUtils.toString(response.getEntity());
      ObjectMapper mapper = new ObjectMapper();
      JsonNode node = mapper.readTree(swaggerJson);
      Assert.assertEquals("2.0", node.get("swagger").getTextValue());
    } finally {
      response.close();
    }
  }
}
