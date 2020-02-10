/*
 * Copyright (c) 2019, Digital Asset (Switzerland) GmbH and/or its affiliates. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package jsonapi;

import com.daml.ledger.javaapi.data.ExerciseCommand;
import com.daml.ledger.javaapi.data.Party;
import da.refapps.marketdataservice.datastream.EmptyDataStream;
import da.refapps.marketdataservice.marketdatatypes.InstrumentId;
import da.refapps.marketdataservice.marketdatatypes.Observation;
import da.refapps.marketdataservice.marketdatatypes.ObservationReference;
import da.refapps.marketdataservice.marketdatatypes.ObservationValue;
import da.refapps.marketdataservice.marketdatatypes.observationvalue.DirtyPrice;
import da.timeservice.timeservice.CurrentTime;
import da.timeservice.timeservice.TimeManager;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
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

  @Ignore
  @Test
  public void serializeExerciseStartDataStream() {
    Observation observation = getObservation();

    EmptyDataStream.ContractId contractId = new EmptyDataStream.ContractId("cid1");
    ExerciseCommand exerciseCommand = contractId.exerciseStartDataStream(observation);
    System.err.println(sampleJsonSerializer.apply(exerciseCommand));
    String expected = "";
    Assert.assertEquals(expected, sampleJsonSerializer.apply(exerciseCommand));
  }

  private Observation getObservation() {
    Instant observationTime = Instant.parse("2019-05-03T10:15:30.00Z");
    String marketName = "Market";
    InstrumentId instrumentId = new InstrumentId("ISIN 123 XYZ");
    LocalDate maturityDate = LocalDate.now().plusWeeks(1);
    ObservationReference label = new ObservationReference(marketName, instrumentId, maturityDate);
    ObservationValue dirtyPrice = new DirtyPrice(BigDecimal.valueOf(10));
    return new Observation(label, observationTime.minusSeconds(3600), dirtyPrice);
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

  @Test
  public void serializeParty() {
    Party party = new Party("Operator");
    Assert.assertEquals("\"Operator\"", sampleJsonSerializer.apply(party));
  }
}
