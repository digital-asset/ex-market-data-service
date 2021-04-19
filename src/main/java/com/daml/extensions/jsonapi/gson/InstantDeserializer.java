/*
 * Copyright (c) 2019, Digital Asset (Switzerland) GmbH and/or its affiliates. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package com.daml.extensions.jsonapi.gson;

import com.google.gson.*;
import java.lang.reflect.Type;
import java.time.Instant;

class InstantDeserializer implements JsonDeserializer<Instant> {

  @Override
  public Instant deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
      throws JsonParseException {
    return Instant.parse(json.getAsString());
  }
}
