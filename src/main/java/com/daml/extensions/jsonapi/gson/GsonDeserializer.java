/*
 * Copyright (c) 2019, Digital Asset (Switzerland) GmbH and/or its affiliates. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package com.daml.extensions.jsonapi.gson;

import com.daml.ledger.javaapi.data.Date;
import com.daml.ledger.javaapi.data.Identifier;
import com.daml.extensions.jsonapi.events.CreatedEvent;
import com.daml.extensions.jsonapi.events.Event;
import com.daml.extensions.jsonapi.http.EventHolder;
import com.daml.extensions.jsonapi.http.HttpResponse;
import com.daml.extensions.jsonapi.http.HttpResponse.Result;
import com.daml.extensions.jsonapi.http.HttpResponse.SearchResult;
import com.daml.extensions.jsonapi.http.WebSocketResponse;
import com.daml.extensions.jsonapi.json.JsonDeserializer;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import da.refapps.marketdataservice.marketdatatypes.ObservationValue;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.time.Instant;
import java.time.LocalDate;

public class GsonDeserializer {

  private final Gson gson =
      new GsonBuilder()
          .registerTypeAdapter(Instant.class, new InstantDeserializer())
          .registerTypeAdapter(ObservationValue.class, new ObservationValueDeserializer())
          .registerTypeAdapter(Identifier.class, new IdentifierDeserializer())
          .registerTypeAdapter(Date.class, new DateDeserializer())
          .registerTypeAdapter(LocalDate.class, new LocalDateDeserializer())
          .registerTypeAdapter(Event.class, new EventDeserializer())
          .registerTypeAdapter(CreatedEvent.class, new CreatedEventDeserializer())
          .registerTypeAdapter(EventHolder.class, new EventHolderDeserializer())
          .registerTypeAdapter(Result.class, new ResultDeserializer())
          .registerTypeAdapter(SearchResult.class, new SearchResultDeserializer())
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
