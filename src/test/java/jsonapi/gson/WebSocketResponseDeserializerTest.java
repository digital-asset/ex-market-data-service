/*
 * Copyright (c) 2019, Digital Asset (Switzerland) GmbH and/or its affiliates. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package jsonapi.gson;

import static org.hamcrest.CoreMatchers.everyItem;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import com.daml.ledger.javaapi.data.Identifier;
import com.google.gson.Gson;
import com.google.gson.JsonDeserializer;
import da.refapps.marketdataservice.roles.OperatorRole;
import java.lang.reflect.Type;
import jsonapi.events.CreatedEvent;
import jsonapi.events.Event;
import jsonapi.http.EventHolder;
import jsonapi.http.WebSocketResponse;
import org.junit.Test;

public class WebSocketResponseDeserializerTest extends DeserializerBaseTest<WebSocketResponse> {

  @Test
  public void TodoIsThereSuchJsonInput() {
    String tid = new GsonSerializer().apply(OperatorRole.TEMPLATE_ID);
    String json =
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
    registerDeserializer(Identifier.class, new IdentifierDeserializer());
    registerDeserializer(Event.class, new EventDeserializer());
    registerDeserializer(CreatedEvent.class, new CreatedEventDeserializer());
    registerDeserializer(EventHolder.class, new EventHolderDeserializer());

    Gson deserializer = createDeserializer();

    WebSocketResponse result = deserializer.fromJson(json, getDeserializedClass());
    assertThat(result.getEvents().size(), is(1));
    assertThat(result.getEvents(), everyItem(instanceOf(CreatedEvent.class)));
  }

  @Test
  public void search() {
    String serializedWebSocketResponse =
        "{ \n"
            + "   \"events\":[ \n"
            + "      { \n"
            + "         \"created\":{ \n"
            + "            \"observers\":[ \n"
            + "\n"
            + "            ],\n"
            + "            \"agreementText\":\"\",\n"
            + "            \"payload\":{ \n"
            + "               \"operator\":\"Operator\",\n"
            + "               \"currentTime\":\"2020-02-13T10:29:57.413838Z\",\n"
            + "               \"observers\":[ \n"
            + "\n"
            + "               ]\n"
            + "            },\n"
            + "            \"signatories\":[ \n"
            + "               \"Operator\"\n"
            + "            ],\n"
            + "            \"key\":\"Operator\",\n"
            + "            \"contractId\":\"#12:0\",\n"
            + "            \"templateId\":\"230a15b6240603917c18612a7dcb83a7040ab1cf8d498bb4b523b5de03659f58:DA.TimeService.TimeService:CurrentTime\"\n"
            + "         },\n"
            + "         \"matchedQueries\":[ \n"
            + "            0\n"
            + "         ]\n"
            + "      }\n"
            + "   ]\n"
            + "}";
    WebSocketResponse deserializedWebSocketResponse =
        GsonRegisteredAllDeserializers.gson()
            .fromJson(serializedWebSocketResponse, WebSocketResponse.class);
  }

  @Override
  protected Type getDeserializedClass() {
    return WebSocketResponse.class;
  }

  @Override
  protected JsonDeserializer<WebSocketResponse> getClassDeserializer() {
    return new WebSocketResponseDeserializer();
  }
}
