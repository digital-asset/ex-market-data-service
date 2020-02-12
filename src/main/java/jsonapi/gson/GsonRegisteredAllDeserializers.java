/*
 * Copyright (c) 2019, Digital Asset (Switzerland) GmbH and/or its affiliates. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package jsonapi.gson;

import com.daml.ledger.javaapi.data.Identifier;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import da.refapps.marketdataservice.marketdatatypes.ObservationValue;
import java.time.Instant;
import jsonapi.events.CreatedEvent;
import jsonapi.events.Event;
import jsonapi.http.EventHolder;
import jsonapi.http.HttpResponse;
import jsonapi.http.WebSocketResponse;

public class GsonRegisteredAllDeserializers {

  public static Gson gson() {
    return new GsonBuilder()
        .registerTypeAdapter(Instant.class, new InstantDeserializer())
        .registerTypeAdapter(ObservationValue.class, new ObservationValueDeserializer())
        .registerTypeAdapter(Identifier.class, new IdentifierDeserializer())
        .registerTypeAdapter(WebSocketResponse.class, new WebSocketResponseDeserializer())
        .registerTypeAdapter(Event.class, new EventDeserializer())
        .registerTypeAdapter(CreatedEvent.class, new CreatedEventDeserializer())
        .registerTypeAdapter(EventHolder.class, new EventHolderDeserializer())
        .registerTypeAdapter(HttpResponse.Result.class, new ResultDeserializer())
        .create();
  }
}
