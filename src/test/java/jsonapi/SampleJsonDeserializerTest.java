/*
 * Copyright (c) 2019, Digital Asset (Switzerland) GmbH and/or its affiliates. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package jsonapi;

import com.daml.ledger.javaapi.data.Party;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import da.timeservice.timeservice.CurrentTime;
import jsonapi.gson.*;
import jsonapi.json.JsonDeserializer;
import jsonapi.json.SampleCurrentTimeDeserializer;
import org.junit.Assert;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.time.Instant;
import java.util.Collections;

public class SampleJsonDeserializerTest {

  private static final Party OPERATOR = new Party("Operator");

  @Test
  public void deserializeCurrentTime() {
    String time = "2020-02-04T22:57:29Z";
    String operator = "Operator";
    String serializedCurrentTime = String.format("{\"operator\":\"%s\",\"currentTime\":\"%s\",\"observers\":[]}", operator, time);
    CurrentTime expectedCurrentTime =
        new CurrentTime(
            operator, Instant.parse(time), Collections.emptyList());
    SampleCurrentTimeDeserializer deserializer = new SampleCurrentTimeDeserializer();
    InputStream is = new ByteArrayInputStream(serializedCurrentTime.getBytes());
    CurrentTime deserializedCurrentTime = deserializer.apply(is);
    Assert.assertEquals(deserializedCurrentTime, expectedCurrentTime);
  }
}
