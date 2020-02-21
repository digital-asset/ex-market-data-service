/*
 * Copyright (c) 2019, Digital Asset (Switzerland) GmbH and/or its affiliates. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package jsonapi.gson;

import static org.hamcrest.CoreMatchers.everyItem;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

import com.daml.ledger.javaapi.data.Identifier;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import da.timeservice.timeservice.CurrentTime;
import java.time.Instant;
import jsonapi.events.CreatedEvent;
import jsonapi.events.Event;
import jsonapi.http.CreatedEventHolder;
import jsonapi.http.EventHolder;
import jsonapi.http.WebSocketResponse;
import org.junit.Test;

public class WebSocketResponseDeserializerTest {

  @Test
  public void deserializeError() {
    String json =
        "{\"error\":\"Endpoints.InvalidUserInput: JsonReaderError. Cannot read JSON: <{blahblah>. \"}";
    Gson deserializer = new GsonBuilder().create();
    WebSocketResponse result = deserializer.fromJson(json, WebSocketResponse.class);
    assertEquals(
        "Endpoints.InvalidUserInput: JsonReaderError. Cannot read JSON: <{blahblah>. ",
        result.getError());
  }

  @Test
  public void deserializeQueryResponse() {
    String templateId = new GsonSerializer().apply(CurrentTime.TEMPLATE_ID);
    String json =
        "{\n"
            + "  \"events\": [\n"
            + "    {\n"
            + "      \"created\": {\n"
            + "        \"observers\": [],\n"
            + "        \"agreementText\": \"\",\n"
            + "        \"payload\": {\n"
            + "          \"operator\": \"Operator\",\n"
            + "          \"currentTime\": \"2020-02-13T10:29:57.413838Z\",\n"
            + "          \"observers\": []\n"
            + "        },\n"
            + "        \"signatories\": [\n"
            + "          \"Operator\"\n"
            + "        ],\n"
            + "        \"key\": \"Operator\",\n"
            + "        \"contractId\": \"#12:0\",\n"
            + "        \"templateId\": "
            + templateId
            + "\n"
            + "      },\n"
            + "      \"matchedQueries\": [\n"
            + "        0\n"
            + "      ]\n"
            + "    }\n"
            + "  ]\n"
            + "}";
    Gson deserializer =
        new GsonBuilder()
            .registerTypeAdapter(Identifier.class, new IdentifierDeserializer())
            .registerTypeAdapter(Instant.class, new InstantDeserializer())
            .registerTypeAdapter(Event.class, new EventDeserializer())
            .registerTypeAdapter(CreatedEvent.class, new CreatedEventDeserializer())
            .registerTypeAdapter(EventHolder.class, new EventHolderDeserializer())
            .create();

    WebSocketResponse result = deserializer.fromJson(json, WebSocketResponse.class);
    assertThat(result.getEventHolders().size(), is(1));
    assertThat(result.getEventHolders(), everyItem(instanceOf(CreatedEventHolder.class)));
  }
}
