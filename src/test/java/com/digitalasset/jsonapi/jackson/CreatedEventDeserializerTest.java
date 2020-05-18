/*
 * Copyright (c) 2019, Digital Asset (Switzerland) GmbH and/or its affiliates. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package com.digitalasset.jsonapi.jackson;

import static org.junit.Assert.assertEquals;

import com.daml.ledger.javaapi.data.Identifier;
import com.digitalasset.jsonapi.events.CreatedEvent;
import com.digitalasset.jsonapi.gson.GsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import da.refapps.marketdataservice.roles.OperatorRole;
import java.io.IOException;
import org.junit.Test;

public class CreatedEventDeserializerTest extends DeserializerBaseTest<CreatedEvent> {

  @Test
  public void deserializeCreatedEvent() throws IOException {
    String json =
        "{\n"
            + "   \"payload\":{ \n"
            + "      \"operator\":\"Operator\"\n"
            + "   },\n"
            + "   \"contractId\":\"#14:1\",\n"
            + "   \"templateId\":"
            + new GsonSerializer().apply(OperatorRole.TEMPLATE_ID)
            + "\n"
            + "}";
    CreatedEvent expected =
        new CreatedEvent(OperatorRole.TEMPLATE_ID, "#14:1", new OperatorRole("Operator"));

    ObjectMapper deserializer = getClassDeserializer();

    CreatedEvent actual = deserializer.readValue(json, getDeserializeClass());
    assertEquals(expected, actual);
  }

  @Override
  protected Class<CreatedEvent> getDeserializeClass() {
    return CreatedEvent.class;
  }

  @Override
  protected ObjectMapper getClassDeserializer() {
    SimpleModule deserializers =
        new SimpleModule()
            .addDeserializer(CreatedEvent.class, new CreatedEventDeserializer(CreatedEvent.class))
            .addDeserializer(Identifier.class, new IdentifierDeserializer(Identifier.class))
            .addDeserializer(OperatorRole.class, new OperatorRoleDeserializer(OperatorRole.class));
    return new ObjectMapper().registerModule(deserializers);
  }
}
