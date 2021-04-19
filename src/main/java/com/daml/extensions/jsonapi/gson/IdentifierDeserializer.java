/*
 * Copyright (c) 2019, Digital Asset (Switzerland) GmbH and/or its affiliates. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package com.daml.extensions.jsonapi.gson;

import com.daml.ledger.javaapi.data.Identifier;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import java.lang.reflect.Type;

class IdentifierDeserializer implements JsonDeserializer<Identifier> {

  @Override
  public Identifier deserialize(
      JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext)
      throws JsonParseException {
    String[] parts = jsonElement.getAsString().split(":");
    return new Identifier(parts[0], parts[1], parts[2]);
  }
}
