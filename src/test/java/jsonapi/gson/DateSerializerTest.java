/*
 * Copyright (c) 2019, Digital Asset (Switzerland) GmbH and/or its affiliates. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package jsonapi.gson;

import static org.junit.Assert.assertEquals;

import com.daml.ledger.javaapi.data.Date;
import com.google.gson.Gson;
import com.google.gson.JsonSerializer;
import java.time.LocalDate;
import org.junit.Test;

public class DateSerializerTest extends SerializerBaseTest<Date> {

  @Test
  public void serializeDate() {
    LocalDate javaDate = LocalDate.parse("2020-02-08");
    Date date = new Date((int) javaDate.toEpochDay());

    Gson serializer = createSerializer();

    assertEquals("\"2020-02-08\"", serializer.toJson(date));
  }

  @Override
  protected Class<Date> getSerializedClass() {
    return Date.class;
  }

  @Override
  protected JsonSerializer<Date> getClassSerializer() {
    return new DateSerializer();
  }
}
