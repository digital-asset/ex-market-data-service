/*
 * Copyright (c) 2019, Digital Asset (Switzerland) GmbH and/or its affiliates. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package com.daml.extensions.jsonapi.gson;

import static org.junit.Assert.assertEquals;

import com.daml.ledger.javaapi.data.DamlRecord;
import com.daml.ledger.javaapi.data.DamlRecord.Field;
import com.daml.ledger.javaapi.data.Identifier;
import com.daml.ledger.javaapi.data.Numeric;
import com.google.gson.Gson;
import com.google.gson.JsonSerializer;
import java.math.BigDecimal;
import org.junit.Test;

public class RecordSerializerTest extends SerializerBaseTest<DamlRecord> {

  @Test
  public void emptyRecordIsSerializedToEmptyObject() {
    DamlRecord record = new DamlRecord();

    Gson serializer = createSerializer();

    assertEquals("{}", serializer.toJson(record));
  }

  @Test
  public void identifierIsLeftOut() {
    DamlRecord record = new DamlRecord(new Identifier("p", "m", "e"));

    Gson serializer = createSerializer();

    assertEquals("{}", serializer.toJson(record));
  }

  @Test
  public void fieldWithLabelIsSerialized() {
    DamlRecord record = new DamlRecord(new Field("apple", new Numeric(BigDecimal.ONE)));
    registerSerializer(Numeric.class, new NumericSerializer());

    Gson serializer = createSerializer();

    assertEquals("{\"apple\":\"1\"}", serializer.toJson(record));
  }

  @Test
  public void fieldWithoutLabelIsNotSerialized() {
    DamlRecord record = new DamlRecord(new Field(new Numeric(BigDecimal.ONE)));

    Gson serializer = createSerializer();

    assertEquals("{}", serializer.toJson(record));
  }

  @Override
  protected Class<DamlRecord> getSerializedClass() {
    return DamlRecord.class;
  }

  @Override
  protected JsonSerializer<DamlRecord> getClassSerializer() {
    return new RecordSerializer();
  }
}
