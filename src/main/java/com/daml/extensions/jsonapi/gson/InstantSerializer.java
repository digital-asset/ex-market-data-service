/*
 * Copyright (c) 2019, Digital Asset (Switzerland) GmbH and/or its affiliates. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package com.daml.extensions.jsonapi.gson;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import java.lang.reflect.Type;
import java.time.Instant;

class InstantSerializer implements JsonSerializer<Instant> {
  @Override
  public JsonElement serialize(
      Instant instant, Type type, JsonSerializationContext jsonSerializationContext) {
    return new JsonPrimitive(instant.toString());
  }
}
