/*
 * Copyright (c) 2019, Digital Asset (Switzerland) GmbH and/or its affiliates. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package com.daml.extensions.jsonapi.gson;

import com.daml.ledger.javaapi.data.Record;
import com.daml.ledger.javaapi.data.Record.Field;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import java.lang.reflect.Type;

/** For simplicity this implementation serializes only labeled {@link Field}s. */
class RecordSerializer implements JsonSerializer<Record> {

  @Override
  public JsonElement serialize(Record record, Type type, JsonSerializationContext context) {
    JsonObject json = new JsonObject();
    for (Field field : record.getFields()) {
      field.getLabel().ifPresent(label -> json.add(label, context.serialize(field.getValue())));
    }
    return json;
  }
}
