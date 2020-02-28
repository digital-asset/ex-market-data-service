/*
 * Copyright (c) 2019, Digital Asset (Switzerland) GmbH and/or its affiliates. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package com.digitalasset.jsonapi.gson;

import static org.junit.Assert.assertEquals;

import com.daml.ledger.javaapi.data.Date;
import com.google.gson.Gson;
import com.google.gson.JsonDeserializer;
import java.time.LocalDate;
import org.junit.Test;

public class DateDeserializerTest extends DeserializerBaseTest<Date> {

  @Test
  public void deserializeDate() {
    String json = "\"2020-02-08\"";

    Gson deserializer = createDeserializer();

    LocalDate javaDate = LocalDate.parse("2020-02-08");
    Date date = new Date((int) javaDate.toEpochDay());
    assertEquals(date, deserializer.fromJson(json, getDeserializedClass()));
  }

  @Override
  protected Class<Date> getDeserializedClass() {
    return Date.class;
  }

  @Override
  protected JsonDeserializer<Date> getClassDeserializer() {
    return new DateDeserializer();
  }
}
