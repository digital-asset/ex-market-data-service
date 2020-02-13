/*
 * Copyright (c) 2019, Digital Asset (Switzerland) GmbH and/or its affiliates. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package jsonapi.gson;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.Collection;
import jsonapi.events.CreatedEvent;
import jsonapi.http.HttpResponse;

public class SearchResultDeserializer implements JsonDeserializer<HttpResponse.SearchResult> {

  @Override
  public HttpResponse.SearchResult deserialize(
      JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext)
      throws JsonParseException {
    Type eventsCollection = new TypeToken<Collection<CreatedEvent>>() {}.getType();
    Collection<CreatedEvent> createdEvents =
        jsonDeserializationContext.deserialize(jsonElement, eventsCollection);
    return new HttpResponse.SearchResult(createdEvents);
  }
}
