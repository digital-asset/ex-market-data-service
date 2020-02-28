/*
 * Copyright (c) 2019, Digital Asset (Switzerland) GmbH and/or its affiliates. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package com.digitalasset.jsonapi.gson;

import static org.junit.Assert.assertEquals;

import com.daml.ledger.javaapi.data.Identifier;
import com.digitalasset.jsonapi.events.CreatedEvent;
import com.google.gson.Gson;
import com.google.gson.JsonDeserializer;
import da.refapps.marketdataservice.roles.OperatorRole;
import org.junit.Test;

public class CreatedEventDeserializerTest extends DeserializerBaseTest<CreatedEvent> {

  @Test
  public void deserializeCreatedEvent() {
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
    CreatedEvent expectedCreatedEvent =
        new CreatedEvent(OperatorRole.TEMPLATE_ID, "#14:1", new OperatorRole("Operator"));
    registerDeserializer(Identifier.class, new IdentifierDeserializer());

    Gson deserializer = createDeserializer();

    CreatedEvent deserializedCreatedEvent = deserializer.fromJson(json, getDeserializedClass());
    assertEquals(expectedCreatedEvent, deserializedCreatedEvent);
  }

  @Override
  protected Class<CreatedEvent> getDeserializedClass() {
    return CreatedEvent.class;
  }

  @Override
  protected JsonDeserializer<CreatedEvent> getClassDeserializer() {
    return new CreatedEventDeserializer();
  }
}
