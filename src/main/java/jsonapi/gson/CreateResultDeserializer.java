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
import jsonapi.events.CreatedEvent;
import jsonapi.http.HttpResponse.CreateResult;

class CreateResultDeserializer implements JsonDeserializer<CreateResult> {

  @Override
  public CreateResult deserialize(
      JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext)
      throws JsonParseException {
    CreatedEvent createdEvent =
        jsonDeserializationContext.deserialize(jsonElement, CreatedEvent.class);
    return new CreateResult(createdEvent);
  }
}
