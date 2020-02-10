/*
 * Copyright (c) 2019, Digital Asset (Switzerland) GmbH and/or its affiliates. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package jsonapi;

import da.refapps.marketdataservice.datastream.DataStream;
import da.refapps.marketdataservice.datastream.EmptyDataStream;
import da.refapps.marketdataservice.marketdatatypes.*;
import da.refapps.marketdataservice.marketdatatypes.observationvalue.CleanPrice;
import da.timeservice.timeservice.CurrentTime;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Collections;
import jsonapi.json.GsonRegisteredAllDeserializers;
import jsonapi.json.SampleJsonSerializer;
import org.junit.Assert;
import org.junit.Ignore;
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

  @Ignore // TODO: deserialize ObservationValue
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
    System.err.println(new SampleJsonSerializer().apply(expectedDataStream));
    String serializedDataStream =
        "{\"observation\":{\"label\":{\"market\":\"Market1\",\"instrumentId\":{\"unpack\":\"InstrumentId1\"},\"maturityDate\":{\"year\":2020,\"month\":2,\"day\":8}},\"time\":\"2020-01-03T10:15:30Z\",\"value\":{\"clean\":1}},\"consumers\":[],\"publisher\":{\"party\":\"Publisher1\"},\"published\":\"2020-01-03T10:15:30Z\",\"operator\":\"Operator1\",\"lastUpdated\":\"2020-01-03T10:15:30Z\"}";
    DataStream deserializedDataStream =
        GsonRegisteredAllDeserializers.gson().fromJson(serializedDataStream, DataStream.class);
    Assert.assertEquals(expectedDataStream, deserializedDataStream);
  }
}
