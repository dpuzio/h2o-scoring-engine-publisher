/**
 * Copyright (c) 2015 Intel Corporation
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

import javax.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.client.DefaultOAuth2ClientContext;
import org.springframework.security.oauth2.client.OAuth2RestTemplate;
import org.springframework.security.oauth2.client.resource.OAuth2ProtectedResourceDetails;
import org.springframework.security.oauth2.client.token.DefaultAccessTokenRequest;
import org.springframework.security.oauth2.client.token.grant.client.ClientCredentialsAccessTokenProvider;
import org.springframework.security.oauth2.client.token.grant.client.ClientCredentialsResourceDetails;
import org.springframework.web.client.RestTemplate;
import org.trustedanalytics.h2oscoringengine.publisher.enginename.EngineNameSupplier;
import org.trustedanalytics.h2oscoringengine.publisher.enginename.KeyCounterConnectionData;
import org.trustedanalytics.h2oscoringengine.publisher.http.BasicAuthServerCredentials;
import org.trustedanalytics.h2oscoringengine.publisher.modelcatalog.OAuth2TokenProvider;
import org.trustedanalytics.h2oscoringengine.publisher.restapi.validation.DownloadRequestValidationRules;
import org.trustedanalytics.modelcatalog.rest.client.ModelCatalogClientBuilder;
import org.trustedanalytics.modelcatalog.rest.client.ModelCatalogReaderClient;

@Configuration
public class ApplicationConfiguration {

  @Bean
  public Publisher publisher(
      @NotNull @Value("${publisher.engineBaseJar.resourcePath}") String engineBaseJarPath,
      OAuth2RestTemplate tapApiServiceRestTemplate,
      @NotNull @Value("${tapApiService.url}") String tapApiServiceUrl,
      ModelCatalogReaderClient modelCatalogClient, KeyCounterConnectionData keyCounter) {

    return new Publisher(new RestTemplate(), tapApiServiceRestTemplate,
        getUrlWithHttpProtocol(tapApiServiceUrl), engineBaseJarPath, modelCatalogClient,
        new EngineNameSupplier(keyCounter));
  }

  @Bean
  public DownloadRequestValidationRules downloadRequestValidationRules() {
    return new DownloadRequestValidationRules();
  }

  @Bean
  public OAuth2RestTemplate oAuth2RestTemplate(OAuth2ProtectedResourceDetails clientCredentials) {
    OAuth2RestTemplate restTemplate = new OAuth2RestTemplate(clientCredentials,
        new DefaultOAuth2ClientContext(new DefaultAccessTokenRequest()));
    ClientCredentialsAccessTokenProvider tokenProvider = new ClientCredentialsAccessTokenProvider();
    restTemplate.setAccessTokenProvider(tokenProvider);

    return restTemplate;
  }

  @Bean
  @ConfigurationProperties("tapApiService.oauth")
  public OAuth2ProtectedResourceDetails clientCredentials() {
    return new ClientCredentialsResourceDetails();
  }

  @Bean
  public ModelCatalogReaderClient modelCatalogClient(
      OAuth2ProtectedResourceDetails modelCatalogCredentials,
      @NotNull @Value("${modelCatalog.url}") String modelCatalogUrl) {
    ModelCatalogClientBuilder builder =
        new ModelCatalogClientBuilder(getUrlWithHttpProtocol(modelCatalogUrl));
    return builder.oAuthTokenProvider(new OAuth2TokenProvider(modelCatalogCredentials))
        .buildReader();
  }

  @Bean
  @ConfigurationProperties("modelCatalog.oauth")
  public OAuth2ProtectedResourceDetails modelCatalogCredentials() {
    return new ClientCredentialsResourceDetails();
  }

  @Bean
  public KeyCounterConnectionData keyCounter(BasicAuthServerCredentials keyCounterCredentials) {
    return new KeyCounterConnectionData(new RestTemplate(), keyCounterCredentials);
  }

  @Bean
  public BasicAuthServerCredentials keyCounterCredentials(
      @NotNull @Value("${keyCounter.host}") String keyCounterUrl,
      @NotNull @Value("${keyCounter.username}") String keyCounterUser,
      @NotNull @Value("${keyCounter.password}") String keyCounterPassword) {
    return new BasicAuthServerCredentials(getUrlWithHttpProtocol(keyCounterUrl), keyCounterUser,
        keyCounterPassword);
  }

  String getUrlWithHttpProtocol(String url) {
    return url.toLowerCase().matches("^http.?:.*$") ? url : "http://" + url;
  }
}
