/*
 * Copyright (c) 2019, Digital Asset (Switzerland) GmbH and/or its affiliates. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package com.digitalasset.jsonapi.gson;

import com.daml.ledger.javaapi.data.Date;
import com.daml.ledger.javaapi.data.Identifier;
import com.digitalasset.jsonapi.events.CreatedEvent;
import com.digitalasset.jsonapi.events.Event;
import com.digitalasset.jsonapi.http.EventHolder;
import com.digitalasset.jsonapi.http.HttpResponse.Result;
import com.digitalasset.jsonapi.http.HttpResponse.SearchResult;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import da.refapps.marketdataservice.marketdatatypes.ObservationValue;
import java.time.Instant;
import java.time.LocalDate;

class GsonRegisteredAllDeserializers {

  public static Gson gson() {
    return new GsonBuilder()
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
  }
}
