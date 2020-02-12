/*
 * Copyright (c) 2019, Digital Asset (Switzerland) GmbH and/or its affiliates. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package jsonapi.gson;

import com.daml.ledger.javaapi.data.Date;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.time.LocalDate;
import org.junit.Assert;
import org.junit.Test;

public class DateSerializerTest {

  @Test
  public void serializeDate() {
    LocalDate javaDate = LocalDate.parse("2020-02-08");
    Date date = new Date((int) javaDate.toEpochDay());

    Gson serializer =
        new GsonBuilder().registerTypeAdapter(Date.class, new DateSerializer()).create();

    Assert.assertEquals("\"2020-02-08\"", serializer.toJson(date));
  }
}
