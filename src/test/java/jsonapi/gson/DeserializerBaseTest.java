/*
 * Copyright (c) 2019, Digital Asset (Switzerland) GmbH and/or its affiliates. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package jsonapi.gson;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonSerializer;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

public abstract class DeserializerBaseTest<T> {

  private Map<Type, JsonSerializer<?>> serializers = new HashMap<>();

  protected Gson createDeserializer() {
    GsonBuilder builderWithDefaultSerializer =
        new GsonBuilder().registerTypeAdapter(getDeserializedClass(), getClassDeserializer());
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

  protected abstract Type getDeserializedClass();

  protected abstract JsonDeserializer<T> getClassDeserializer();
}
