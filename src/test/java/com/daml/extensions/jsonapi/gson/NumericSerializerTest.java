/*
 * Copyright (c) 2019, Digital Asset (Switzerland) GmbH and/or its affiliates. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package com.daml.extensions.jsonapi.gson;

import static org.junit.Assert.assertEquals;

import com.daml.ledger.javaapi.data.Numeric;
import com.google.gson.Gson;
import com.google.gson.JsonSerializer;
import java.math.BigDecimal;
import org.junit.Test;

public class NumericSerializerTest extends SerializerBaseTest<Numeric> {
  @Test
  public void serializeNumeric() {
    Numeric numeric = new Numeric(BigDecimal.valueOf(12.3));

    Gson serializer = createSerializer();

    assertEquals("\"12.3\"", serializer.toJson(numeric));
  }

  @Override
  protected Class<Numeric> getSerializedClass() {
    return Numeric.class;
  }

  @Override
  protected JsonSerializer<Numeric> getClassSerializer() {
    return new NumericSerializer();
  }
}
