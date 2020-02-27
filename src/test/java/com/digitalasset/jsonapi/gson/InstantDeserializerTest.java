/*
 * Copyright (c) 2019, Digital Asset (Switzerland) GmbH and/or its affiliates. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package com.digitalasset.jsonapi.gson;

import static org.junit.Assert.assertEquals;

import com.google.gson.Gson;
import com.google.gson.JsonDeserializer;
import java.time.Instant;
import org.junit.Test;

public class InstantDeserializerTest extends DeserializerBaseTest<Instant> {

  @Test
  public void deserializeInstant() {
    String json = "\"2020-02-04T22:57:29Z\"";

    Gson deserializer = createDeserializer();

    Instant expected = Instant.parse("2020-02-04T22:57:29Z");
    assertEquals(expected, deserializer.fromJson(json, getDeserializedClass()));
  }

  @Override
  protected Class<Instant> getDeserializedClass() {
    return Instant.class;
  }

  @Override
  protected JsonDeserializer<Instant> getClassDeserializer() {
    return new InstantDeserializer();
  }
}
