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
import java.util.Collection;
import jsonapi.events.CreatedEvent;
import jsonapi.events.Event;
import jsonapi.http.EventHolder;
import jsonapi.http.WebSocketResponse;
import org.hamcrest.core.StringContains;
import org.junit.Test;

public class WebSocketResponseDeserializerTest {

  @Test
  public void deserializeHeartbeat() {
    String json = "{\"heartbeat\":\"ping\"}";
    Gson deserializer = new GsonBuilder().create();
    WebSocketResponse result = deserializer.fromJson(json, WebSocketResponse.class);
    assertThat(result.getHeartbeat().get(), StringContains.containsString("ping"));
  }

  @Test
  public void deserializeLive() {
    String json = "{\"live\":true}";
    Gson deserializer = new GsonBuilder().create();
    WebSocketResponse result = deserializer.fromJson(json, WebSocketResponse.class);
    assertEquals(true, result.getLive().get());
  }

  @Test
  public void deserializeWarning() {
    String json =
        "{\"warnings\":{\"unknownTemplateIds\":[\"230a15b6240603917c18612a7dcb83a7040ab1cf8d498bb4b523b5de03659f58:DA.RefApps.MarketDataService.Roles:OperatorRol\"]}}";
    Gson deserializer = new GsonBuilder().create();
    WebSocketResponse result = deserializer.fromJson(json, WebSocketResponse.class);
    assertThat(result.getWarnings().get().toString(), StringContains.containsString("OperatorRol"));
  }

  @Test
  public void deserializeError() {
    String json =
        "{\"error\":\"Endpoints.InvalidUserInput: JsonReaderError. Cannot read JSON: <{blahblah>. \"}";
    Gson deserializer = new GsonBuilder().create();
    WebSocketResponse result = deserializer.fromJson(json, WebSocketResponse.class);
    assertEquals(
        "Endpoints.InvalidUserInput: JsonReaderError. Cannot read JSON: <{blahblah>. ",
        result.getError().get());
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
    Collection<Event> events = result.getEvents().get();
    assertThat(events.size(), is(1));
    assertThat(events, everyItem(instanceOf(CreatedEvent.class)));
  }
}
