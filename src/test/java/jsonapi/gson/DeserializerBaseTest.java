/*
 * Copyright (c) 2019, Digital Asset (Switzerland) GmbH and/or its affiliates. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package jsonapi.gson;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializer;
import java.lang.reflect.Type;

public abstract class DeserializerBaseTest<T> {

  protected Gson createDeserializer() {
    return new GsonBuilder()
        .registerTypeAdapter(getDeserializedClass(), getClassDeserializer())
        .create();
  }

  protected abstract Type getDeserializedClass();

  protected abstract JsonDeserializer<T> getClassDeserializer();
}
