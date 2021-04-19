/*
 * Copyright (c) 2019, Digital Asset (Switzerland) GmbH and/or its affiliates. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package com.daml.extensions.jsonapi.gson;

import static org.junit.Assert.assertEquals;

import com.daml.ledger.javaapi.data.Party;
import com.google.gson.Gson;
import com.google.gson.JsonSerializer;
import org.junit.Test;

public class PartySerializerTest extends SerializerBaseTest<Party> {

  @Test
  public void serializedPartyContainsName() {
    Party party = new Party("John Doe");

    Gson serializer = createSerializer();

    assertEquals("\"John Doe\"", serializer.toJson(party));
  }

  @Override
  protected Class<Party> getSerializedClass() {
    return Party.class;
  }

  @Override
  protected JsonSerializer<Party> getClassSerializer() {
    return new PartySerializer();
  }
}
