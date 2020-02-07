/*
 * Copyright (c) 2019, Digital Asset (Switzerland) GmbH and/or its affiliates. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package jsonapi;

import com.daml.ledger.javaapi.data.ExerciseCommand;
import com.daml.ledger.javaapi.data.Party;
import da.timeservice.timeservice.CurrentTime;
import da.timeservice.timeservice.TimeManager;
import java.time.Instant;
import java.util.Collections;
import jsonapi.json.SampleJsonSerializer;
import org.junit.*;

public class SampleJsonSerializerTest {

  private static final Party OPERATOR = new Party("Operator");
  private final SampleJsonSerializer sampleJsonSerializer = new SampleJsonSerializer();

  @Test
  public void serializeExerciseAdvanceCurrentTime() {
    TimeManager.ContractId contractId = new TimeManager.ContractId("cid1");
    ExerciseCommand exerciseCommand = contractId.exerciseAdvanceCurrentTime();
    String expected =
        String.format(
            "{\"templateId\":\"%s:%s:%s\",\"contractId\":\"cid1\",\"choice\":\"AdvanceCurrentTime\",\"argument\":{}}",
            TimeManager.TEMPLATE_ID.getPackageId(),
            TimeManager.TEMPLATE_ID.getModuleName(),
            TimeManager.TEMPLATE_ID.getEntityName());
    Assert.assertEquals(expected, sampleJsonSerializer.apply(exerciseCommand));
  }

  @Test
  public void serializeCurrentTime() {
    CurrentTime currentTime =
        new CurrentTime(
            OPERATOR.getValue(), Instant.parse("2020-02-04T22:57:29Z"), Collections.emptyList());
    Assert.assertEquals(
        "{\"operator\":\"Operator\",\"currentTime\":\"2020-02-04T22:57:29Z\",\"observers\":[]}",
        sampleJsonSerializer.apply(currentTime));
  }
}
