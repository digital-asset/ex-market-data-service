/*
 * Copyright (c) 2019, Digital Asset (Switzerland) GmbH and/or its affiliates. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package jsonapi.json;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.time.Instant;
import jsonapi.gson.InstantDeserializer;

public class GsonRegisteredAllDeserializers {

  public static Gson gson() {
    return new GsonBuilder().registerTypeAdapter(Instant.class, new InstantDeserializer()).create();
  }
}
