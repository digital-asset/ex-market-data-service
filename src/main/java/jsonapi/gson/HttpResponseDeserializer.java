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
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.Collection;
import jsonapi.events.CreatedEvent;
import jsonapi.http.HttpResponse;

public class HttpResponseDeserializer implements JsonDeserializer<HttpResponse> {

  @Override
  public HttpResponse deserialize(
      JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext)
      throws JsonParseException {
    JsonObject o = jsonElement.getAsJsonObject();
    int status = o.getAsJsonPrimitive("status").getAsInt();
    // TODO: Works only for search result.
    JsonElement result = o.get("result");
    Type eventsCollection = new TypeToken<Collection<CreatedEvent>>() {}.getType();
    Collection<CreatedEvent> events =
        jsonDeserializationContext.deserialize(result, eventsCollection);
    return new HttpResponse(status, events, null, null);
  }
}
