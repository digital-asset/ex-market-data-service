/*
 * Copyright (c) 2019, Digital Asset (Switzerland) GmbH and/or its affiliates. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package jsonapi;

import da.refapps.marketdataservice.datastream.EmptyDataStream;
import da.refapps.marketdataservice.marketdatatypes.InstrumentId;
import da.refapps.marketdataservice.marketdatatypes.ObservationReference;
import da.refapps.marketdataservice.marketdatatypes.Publisher;
import da.timeservice.timeservice.CurrentTime;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Collections;
import jsonapi.json.GsonRegisteredAllDeserializers;
import org.junit.Assert;
import org.junit.Test;

public class JsonDeserializerTest {

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
    final String operator = "Operator1";
    final ObservationReference reference =
        new ObservationReference(
            "Market1", new InstrumentId("InstrumentId1"), LocalDate.parse("2020-02-08"));
    final Publisher publisher = new Publisher("Publisher1");
    EmptyDataStream expectedEmptyDataStream =
        new EmptyDataStream(operator, reference, Collections.emptyList(), publisher);
    String serializedEmptyDataStream =
        "{\"operator\":\"Operator1\",\"reference\":{\"market\":\"Market1\",\"instrumentId\":{\"unpack\":\"InstrumentId1\"},\"maturityDate\":{\"year\":2020,\"month\":2,\"day\":8}},\"consumers\":[],\"publisher\":{\"party\":\"Publisher1\"}}";
    EmptyDataStream deserializedEmptyDataStream =
        GsonRegisteredAllDeserializers.gson()
            .fromJson(serializedEmptyDataStream, EmptyDataStream.class);
    Assert.assertEquals(expectedEmptyDataStream, deserializedEmptyDataStream);
  }
}
