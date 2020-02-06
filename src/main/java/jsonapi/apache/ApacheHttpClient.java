/*
 * Copyright (c) 2019, Digital Asset (Switzerland) GmbH and/or its affiliates. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package jsonapi.apache;

import java.io.IOException;
import java.net.URI;
import jsonapi.http.HttpClient;
import jsonapi.http.HttpResponse;
import jsonapi.json.JsonDeserializer;
import jsonapi.json.JsonSerializer;
import org.apache.http.HttpEntity;
import org.apache.http.client.fluent.Request;
import org.apache.http.entity.ContentType;
import org.apache.http.message.BasicHeader;

public class ApacheHttpClient implements HttpClient {

  private static final BasicHeader JSON_CONTENT =
      new BasicHeader("Content-Type", ContentType.APPLICATION_JSON.getMimeType());
  private final JsonDeserializer<HttpResponse> fromJson;
  private final JsonSerializer toJson;
  private final BasicHeader authorization;

  public ApacheHttpClient(
      JsonDeserializer<HttpResponse> fromJson, JsonSerializer toJson, String jwt) {
    this.fromJson = fromJson;
    this.toJson = toJson;
    authorization = new BasicHeader("Authorization", "Bearer " + jwt);
  }

  @Override
  public HttpResponse get(URI resource) {
    Request request = addHeaders(Request.Get(resource));
    return execute(request);
  }

  @Override
  public HttpResponse post(URI resource, Object body) {
    String json = toJson.apply(body);
    Request request =
        addHeaders(Request.Post(resource)).bodyString(json, ContentType.APPLICATION_JSON);
    return execute(request);
  }

  private Request addHeaders(Request request) {
    return request.addHeader(JSON_CONTENT).addHeader(authorization);
  }

  private HttpResponse execute(Request request) {
    try {
      return request.execute().handleResponse(this::handleResponse);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  private HttpResponse handleResponse(org.apache.http.HttpResponse response) throws IOException {
    HttpEntity payload = response.getEntity();
    return fromJson.apply(payload.getContent());
  }
}
