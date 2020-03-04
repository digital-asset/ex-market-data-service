/*
 * Copyright (c) 2019, Digital Asset (Switzerland) GmbH and/or its affiliates. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package com.digitalasset.jsonapi.gson;

import com.daml.ledger.javaapi.data.ExerciseCommand;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import java.lang.reflect.Type;

class ExerciseCommandSerializer implements JsonSerializer<ExerciseCommand> {

  @Override
  public JsonElement serialize(
      ExerciseCommand exerciseCommand,
      Type type,
      JsonSerializationContext jsonSerializationContext) {
    JsonObject json = new JsonObject();
    json.add("templateId", jsonSerializationContext.serialize(exerciseCommand.getTemplateId()));
    json.addProperty("contractId", exerciseCommand.getContractId());
    json.addProperty("choice", exerciseCommand.getChoice());
    json.add("argument", jsonSerializationContext.serialize(exerciseCommand.getChoiceArgument()));
    return json;
  }
}
