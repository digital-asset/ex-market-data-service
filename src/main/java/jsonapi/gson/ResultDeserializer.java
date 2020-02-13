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
    return jsonDeserializationContext.deserialize(jsonElement, dispatch(jsonElement));
  }

  private Type dispatch(JsonElement jsonElement) {
    if (jsonElement.isJsonObject()) {
      JsonObject object = jsonElement.getAsJsonObject();
      if (object.size() == 2 && object.has("exerciseResult") && object.has("events")) {
        return HttpResponse.ExerciseResult.class;
      }
    }
    if (jsonElement.isJsonArray()) {
      return HttpResponse.SearchResult.class;
    } else {
      return HttpResponse.CreateResult.class;
    }
  }
}
