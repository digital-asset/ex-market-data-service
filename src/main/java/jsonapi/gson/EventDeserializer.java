/*
 * Copyright (c) 2019, Digital Asset (Switzerland) GmbH and/or its affiliates. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package jsonapi.gson;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import java.lang.reflect.Type;
import jsonapi.events.ArchivedEvent;
import jsonapi.events.CreatedEvent;
import jsonapi.events.Event;

public class EventDeserializer implements JsonDeserializer<Event> {

  @Override
  public Event deserialize(
      JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext)
      throws JsonParseException {
    JsonObject event = jsonElement.getAsJsonObject();
    if (event.has("archive")) {
      return jsonDeserializationContext.deserialize(event.get("archived"), ArchivedEvent.class);
    } else if (event.has("created")) {
      return jsonDeserializationContext.deserialize(event, CreatedEvent.class);
    } else {
      throw new IllegalStateException("Unsupported event type.");
    }
  }
}
