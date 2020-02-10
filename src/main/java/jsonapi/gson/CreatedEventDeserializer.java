/*
 * Copyright (c) 2019, Digital Asset (Switzerland) GmbH and/or its affiliates. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package jsonapi.gson;

import com.daml.ledger.javaapi.data.Identifier;
import com.daml.ledger.javaapi.data.Template;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import java.lang.reflect.Type;
import jsonapi.ClassName;
import jsonapi.events.CreatedEvent;

public class CreatedEventDeserializer implements JsonDeserializer<CreatedEvent> {

  @Override
  public CreatedEvent deserialize(
      JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext)
      throws JsonParseException {
    JsonObject o = jsonElement.getAsJsonObject().getAsJsonObject("created");
    // TODO: This is likely not needed and can be eliminated.
    JsonPrimitive templateId = o.getAsJsonPrimitive("templateId");
    Identifier identifier = jsonDeserializationContext.deserialize(templateId, Identifier.class);

    String contractId = o.getAsJsonPrimitive("contractId").getAsString();

    JsonObject payload = o.getAsJsonObject("payload");
    String templateType = ClassName.from(identifier);
    try {
      Template template =
          jsonDeserializationContext.deserialize(payload, Class.forName(templateType));
      return new CreatedEvent(identifier, contractId, template);
    } catch (ClassNotFoundException e) {
      throw new IllegalStateException(e.getMessage(), e);
    }
  }
}
