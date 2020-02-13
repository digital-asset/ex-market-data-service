/*
 * Copyright (c) 2019, Digital Asset (Switzerland) GmbH and/or its affiliates. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package jsonapi.gson;

import static org.junit.Assert.assertEquals;

import com.daml.ledger.javaapi.data.Text;
import com.daml.ledger.javaapi.data.Variant;
import com.google.gson.Gson;
import com.google.gson.JsonSerializer;
import java.lang.reflect.Type;
import org.junit.Test;

public class VariantSerializerTest extends SerializerBaseTest<Variant> {

  @Test
  public void serializeVariant() {
    Variant variant = new Variant("VariantType", new Text("SomeValue"));

    Gson serializer = createSerializer();

    assertEquals(
        "{\"tag\":\"VariantType\",\"value\":{\"value\":\"SomeValue\"}}",
        serializer.toJson(variant));
  }

  @Override
  protected Type getSerializedClass() {
    return Variant.class;
  }

  @Override
  protected JsonSerializer<Variant> getClassSerializer() {
    return new VariantSerializer();
  }
}
