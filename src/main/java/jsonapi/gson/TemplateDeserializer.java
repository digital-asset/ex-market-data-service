/*
 * Copyright (c) 2019, Digital Asset (Switzerland) GmbH and/or its affiliates. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package jsonapi.gson;

import com.daml.ledger.javaapi.data.Template;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import da.timeservice.timeservice.CurrentTime;
import java.lang.reflect.Type;
import java.time.Instant;
import java.util.List;

// TODO: This is specific for CurrentTime only.
public class TemplateDeserializer implements JsonDeserializer<Template> {

  @Override
  public Template deserialize(
      JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext)
      throws JsonParseException {
    JsonObject o = jsonElement.getAsJsonObject();
    String operator = o.getAsJsonPrimitive("operator").getAsString();
    Instant currentTime =
        jsonDeserializationContext.deserialize(o.get("currentTime"), Instant.class);
    List<String> observers = jsonDeserializationContext.deserialize(o.get("observers"), List.class);
    return new CurrentTime(operator, currentTime, observers);
  }
}
