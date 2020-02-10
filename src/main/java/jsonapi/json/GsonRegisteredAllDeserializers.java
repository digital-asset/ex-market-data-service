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
import jsonapi.events.Event;
import jsonapi.gson.*;
import jsonapi.http.WebSocketResponse;

public class GsonRegisteredAllDeserializers {

  public static Gson gson() {
    return new GsonBuilder()
        .registerTypeAdapter(Instant.class, new InstantDeserializer())
        .registerTypeAdapter(ObservationValue.class, new ObservationValueDeserializer())
        .registerTypeAdapter(Identifier.class, new IdentifierSerializer())
        .registerTypeAdapter(WebSocketResponse.class, new WebSocketResponseDeserializer())
        .registerTypeAdapter(Event.class, new CreatedEventDeserializer())
        .create();
  }
}
