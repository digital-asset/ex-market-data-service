/*
 * Copyright (c) 2019, Digital Asset (Switzerland) GmbH and/or its affiliates. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package com.digitalasset.jsonapi.gson;

import com.digitalasset.jsonapi.events.CreatedEvent;
import com.digitalasset.jsonapi.http.HttpResponse.CreateResult;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import java.lang.reflect.Type;

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
