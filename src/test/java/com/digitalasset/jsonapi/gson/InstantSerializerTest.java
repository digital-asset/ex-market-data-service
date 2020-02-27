/*
 * Copyright (c) 2019, Digital Asset (Switzerland) GmbH and/or its affiliates. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package com.digitalasset.jsonapi.gson;

import static org.junit.Assert.assertEquals;

import com.google.gson.Gson;
import com.google.gson.JsonSerializer;
import java.time.Instant;
import org.junit.Test;

public class InstantSerializerTest extends SerializerBaseTest<Instant> {

  @Test
  public void serializeInstant() {
    Instant instant = Instant.parse("2020-02-04T22:57:29Z");

    Gson serializer = createSerializer();

    assertEquals("\"2020-02-04T22:57:29Z\"", serializer.toJson(instant));
  }

  @Override
  protected Class<Instant> getSerializedClass() {
    return Instant.class;
  }

  @Override
  protected JsonSerializer<Instant> getClassSerializer() {
    return new InstantSerializer();
  }
}
