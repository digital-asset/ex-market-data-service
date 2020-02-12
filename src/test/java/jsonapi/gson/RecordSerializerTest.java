/*
 * Copyright (c) 2019, Digital Asset (Switzerland) GmbH and/or its affiliates. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package jsonapi.gson;

import static org.junit.Assert.assertEquals;

import com.daml.ledger.javaapi.data.Record;
import com.google.gson.Gson;
import com.google.gson.JsonSerializer;
import java.lang.reflect.Type;
import org.junit.Test;

public class RecordSerializerTest extends SerializerBaseTest<Record> {

  @Test
  public void emptyRecordIsSerializedToEmptyObject() {
    Record record = new Record();

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
