/*
 * Copyright (c) 2019, Digital Asset (Switzerland) GmbH and/or its affiliates. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package com.digitalasset.jsonapi.jackson;

import static org.junit.Assert.assertEquals;

import com.daml.ledger.javaapi.data.Date;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import java.io.IOException;
import java.time.LocalDate;
import org.junit.Test;

public class DateDeserializerTest extends DeserializerBaseTest<Date> {

  @Test
  public void deserializeDate() throws IOException {
    String json = "\"2020-02-08\"";

    ObjectMapper deserializer = getClassDeserializer();

    LocalDate javaDate = LocalDate.parse("2020-02-08");
    Date date = new Date((int) javaDate.toEpochDay());
    assertEquals(date, deserializer.readValue(json, getDeserializeClass()));
  }

  @Override
  protected Class<Date> getDeserializeClass() {
    return Date.class;
  }

  @Override
  protected ObjectMapper getClassDeserializer() {
    SimpleModule deserializers =
        new SimpleModule().addDeserializer(Date.class, new DateDeserializer(Date.class));
    return new ObjectMapper().registerModule(deserializers);
  }
}
