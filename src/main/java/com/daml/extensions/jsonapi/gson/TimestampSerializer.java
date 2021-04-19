/*
 * Copyright (c) 2019, Digital Asset (Switzerland) GmbH and/or its affiliates. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package com.daml.extensions.jsonapi.gson;

import com.daml.ledger.javaapi.data.Timestamp;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import java.lang.reflect.Type;

class TimestampSerializer implements JsonSerializer<Timestamp> {

  @Override
  public JsonElement serialize(Timestamp timestamp, Type type, JsonSerializationContext context) {
    return new JsonPrimitive(timestamp.getValue().toString());
  }
}
