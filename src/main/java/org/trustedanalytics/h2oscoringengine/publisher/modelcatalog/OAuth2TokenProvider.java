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

import org.springframework.security.oauth2.client.OAuth2RestTemplate;
import org.springframework.security.oauth2.client.resource.OAuth2ProtectedResourceDetails;
import org.trustedanalytics.modelcatalog.rest.client.http.OAuthTokenProvider;

public class OAuth2TokenProvider implements OAuthTokenProvider {
  
  private final OAuth2RestTemplate oAuth2RestTemplate;
  
  public OAuth2TokenProvider(OAuth2ProtectedResourceDetails clientCredentials) {
    this.oAuth2RestTemplate = new OAuth2RestTemplate(clientCredentials);
  }

  @Override
  public String provideToken() {
    return oAuth2RestTemplate.getAccessToken().getValue();
  }

}
