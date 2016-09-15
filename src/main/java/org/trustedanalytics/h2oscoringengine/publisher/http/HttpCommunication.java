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
package org.trustedanalytics.h2oscoringengine.publisher.http;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;

public final class HttpCommunication {

  public static final String CONTENT_TYPE_HEADER_NAME = "Content-type";
  public static final String JSON_ACCEPT_HEADER_VALUE = "application/json";

  private HttpCommunication() {}

  public static HttpEntity<String> basicAuthRequest(String basicAuthToken) {
    HttpHeaders headers = new HttpHeaders();
    headers.add("Authorization", "Basic " + basicAuthToken);

    return new HttpEntity<>(headers);
  }

  public static HttpHeaders basicAuthJsonHeaders(String basicAuthToken) {
    HttpHeaders headers = new HttpHeaders();
    headers.add(CONTENT_TYPE_HEADER_NAME, JSON_ACCEPT_HEADER_VALUE);
    headers.add("Authorization", "Basic " + basicAuthToken);

    return headers;
  }

}
