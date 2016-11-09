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

import static org.junit.Assert.*;
import static org.hamcrest.Matchers.*;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;

import java.util.List;

import org.junit.Test;
import org.springframework.security.oauth2.client.OAuth2RestTemplate;

public class ApplicationConfigurationTest {
  
  @Test
  public void getUrlWithHttpProtocol_urlWithoutHttpGiven_setsProperUrlWithHttp() {
    // given
    ApplicationConfiguration sut = new ApplicationConfiguration();
    String testUrl = "some-url";
    String expectedUrl = "http://some-url";
    
    // when
    String url = sut.getUrlWithHttpProtocol(testUrl);
    
    // then
    assertEquals(expectedUrl, url);
  }
  
  @Test
  public void getUrlWithHttpProtocol_urlWithHttpGiven_TheSameUrlReturned() {
    // given
    ApplicationConfiguration sut = new ApplicationConfiguration();
    String testUrl = "http://some-url";
    String expectedUrl = testUrl;
    
    // when
    String url = sut.getUrlWithHttpProtocol(testUrl);
    
    // then
    assertEquals(expectedUrl, url);
  }
}
