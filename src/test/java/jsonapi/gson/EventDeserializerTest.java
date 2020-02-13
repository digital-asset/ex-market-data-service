/*
 * Copyright (c) 2019, Digital Asset (Switzerland) GmbH and/or its affiliates. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package jsonapi.gson;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;

import com.daml.ledger.javaapi.data.Identifier;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import da.refapps.marketdataservice.roles.OperatorRole;
import jsonapi.events.ArchivedEvent;
import jsonapi.events.CreatedEvent;
import jsonapi.events.Event;
import org.junit.Test;

public class EventDeserializerTest {

  @Test
  public void deserializeCreatedEvent() {
    String serializedCreatedEvent =
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
    Gson gson =
        new GsonBuilder()
            .registerTypeAdapter(Identifier.class, new IdentifierDeserializer())
            .registerTypeAdapter(CreatedEvent.class, new CreatedEventDeserializer())
            .registerTypeAdapter(Event.class, new EventDeserializer())
            .create();
    Event deserializedEvent = gson.fromJson(serializedCreatedEvent, Event.class);
    assertThat(deserializedEvent, instanceOf(CreatedEvent.class));
    CreatedEvent deserializedCreatedEvent = (CreatedEvent) deserializedEvent;
    assertEquals(expectedCreatedEvent.getPayload(), deserializedCreatedEvent.getPayload());
    assertEquals(expectedCreatedEvent.getTemplateId(), deserializedCreatedEvent.getTemplateId());
    assertEquals(expectedCreatedEvent.getContractId(), deserializedCreatedEvent.getContractId());
  }

  @Test
  public void deserializeArchivedEvent() {
    String serializedArchivedEvent =
        "{\n"
            + "   \"contractId\":\"#14:1\",\n"
            + "   \"templateId\":"
            + new GsonSerializer().apply(OperatorRole.TEMPLATE_ID)
            + "\n"
            + "}";
    ArchivedEvent expectedArchivedEvent = new ArchivedEvent("#14:1");
    Gson gson =
        new GsonBuilder()
            .registerTypeAdapter(Identifier.class, new IdentifierDeserializer())
            .registerTypeAdapter(Event.class, new EventDeserializer())
            .create();
    Event deserializedEvent = gson.fromJson(serializedArchivedEvent, Event.class);
    assertThat(deserializedEvent, instanceOf(ArchivedEvent.class));
    ArchivedEvent deserializedArchivedEvent = (ArchivedEvent) deserializedEvent;
    assertEquals(expectedArchivedEvent.getContractId(), deserializedArchivedEvent.getContractId());
    assertEquals(expectedArchivedEvent.getContractId(), deserializedArchivedEvent.getContractId());
  }
}
