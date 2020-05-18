/*
 * Copyright (c) 2019, Digital Asset (Switzerland) GmbH and/or its affiliates. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package com.digitalasset.jsonapi.jackson;

import com.daml.ledger.javaapi.data.Identifier;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import java.io.IOException;

class IdentifierDeserializer extends StdDeserializer<Identifier> {

  protected IdentifierDeserializer(Class<?> vc) {
    super(vc);
  }

  @Override
  public Identifier deserialize(
      JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException {
    String[] parts = jsonParser.getValueAsString().split(":");
    return new Identifier(parts[0], parts[1], parts[2]);
  }
}
