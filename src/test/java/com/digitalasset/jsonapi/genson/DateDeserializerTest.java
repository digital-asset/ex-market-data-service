/*
 * Copyright (c) 2019, Digital Asset (Switzerland) GmbH and/or its affiliates. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package com.digitalasset.jsonapi.genson;

import static org.junit.Assert.assertEquals;

import com.daml.ledger.javaapi.data.Date;
import com.owlike.genson.Genson;
import com.owlike.genson.GensonBuilder;
import java.time.LocalDate;
import org.junit.Test;

public class DateDeserializerTest extends DeserializerBaseTest<Date> {

  @Test
  public void deserializeDate() {
    String json = "\"2020-02-08\"";

    Genson deserializer = getClassDeserializer();

    LocalDate javaDate = LocalDate.parse("2020-02-08");
    Date date = new Date((int) javaDate.toEpochDay());
    assertEquals(date, deserializer.deserialize(json, getDeserializeClass()));
  }

  @Override
  protected Class<Date> getDeserializeClass() {
    return Date.class;
  }

  protected Genson getClassDeserializer() {
    return new GensonBuilder().withDeserializer(new DateDeserializer(), Date.class).create();
  }
}
