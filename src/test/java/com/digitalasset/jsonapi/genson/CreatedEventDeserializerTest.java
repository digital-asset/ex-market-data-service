/*
 * Copyright (c) 2019, Digital Asset (Switzerland) GmbH and/or its affiliates. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package com.digitalasset.jsonapi.genson;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

import com.daml.ledger.javaapi.data.Identifier;
import com.digitalasset.jsonapi.events.CreatedEvent;
import com.owlike.genson.Genson;
import com.owlike.genson.GensonBuilder;
import da.refapps.marketdataservice.datastream.EmptyDataStream;
import da.refapps.marketdataservice.marketdatatypes.Consumer;
import da.refapps.marketdataservice.marketdatatypes.InstrumentId;
import da.refapps.marketdataservice.marketdatatypes.ObservationReference;
import da.refapps.marketdataservice.marketdatatypes.Publisher;
import da.refapps.marketdataservice.roles.OperatorRole;
import java.time.LocalDate;
import java.util.Collections;
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
            + "   \"templateId\": \"31797e58e2941179b7be4f1ab5661948f684273217a67036a9b6bd2ca92a2012:DA.RefApps.MarketDataService.Roles:OperatorRole\""
            + "\n"
            + "}";
    CreatedEvent expected =
        new CreatedEvent(OperatorRole.TEMPLATE_ID, "#14:1", new OperatorRole("Operator"));

    Genson deserializer = getClassDeserializer();

    CreatedEvent actual = deserializer.deserialize(json, getDeserializeClass());
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
    actual = deserializer.deserialize(json, getDeserializeClass());
    assertEquals(expected, actual);
  }

  @Test
  public void deserializeInstrumentId() {
    String json = "{ \"unpack\": \"ISIN 288 2839\" }";

    Genson deserializer = getClassDeserializer();

    InstrumentId actual = deserializer.deserialize(json, InstrumentId.class);
    assertThat(actual, is(new InstrumentId("ISIN 288 2839")));
  }

  @Test
  public void deserializeIdentifier() {
    String json =
        "\"31797e58e2941179b7be4f1ab5661948f684273217a67036a9b6bd2ca92a2012:DA.RefApps.MarketDataService.DataStream:EmptyDataStream\"";

    Genson deserializer = getClassDeserializer();

    Identifier actual = deserializer.deserialize(json, Identifier.class);
    assertThat(
        actual,
        is(
            new Identifier(
                "31797e58e2941179b7be4f1ab5661948f684273217a67036a9b6bd2ca92a2012",
                "DA.RefApps.MarketDataService.DataStream",
                "EmptyDataStream")));
  }

  @Override
  protected Class<CreatedEvent> getDeserializeClass() {
    return CreatedEvent.class;
  }

  @Override
  protected Genson getClassDeserializer() {
    return new GensonBuilder()
        .useConstructorWithArguments(true)
        .withDeserializer(new IdentifierDeserializer(), Identifier.class)
        .create();
  }
}
