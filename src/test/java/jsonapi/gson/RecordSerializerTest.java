/*
 * Copyright (c) 2019, Digital Asset (Switzerland) GmbH and/or its affiliates. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package jsonapi.gson;

import static org.junit.Assert.assertEquals;

import com.daml.ledger.javaapi.data.Identifier;
import com.daml.ledger.javaapi.data.Numeric;
import com.daml.ledger.javaapi.data.Record;
import com.daml.ledger.javaapi.data.Record.Field;
import com.google.gson.Gson;
import com.google.gson.JsonSerializer;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import org.junit.Test;

public class RecordSerializerTest extends SerializerBaseTest<Record> {

  @Test
  public void emptyRecordIsSerializedToEmptyObject() {
    Record record = new Record();

    Gson serializer = createSerializer();

    assertEquals("{}", serializer.toJson(record));
  }

  @Test
  public void identifierIsLeftOut() {
    Record record = new Record(new Identifier("p", "m", "e"));

    Gson serializer = createSerializer();

    assertEquals("{}", serializer.toJson(record));
  }

  @Test
  public void fieldWithLabelIsSerialized() {
    Record record = new Record(new Field("apple", new Numeric(BigDecimal.ONE)));

    Gson serializer = createSerializer();

    assertEquals("{\"apple\":{\"value\":1}}", serializer.toJson(record));
  }

  @Test
  public void fieldWithoutLabelIsNotSerialized() {
    Record record = new Record(new Field(new Numeric(BigDecimal.ONE)));

    Gson serializer = createSerializer();

    assertEquals("{}", serializer.toJson(record));
  }

  @Override
  protected Type getSerializedClass() {
    return Record.class;
  }

  @Override
  protected JsonSerializer<Record> getClassSerializer() {
    return new RecordSerializer();
  }
}
