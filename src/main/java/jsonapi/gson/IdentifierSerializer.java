/*
 * Copyright (c) 2019, Digital Asset (Switzerland) GmbH and/or its affiliates. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package jsonapi.gson;

import com.daml.ledger.javaapi.data.Identifier;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import java.lang.reflect.Type;

public class IdentifierSerializer
    implements JsonSerializer<Identifier>, JsonDeserializer<Identifier> {

  @Override
  public JsonElement serialize(
      Identifier identifier, Type type, JsonSerializationContext jsonSerializationContext) {
    return new JsonPrimitive(toTemplateId(identifier));
  }

  private String toTemplateId(Identifier identifier) {
    return String.format(
        "%s:%s:%s",
        identifier.getPackageId(), identifier.getModuleName(), identifier.getEntityName());
  }

  @Override
  public Identifier deserialize(
      JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext)
      throws JsonParseException {
    String[] parts = jsonElement.getAsString().split(":");
    return new Identifier(parts[0], parts[1], parts[2]);
  }
}
