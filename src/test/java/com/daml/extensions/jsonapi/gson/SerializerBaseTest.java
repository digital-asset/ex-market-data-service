/*
 * Copyright (c) 2019, Digital Asset (Switzerland) GmbH and/or its affiliates. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package com.daml.extensions.jsonapi.gson;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSerializer;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

public abstract class SerializerBaseTest<T> {

  private Map<Type, JsonSerializer<?>> serializers = new HashMap<>();

  protected Gson createSerializer() {
    GsonBuilder builderWithDefaultSerializer =
        new GsonBuilder().registerTypeAdapter(getSerializedClass(), getClassSerializer());
    return serializers.entrySet().stream()
        .reduce(
            builderWithDefaultSerializer,
            (builder, serializer) ->
                builder.registerTypeAdapter(serializer.getKey(), serializer.getValue()),
            (x, y) -> x)
        .create();
  }

  protected <U> void registerSerializer(Type type, JsonSerializer<U> serializer) {
    serializers.put(type, serializer);
  }

  protected abstract Class<T> getSerializedClass();

  protected abstract JsonSerializer<T> getClassSerializer();
}
