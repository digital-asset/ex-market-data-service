/*
 * Copyright (c) 2019, Digital Asset (Switzerland) GmbH and/or its affiliates. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package jsonapi.gson;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import java.lang.reflect.Type;
import java.time.Instant;

public class InstantSerializer implements JsonSerializer<Instant>, JsonDeserializer<Instant> {
  @Override
  public JsonElement serialize(
      Instant instant, Type type, JsonSerializationContext jsonSerializationContext) {
    return new JsonPrimitive(instant.toString());
  }

  @Override
  public Instant deserialize(
      JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext)
      throws JsonParseException {
    return Instant.parse(jsonElement.getAsJsonPrimitive().getAsString());
  }
}
