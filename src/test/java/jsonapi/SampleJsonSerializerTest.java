/*
 * Copyright (c) 2019, Digital Asset (Switzerland) GmbH and/or its affiliates. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package jsonapi;

import com.daml.ledger.javaapi.data.ExerciseCommand;
import da.timeservice.timeservice.TimeManager;
import jsonapi.json.SampleJsonSerializer;
import org.junit.*;

public class SampleJsonSerializerTest {

  SampleJsonSerializer sampleJsonSerializer = new SampleJsonSerializer();

  @Test
  public void SerializeExerciseAdvanceCurrentTime() {
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
}
