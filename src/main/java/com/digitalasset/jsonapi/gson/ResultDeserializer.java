/*
 * Copyright (c) 2019, Digital Asset (Switzerland) GmbH and/or its affiliates. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package com.digitalasset.jsonapi.gson;

import com.digitalasset.jsonapi.http.HttpResponse.CreateResult;
import com.digitalasset.jsonapi.http.HttpResponse.ExerciseResult;
import com.digitalasset.jsonapi.http.HttpResponse.Result;
import com.digitalasset.jsonapi.http.HttpResponse.SearchResult;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import java.lang.reflect.Type;

class ResultDeserializer implements JsonDeserializer<Result> {

  @Override
  public Result deserialize(
      JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext)
      throws JsonParseException {
    return jsonDeserializationContext.deserialize(jsonElement, dispatch(jsonElement));
  }

  private Type dispatch(JsonElement jsonElement) {
    if (jsonElement.isJsonObject()) {
      JsonObject object = jsonElement.getAsJsonObject();
      if (object.size() == 2 && object.has("exerciseResult") && object.has("events")) {
        return ExerciseResult.class;
      } else {
        return CreateResult.class;
      }
    } else if (jsonElement.isJsonArray()) {
      return SearchResult.class;
    } else {
      throw new IllegalStateException("Could not detect result type");
    }
  }
}
