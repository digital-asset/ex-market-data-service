/*
 * Copyright (c) 2019, Digital Asset (Switzerland) GmbH and/or its affiliates. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package com.daml.extensions.jsonapi.gson;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import com.daml.ledger.javaapi.data.Date;
import com.daml.ledger.javaapi.data.Identifier;
import com.daml.extensions.jsonapi.events.CreatedEvent;
import com.daml.extensions.jsonapi.events.Event;
import com.daml.extensions.jsonapi.http.CreatedEventHolder;
import com.daml.extensions.jsonapi.http.EventHolder;
import com.daml.extensions.jsonapi.http.HttpResponse;
import com.daml.extensions.jsonapi.http.HttpResponse.Result;
import com.daml.extensions.jsonapi.http.HttpResponse.SearchResult;
import com.google.common.collect.Iterables;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import da.refapps.marketdataservice.datasource.DataSource;
import da.refapps.marketdataservice.datastream.DataStream;
import da.refapps.marketdataservice.datastream.EmptyDataStream;
import da.refapps.marketdataservice.marketdatatypes.InstrumentId;
import da.refapps.marketdataservice.marketdatatypes.Observation;
import da.refapps.marketdataservice.marketdatatypes.ObservationReference;
import da.refapps.marketdataservice.marketdatatypes.ObservationValue;
import da.refapps.marketdataservice.marketdatatypes.Publisher;
import da.refapps.marketdataservice.marketdatatypes.observationvalue.CleanPrice;
import da.refapps.marketdataservice.roles.OperatorRole;
import da.timeservice.timeservice.CurrentTime;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Collections;
import org.junit.Assert;
import org.junit.Test;

// TODO: ERA-745
public class JsonDeserializerTest {

  private static final String OPERATOR = "Operator1";
  private static final ObservationReference REFERENCE =
      new ObservationReference(
          "Market1", new InstrumentId("InstrumentId1"), LocalDate.parse("2020-02-08"));
  private static final Publisher PUBLISHER = new Publisher("Publisher1");

  @Test
  public void deserializeCurrentTime() {
    String time = "2020-02-04T22:57:29Z";
    String operator = "Operator";
    String serializedCurrentTime =
        String.format(
            "{\"operator\":\"%s\",\"currentTime\":\"%s\",\"observers\":[]}", operator, time);
    CurrentTime expectedCurrentTime =
        new CurrentTime(operator, Instant.parse(time), Collections.emptyList());
    CurrentTime deserializedCurrentTime =
        getDeserializer().fromJson(serializedCurrentTime, CurrentTime.class);
    Assert.assertEquals(expectedCurrentTime, deserializedCurrentTime);
  }

  @Test
  public void deserializeEmptyDataStream() {
    EmptyDataStream expectedEmptyDataStream =
        new EmptyDataStream(OPERATOR, REFERENCE, Collections.emptyList(), PUBLISHER);
    String serializedEmptyDataStream =
        "{ \n"
            + "   \"operator\":\"Operator1\",\n"
            + "   \"reference\":{ \n"
            + "      \"market\":\"Market1\",\n"
            + "      \"instrumentId\":{ \n"
            + "         \"unpack\":\"InstrumentId1\"\n"
            + "      },\n"
            + "      \"maturityDate\":\"2020-02-08\"\n"
            + "   },\n"
            + "   \"consumers\":[],\n"
            + "   \"publisher\":{ \n"
            + "      \"party\":\"Publisher1\"\n"
            + "   }\n"
            + "}";
    EmptyDataStream deserializedEmptyDataStream =
        getDeserializer().fromJson(serializedEmptyDataStream, EmptyDataStream.class);
    Assert.assertEquals(expectedEmptyDataStream, deserializedEmptyDataStream);
  }

  @Test
  public void deserializeDataStream() {
    Instant now = Instant.parse("2020-01-03T10:15:30.00Z");
    DataStream expectedDataStream =
        new DataStream(
            new Observation(REFERENCE, now, new CleanPrice(BigDecimal.ONE)),
            Collections.emptyList(),
            PUBLISHER,
            now,
            OPERATOR,
            now);
    String serializedDataStream =
        "{ \n"
            + "   \"observation\":{ \n"
            + "      \"label\":{ \n"
            + "         \"market\":\"Market1\",\n"
            + "         \"instrumentId\":{ \n"
            + "            \"unpack\":\"InstrumentId1\"\n"
            + "         },\n"
            + "         \"maturityDate\":\"2020-02-08\"\n"
            + "      },\n"
            + "      \"time\":\"2020-01-03T10:15:30Z\",\n"
            + "      \"value\":{ \n"
            + "         \"tag\":\"CleanPrice\",\n"
            + "         \"value\":{ \n"
            + "            \"clean\":\"1\"\n"
            + "         }\n"
            + "      }\n"
            + "   },\n"
            + "   \"consumers\":[],\n"
            + "   \"publisher\":{ \n"
            + "      \"party\":\"Publisher1\"\n"
            + "   },\n"
            + "   \"published\":\"2020-01-03T10:15:30Z\",\n"
            + "   \"operator\":\"Operator1\",\n"
            + "   \"lastUpdated\":\"2020-01-03T10:15:30Z\"\n"
            + "}";
    DataStream deserializedDataStream =
        getDeserializer().fromJson(serializedDataStream, DataStream.class);
    Assert.assertEquals(expectedDataStream, deserializedDataStream);
  }

  @Test
  public void deserializeExerciseHttpResponseWithoutArchivedEvent() {
    String serializedHttpResponse =
        "{ \n"
            + "   \"result\":{ \n"
            + "      \"exerciseResult\":\"#14:1\",\n"
            + "      \"events\":[ \n"
            + "         { \n"
            + "            \"created\":{ \n"
            + "               \"payload\":{ \n"
            + "                  \"operator\":\"Operator\"\n"
            + "               },\n"
            + "               \"contractId\":\"#14:1\",\n"
            + "               \"templateId\":"
            + new GsonSerializer().apply(OperatorRole.TEMPLATE_ID)
            + "\n"
            + "            }\n"
            + "         }\n"
            + "      ]\n"
            + "   }\n"
            + "}";
    CreatedEvent expectedCreatedEvent =
        new CreatedEvent(OperatorRole.TEMPLATE_ID, "#14:1", new OperatorRole("Operator"));
    HttpResponse deserializedHttpResponse =
        getDeserializer().fromJson(serializedHttpResponse, HttpResponse.class);
    HttpResponse.ExerciseResult result =
        (HttpResponse.ExerciseResult) deserializedHttpResponse.getResult();
    CreatedEventHolder deserializedCreatedEventHolder =
        (CreatedEventHolder) Iterables.getOnlyElement(result.getEvents());
    Assert.assertEquals(expectedCreatedEvent, deserializedCreatedEventHolder.event());
  }

  @Test
  public void deserializeDataSource() {
    String json =
        "{\n"
            + "  \"observers\": [],\n"
            + "  \"reference\": {\n"
            + "    \"market\": \"European Bond Market\",\n"
            + "    \"instrumentId\": {\n"
            + "      \"unpack\": \"ISIN 123 1244\"\n"
            + "    },\n"
            + "    \"maturityDate\": \"2021-03-20\"\n"
            + "  },\n"
            + "  \"path\": \"default-1000.csv\",\n"
            + "  \"operator\": \"Operator\",\n"
            + "  \"owner\": \"MarketDataProvider2\"\n"
            + "}";

    Gson deserializer = getDeserializer();
    DataSource result = deserializer.fromJson(json, DataSource.class);

    assertThat(
        result,
        is(
            new DataSource(
                "MarketDataProvider2",
                "Operator",
                Collections.emptyList(),
                new ObservationReference(
                    "European Bond Market",
                    new InstrumentId("ISIN 123 1244"),
                    LocalDate.of(2021, 3, 20)),
                "default-1000.csv")));
  }

  private Gson getDeserializer() {
    return new GsonBuilder()
        .registerTypeAdapter(Instant.class, new InstantDeserializer())
        .registerTypeAdapter(ObservationValue.class, new ObservationValueDeserializer())
        .registerTypeAdapter(Identifier.class, new IdentifierDeserializer())
        .registerTypeAdapter(Date.class, new DateDeserializer())
        .registerTypeAdapter(LocalDate.class, new LocalDateDeserializer())
        .registerTypeAdapter(Event.class, new EventDeserializer())
        .registerTypeAdapter(CreatedEvent.class, new CreatedEventDeserializer())
        .registerTypeAdapter(EventHolder.class, new EventHolderDeserializer())
        .registerTypeAdapter(Result.class, new ResultDeserializer())
        .registerTypeAdapter(SearchResult.class, new SearchResultDeserializer())
        .create();
  }
}
