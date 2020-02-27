/*
 * Copyright (c) 2019, Digital Asset (Switzerland) GmbH and/or its affiliates. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package com.digitalasset.jsonapi.gson;

import com.digitalasset.jsonapi.http.HttpResponse;
import com.digitalasset.jsonapi.http.WebSocketResponse;
import com.digitalasset.jsonapi.json.JsonDeserializer;
import com.google.gson.Gson;
import java.io.InputStream;
import java.io.InputStreamReader;

public class GsonDeserializer {

  private final Gson gson = GsonRegisteredAllDeserializers.gson();

  private final JsonDeserializer<HttpResponse> httpResponseJsonDeserializer =
      s -> gson.fromJson(toReader(s), HttpResponse.class);

  private final JsonDeserializer<WebSocketResponse> webSocketResponseJsonDeserializer =
      s -> gson.fromJson(toReader(s), WebSocketResponse.class);

  private InputStreamReader toReader(InputStream s) {
    return new InputStreamReader(s);
  }

  public JsonDeserializer<HttpResponse> getHttpResponseDeserializer() {
    return httpResponseJsonDeserializer;
  }

  public JsonDeserializer<WebSocketResponse> getWebSocketResponseDeserializer() {
    return webSocketResponseJsonDeserializer;
  }
}
