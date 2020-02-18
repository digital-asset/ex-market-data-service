/*
 * Copyright (c) 2019, Digital Asset (Switzerland) GmbH and/or its affiliates. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package jsonapi.gson;

import static org.junit.Assert.assertEquals;

import com.google.gson.Gson;
import com.google.gson.JsonDeserializer;
import java.time.LocalDate;
import org.junit.Test;

public class LocalDateDeserializerTest extends DeserializerBaseTest<LocalDate> {

  @Test
  public void deserializeLocalDate() {
    String json = "\"2020-02-08\"";

    Gson deserializer = createDeserializer();

    LocalDate date = LocalDate.parse("2020-02-08");
    assertEquals(date, deserializer.fromJson(json, getDeserializedClass()));
  }

  @Override
  protected Class<LocalDate> getDeserializedClass() {
    return LocalDate.class;
  }

  @Override
  protected JsonDeserializer<LocalDate> getClassDeserializer() {
    return new LocalDateDeserializer();
  }
}
