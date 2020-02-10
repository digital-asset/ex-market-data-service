/*
 * Copyright (c) 2019, Digital Asset (Switzerland) GmbH and/or its affiliates. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package jsonapi.gson;

import com.daml.ledger.javaapi.data.Record;
import com.daml.ledger.javaapi.data.Record.Field;
import com.google.gson.*;
import java.lang.reflect.Type;

public class RecordSerializer implements JsonSerializer<Record> {

  @Override
  public JsonElement serialize(Record record, Type type, JsonSerializationContext context) {
    JsonObject json = new JsonObject();
    // TODO: Implement properly.
    for (Field field : record.getFields()) {
      System.out.println(field); // TODO remove
      field.getLabel().ifPresent(label -> json.add(label, context.serialize(field.getValue())));
    }
    return json;
  }
}
