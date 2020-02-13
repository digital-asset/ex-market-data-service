/*
 * Copyright (c) 2019, Digital Asset (Switzerland) GmbH and/or its affiliates. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package jsonapi.gson;

import static org.junit.Assert.assertEquals;

import com.daml.ledger.javaapi.data.Identifier;
import com.google.gson.Gson;
import com.google.gson.JsonSerializer;
import java.lang.reflect.Type;
import org.junit.Test;

public class IdentifierSerializerTest extends SerializerBaseTest<Identifier> {

  @Test
  public void identifierPartsSeparatedWithColons() {
    Identifier identifier = new Identifier("package", "module", "entity");

    Gson serializer = createSerializer();

    assertEquals("\"package:module:entity\"", serializer.toJson(identifier));
  }

  @Override
  protected Type getSerializedClass() {
    return Identifier.class;
  }

  @Override
  protected JsonSerializer<Identifier> getClassSerializer() {
    return new IdentifierSerializer();
  }
}
