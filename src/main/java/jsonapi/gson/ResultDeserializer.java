/*
 * Copyright (c) 2019, Digital Asset (Switzerland) GmbH and/or its affiliates. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package jsonapi.gson;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import java.lang.reflect.Type;
import jsonapi.http.HttpResponse;

public class ResultDeserializer implements JsonDeserializer<HttpResponse.Result> {

  @Override
  public HttpResponse.Result deserialize(
      JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext)
      throws JsonParseException {
    JsonObject result = jsonElement.getAsJsonObject();
    return jsonDeserializationContext.deserialize(result, dispatch(result));
  }

  private static Class dispatch(JsonObject result) {
    if (result.size() == 2 && result.has("exerciseResult") && result.has("contracts"))
      return HttpResponse.ExerciseResult.class;
    else throw new IllegalStateException("Unsupported result type. Json content: " + result);
  }
}
