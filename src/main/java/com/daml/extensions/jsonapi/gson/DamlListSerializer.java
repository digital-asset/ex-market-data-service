/*
 * Copyright (c) 2019, Digital Asset (Switzerland) GmbH and/or its affiliates. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package com.daml.extensions.jsonapi.gson;

import com.daml.ledger.javaapi.data.DamlList;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import java.lang.reflect.Type;

class DamlListSerializer implements JsonSerializer<DamlList> {

  @Override
  public JsonElement serialize(
      DamlList damlList, Type type, JsonSerializationContext jsonSerializationContext) {
    JsonArray json = new JsonArray();
    damlList.stream().map(jsonSerializationContext::serialize).forEach(json::add);
    return json;
  }
}
