/*
 * Copyright (c) 2019, Digital Asset (Switzerland) GmbH and/or its affiliates. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package jsonapi.gson;

import static org.junit.Assert.assertEquals;

import com.daml.ledger.javaapi.data.Timestamp;
import com.google.gson.Gson;
import com.google.gson.JsonSerializer;
import java.lang.reflect.Type;
import java.time.Instant;
import org.junit.Test;

public class TimestampSerializerTest extends SerializerBaseTest<Timestamp> {

  @Test
  public void serializeTimestamp() {
    Timestamp timestamp = Timestamp.fromInstant(Instant.parse("2020-02-08T12:30:00Z"));

    Gson serializer = createSerializer();

    assertEquals("\"2020-02-08T12:30:00Z\"", serializer.toJson(timestamp));
  }

  @Override
  protected Type getSerializedClass() {
    return Timestamp.class;
  }

  @Override
  protected JsonSerializer<Timestamp> getClassSerializer() {
    return new TimestampSerializer();
  }
}
