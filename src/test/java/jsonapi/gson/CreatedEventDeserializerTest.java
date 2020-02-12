/*
 * Copyright (c) 2019, Digital Asset (Switzerland) GmbH and/or its affiliates. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package jsonapi.gson;

import com.daml.ledger.javaapi.data.Identifier;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import da.refapps.marketdataservice.roles.OperatorRole;
import jsonapi.events.CreatedEvent;
import org.junit.Assert;
import org.junit.Test;

public class CreatedEventDeserializerTest {

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
            .create();
    CreatedEvent deserializedCreatedEvent =
        gson.fromJson(serializedCreatedEvent, CreatedEvent.class);
    Assert.assertEquals(expectedCreatedEvent.getPayload(), deserializedCreatedEvent.getPayload());
    Assert.assertEquals(
        expectedCreatedEvent.getTemplateId(), deserializedCreatedEvent.getTemplateId());
    Assert.assertEquals(
        expectedCreatedEvent.getContractId(), deserializedCreatedEvent.getContractId());
  }
}
