/*
 * Copyright (c) 2019, Digital Asset (Switzerland) GmbH and/or its affiliates. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package jsonapi.gson;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import java.lang.reflect.Type;
import jsonapi.http.HttpResponse;

public class SearchResultDeserializer implements JsonDeserializer<HttpResponse.SearchResult> {

  @Override
  public HttpResponse.SearchResult deserialize(
      JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext)
      throws JsonParseException {
    throw new RuntimeException("implement by Ricsi");
  }
}
