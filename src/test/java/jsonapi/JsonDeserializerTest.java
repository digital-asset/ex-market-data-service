/*
 * Copyright (c) 2019, Digital Asset (Switzerland) GmbH and/or its affiliates. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package jsonapi;

import com.google.common.collect.Iterables;
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
import java.math.BigInteger;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Collections;
import jsonapi.events.CreatedEvent;
import jsonapi.gson.GsonRegisteredAllDeserializers;
import jsonapi.gson.GsonSerializer;
import jsonapi.http.CreatedEventHolder;
import jsonapi.http.HttpResponse;
import org.junit.Assert;
import org.junit.Test;

public class JsonDeserializerTest {

  private static final String OPERATOR = "Operator1";
  private static final ObservationReference REFERENCE =
      new ObservationReference(
          "Market1", new InstrumentId("InstrumentId1"), LocalDate.parse("2020-02-08"));
  private static final Publisher PUBLISHER = new Publisher("Publisher1");
  private static final ObservationValue OBSERVATION_VALUE_1 =
      new CleanPrice(new BigDecimal(BigInteger.TEN));

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
        GsonRegisteredAllDeserializers.gson().fromJson(serializedCurrentTime, CurrentTime.class);
    Assert.assertEquals(expectedCurrentTime, deserializedCurrentTime);
  }

  @Test
  public void deserializeEmptyDataStream() {
    EmptyDataStream expectedEmptyDataStream =
        new EmptyDataStream(OPERATOR, REFERENCE, Collections.emptyList(), PUBLISHER);
    String serializedEmptyDataStream =
        "{\"operator\":\"Operator1\",\"reference\":{\"market\":\"Market1\",\"instrumentId\":{\"unpack\":\"InstrumentId1\"},\"maturityDate\":{\"year\":2020,\"month\":2,\"day\":8}},\"consumers\":[],\"publisher\":{\"party\":\"Publisher1\"}}";
    EmptyDataStream deserializedEmptyDataStream =
        GsonRegisteredAllDeserializers.gson()
            .fromJson(serializedEmptyDataStream, EmptyDataStream.class);
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
        "{\"observation\":{\"label\":{\"market\":\"Market1\",\"instrumentId\":{\"unpack\":\"InstrumentId1\"},\"maturityDate\":{\"year\":2020,\"month\":2,\"day\":8}},\"time\":\"2020-01-03T10:15:30Z\",\"value\":{\"clean\":1}},\"consumers\":[],\"publisher\":{\"party\":\"Publisher1\"},\"published\":\"2020-01-03T10:15:30Z\",\"operator\":\"Operator1\",\"lastUpdated\":\"2020-01-03T10:15:30Z\"}";
    DataStream deserializedDataStream =
        GsonRegisteredAllDeserializers.gson().fromJson(serializedDataStream, DataStream.class);
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
    CreatedEvent expectedCreatedEvent = new CreatedEvent(null, null, new OperatorRole("Operator"));
    HttpResponse deserializedHttpResponse =
        GsonRegisteredAllDeserializers.gson().fromJson(serializedHttpResponse, HttpResponse.class);
    HttpResponse.ExerciseResult result =
        (HttpResponse.ExerciseResult) deserializedHttpResponse.getResult();
    CreatedEventHolder deserializedCreatedEventHolder =
        (CreatedEventHolder) Iterables.getOnlyElement(result.getEvents());
    Assert.assertEquals(
        expectedCreatedEvent.getPayload(), deserializedCreatedEventHolder.event().getPayload());
  }
}
