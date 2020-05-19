/*
 * Copyright (c) 2019, Digital Asset (Switzerland) GmbH and/or its affiliates. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package com.digitalasset.jsonapi.jackson;

import static org.junit.Assert.assertEquals;

import com.daml.ledger.javaapi.data.Identifier;
import com.digitalasset.jsonapi.events.CreatedEvent;
import com.digitalasset.jsonapi.gson.GsonSerializer;
import com.digitalasset.jsonapi.jackson.mixins.PartyWrapper;
import com.fasterxml.jackson.annotation.JsonCreator.Mode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;
import da.refapps.marketdataservice.datastream.EmptyDataStream;
import da.refapps.marketdataservice.marketdatatypes.Consumer;
import da.refapps.marketdataservice.marketdatatypes.InstrumentId;
import da.refapps.marketdataservice.marketdatatypes.ObservationReference;
import da.refapps.marketdataservice.marketdatatypes.Publisher;
import da.refapps.marketdataservice.roles.OperatorRole;
import java.io.IOException;
import java.time.LocalDate;
import java.util.Collections;
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

    json =
        "{\n"
            + "    \"payload\": {\n"
            + "        \"operator\": \"Operator\",\n"
            + "        \"reference\": {\n"
            + "            \"market\": \"US Bond Market\",\n"
            + "            \"instrumentId\": {\n"
            + "                \"unpack\": \"ISIN 288 2839\"\n"
            + "            },\n"
            + "            \"maturityDate\": \"2021-02-11\"\n"
            + "        },\n"
            + "        \"consumers\": [\n"
            + "            {\n"
            + "                \"party\": \"MarketDataVendor\"\n"
            + "            }\n"
            + "        ],\n"
            + "        \"publisher\": {\n"
            + "            \"party\": \"MarketDataProvider1\"\n"
            + "        }\n"
            + "    },\n"
            + "    \"contractId\": \"#15:9\",\n"
            + "    \"templateId\": \"31797e58e2941179b7be4f1ab5661948f684273217a67036a9b6bd2ca92a2012:DA.RefApps.MarketDataService.DataStream:EmptyDataStream\"\n"
            + "}";
    expected =
        new CreatedEvent(
            EmptyDataStream.TEMPLATE_ID,
            "#15:9",
            new EmptyDataStream(
                "Operator",
                new ObservationReference(
                    "US Bond Market", new InstrumentId("ISIN 288 2839"), LocalDate.of(2021, 2, 11)),
                Collections.singletonList(new Consumer("MarketDataVendor")),
                new Publisher("MarketDataProvider1")));
    actual = deserializer.readValue(json, getDeserializeClass());
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
            .addDeserializer(InstrumentId.class, new InstrumentIdDeserializer(InstrumentId.class))
            .addDeserializer(OperatorRole.class, new OperatorRoleDeserializer(OperatorRole.class));

    return new ObjectMapper()
        .addMixIn(Consumer.class, PartyWrapper.class)
        .addMixIn(Publisher.class, PartyWrapper.class)
        .registerModules(
            deserializers, new ParameterNamesModule(Mode.PROPERTIES), new JavaTimeModule());
  }
}
