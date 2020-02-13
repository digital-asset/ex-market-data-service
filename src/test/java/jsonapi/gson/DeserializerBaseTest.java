/*
 * Copyright (c) 2019, Digital Asset (Switzerland) GmbH and/or its affiliates. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package jsonapi.gson;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializer;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

public abstract class DeserializerBaseTest<T> {

  private Map<Type, JsonDeserializer<?>> serializers = new HashMap<>();

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

  protected <U> void registerDeserializer(Type type, JsonDeserializer<U> deserializer) {
    serializers.put(type, deserializer);
  }

  protected abstract Type getDeserializedClass();

  protected abstract JsonDeserializer<T> getClassDeserializer();
}
