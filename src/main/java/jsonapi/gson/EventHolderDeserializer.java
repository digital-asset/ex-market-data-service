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
import jsonapi.http.ArchivedEventHolder;
import jsonapi.http.CreatedEventHolder;
import jsonapi.http.EventHolder;

public class EventHolderDeserializer implements JsonDeserializer<EventHolder> {

  @Override
  public EventHolder deserialize(
      JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext)
      throws JsonParseException {
    JsonObject eventHolder = jsonElement.getAsJsonObject();
    return jsonDeserializationContext.deserialize(eventHolder, dispatch(eventHolder));
  }

  private static Class dispatch(JsonObject eventHolder) {
    if (eventHolder.has("archived")) return ArchivedEventHolder.class;
    else if (eventHolder.has("created")) return CreatedEventHolder.class;
    else throw new IllegalStateException("Unsupported event type. Json content: " + eventHolder);
  }
}
