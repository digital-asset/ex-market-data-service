/*
 * Copyright (c) 2019, Digital Asset (Switzerland) GmbH and/or its affiliates. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package jsonapi.gson;

import com.daml.ledger.javaapi.data.Variant;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import java.lang.reflect.Type;

public class VariantSerializer implements JsonSerializer<Variant> {

  @Override
  public JsonElement serialize(Variant variant, Type type, JsonSerializationContext context) {
    JsonObject json = new JsonObject();
    json.addProperty("tag", variant.getConstructor());
    json.add("value", context.serialize(variant.getValue()));
    return json;
  }
}
