/*
 * Copyright (c) 2019, Digital Asset (Switzerland) GmbH and/or its affiliates. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package com.digitalasset.jsonapi.gson;

import com.digitalasset.jsonapi.events.ArchivedEvent;
import com.digitalasset.jsonapi.events.CreatedEvent;
import com.digitalasset.jsonapi.events.Event;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import java.lang.reflect.Type;

class EventDeserializer implements JsonDeserializer<Event> {

  @Override
  public Event deserialize(
      JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext)
      throws JsonParseException {
    JsonObject event = jsonElement.getAsJsonObject();
    return jsonDeserializationContext.deserialize(event, dispatch(event));
  }

  private static Class<? extends Event> dispatch(JsonObject event) {
    if (event.size() == 2 && event.has("contractId") && event.has("templateId"))
      return ArchivedEvent.class;
    else if (event.has("payload")) return CreatedEvent.class;
    else throw new IllegalStateException("Unsupported event type. Json content: " + event);
  }
}
