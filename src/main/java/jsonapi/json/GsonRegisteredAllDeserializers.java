/*
 * Copyright (c) 2019, Digital Asset (Switzerland) GmbH and/or its affiliates. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package jsonapi.json;

import com.daml.ledger.javaapi.data.Identifier;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import da.refapps.marketdataservice.marketdatatypes.ObservationValue;
import java.time.Instant;
import jsonapi.events.CreatedEvent;
import jsonapi.events.Event;
import jsonapi.gson.CreatedEventDeserializer;
import jsonapi.gson.EventDeserializer;
import jsonapi.gson.HttpResponseDeserializer;
import jsonapi.gson.IdentifierDeserializer;
import jsonapi.gson.InstantDeserializer;
import jsonapi.gson.ObservationValueDeserializer;
import jsonapi.gson.WebSocketResponseDeserializer;
import jsonapi.http.HttpResponse;
import jsonapi.http.WebSocketResponse;

public class GsonRegisteredAllDeserializers {

  public static Gson gson() {
    return new GsonBuilder()
        .registerTypeAdapter(Instant.class, new InstantDeserializer())
        .registerTypeAdapter(Identifier.class, new IdentifierDeserializer())
        .registerTypeAdapter(WebSocketResponse.class, new WebSocketResponseDeserializer())
        .registerTypeAdapter(HttpResponse.class, new HttpResponseDeserializer())
        .registerTypeAdapter(Event.class, new EventDeserializer())
        .registerTypeAdapter(CreatedEvent.class, new CreatedEventDeserializer())
        .registerTypeAdapter(ObservationValue.class, new ObservationValueDeserializer())
        .create();
  }
}
