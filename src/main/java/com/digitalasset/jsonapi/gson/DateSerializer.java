/*
 * Copyright (c) 2019, Digital Asset (Switzerland) GmbH and/or its affiliates. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package com.digitalasset.jsonapi.gson;

import com.daml.ledger.javaapi.data.Date;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import java.lang.reflect.Type;

class DateSerializer implements JsonSerializer<Date> {

  @Override
  public JsonElement serialize(Date date, Type type, JsonSerializationContext context) {
    return new JsonPrimitive(date.getValue().toString());
  }
}
