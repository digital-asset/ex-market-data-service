/*
 * Copyright (c) 2019, Digital Asset (Switzerland) GmbH and/or its affiliates. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package jsonapi.gson;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSerializer;
import java.lang.reflect.Type;

public abstract class SerializerBaseTest<T> {

  protected Gson createSerializer() {
    return new GsonBuilder()
        .registerTypeAdapter(getSerializedClass(), getClassSerializer())
        .create();
  }

  protected abstract Type getSerializedClass();

  protected abstract JsonSerializer<T> getClassSerializer();
}
