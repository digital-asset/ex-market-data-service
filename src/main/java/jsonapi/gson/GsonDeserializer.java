/*
 * Copyright (c) 2019, Digital Asset (Switzerland) GmbH and/or its affiliates. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package jsonapi.gson;

import com.daml.ledger.javaapi.data.Template;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.InputStream;
import java.io.InputStreamReader;
import jsonapi.events.Event;
import jsonapi.http.HttpResponse;
import jsonapi.http.WebSocketResponse;
import jsonapi.json.JsonDeserializer;

public class GsonDeserializer {

  private Gson gson =
      new GsonBuilder()
          .registerTypeAdapter(WebSocketResponse.class, new InstantDeserializer())
          .registerTypeAdapter(WebSocketResponse.class, new WebSocketResponseDeserializer())
          .registerTypeAdapter(Event.class, new EventDeserializer())
          .registerTypeAdapter(Template.class, new CreatedEventDeserializer())
          .create();

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
