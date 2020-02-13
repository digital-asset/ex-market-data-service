/*
 * Copyright (c) 2019, Digital Asset (Switzerland) GmbH and/or its affiliates. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package jsonapi.gson;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

import com.google.common.collect.Iterables;
import da.refapps.marketdataservice.roles.OperatorRole;
import jsonapi.events.CreatedEvent;
import jsonapi.events.Event;
import jsonapi.http.WebSocketResponse;
import org.junit.Test;

public class WebSocketResponseDeserializerTest {

  @Test
  public void deserializeWebSocketResponse() {
    String tid = new GsonSerializer().apply(OperatorRole.TEMPLATE_ID);
    String serializedWebSocketResponse =
        "[ \n"
            + "   { \n"
            + "      \"created\":{ \n"
            + "         \"observers\":[],\n"
            + "         \"agreementText\":\"\",\n"
            + "         \"payload\":{ \n"
            + "            \"operator\":\"Operator\"\n"
            + "         },\n"
            + "         \"signatories\":[ \n"
            + "            \"Operator\"\n"
            + "         ],\n"
            + "         \"key\":\"Operator\",\n"
            + "         \"contractId\":\"#11:0\",\n"
            + "         \"templateId\":"
            + tid
            + "\n"
            + "      }\n"
            + "   }\n"
            + "]";
    WebSocketResponse deserializedWebSocketResponse =
        GsonRegisteredAllDeserializers.gson()
            .fromJson(serializedWebSocketResponse, WebSocketResponse.class);
    assertEquals(deserializedWebSocketResponse.getEvents().size(), 1);
    Event event = Iterables.getOnlyElement(deserializedWebSocketResponse.getEvents());
    assertThat(event, instanceOf(CreatedEvent.class));
  }
}
