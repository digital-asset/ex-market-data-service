/*
 * Copyright (c) 2019, Digital Asset (Switzerland) GmbH and/or its affiliates. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package jsonapi.gson;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import jsonapi.events.Event;
import jsonapi.http.EventHolder;
import jsonapi.http.WebSocketResponse;

public class WebSocketResponseDeserializer implements JsonDeserializer<WebSocketResponse> {

  @Override
  public WebSocketResponse deserialize(
      JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext)
      throws JsonParseException {
    Type eventsCollection = new TypeToken<Collection<Event>>() {}.getType();
    System.err.println(jsonElement);
    Collection<EventHolder> eventHolders =
        jsonDeserializationContext.deserialize(jsonElement, eventsCollection);
    List<Event> events = eventHolders.stream().map(EventHolder::event).collect(Collectors.toList());
    return new WebSocketResponse(events);
  }
}
