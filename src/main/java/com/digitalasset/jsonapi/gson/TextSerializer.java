/*
 * Copyright (c) 2019, Digital Asset (Switzerland) GmbH and/or its affiliates. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package com.digitalasset.jsonapi.gson;

import com.daml.ledger.javaapi.data.Text;
import com.google.gson.*;
import java.lang.reflect.Type;

class TextSerializer implements JsonSerializer<Text> {

  @Override
  public JsonElement serialize(Text text, Type type, JsonSerializationContext context) {
    return new JsonPrimitive(text.getValue());
  }
}
