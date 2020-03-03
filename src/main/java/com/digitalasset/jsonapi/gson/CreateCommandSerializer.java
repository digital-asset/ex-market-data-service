/*
 * Copyright (c) 2019, Digital Asset (Switzerland) GmbH and/or its affiliates. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package com.digitalasset.jsonapi.gson;

import com.daml.ledger.javaapi.data.CreateCommand;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import java.lang.reflect.Type;

class CreateCommandSerializer implements JsonSerializer<CreateCommand> {

  @Override
  public JsonElement serialize(
      CreateCommand createCommand, Type type, JsonSerializationContext jsonSerializationContext) {
    JsonObject json = new JsonObject();
    json.add("templateId", jsonSerializationContext.serialize(createCommand.getTemplateId()));
    json.add("payload", jsonSerializationContext.serialize(createCommand.getCreateArguments()));
    return json;
  }
}
