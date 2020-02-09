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
import jsonapi.events.Event;
import jsonapi.http.WebSocketResponse;

public class WebSocketResponseDeserializer implements JsonDeserializer<WebSocketResponse> {

  @Override
  public WebSocketResponse deserialize(
      JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext)
      throws JsonParseException {
    Type eventsCollection = new TypeToken<Collection<Event>>() {}.getType();
    Collection<Event> events =
        jsonDeserializationContext.deserialize(jsonElement, eventsCollection);
    return new WebSocketResponse(events);
  }
}
