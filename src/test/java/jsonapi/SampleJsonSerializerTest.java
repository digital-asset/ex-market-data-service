/*
 * Copyright (c) 2019, Digital Asset (Switzerland) GmbH and/or its affiliates. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package jsonapi;

import com.daml.ledger.javaapi.data.Date;
import com.daml.ledger.javaapi.data.ExerciseCommand;
import com.daml.ledger.javaapi.data.Numeric;
import com.daml.ledger.javaapi.data.Party;
import com.daml.ledger.javaapi.data.Record;
import com.daml.ledger.javaapi.data.Text;
import com.daml.ledger.javaapi.data.Timestamp;
import com.daml.ledger.javaapi.data.Variant;
import da.refapps.marketdataservice.datastream.EmptyDataStream;
import da.refapps.marketdataservice.marketdatatypes.InstrumentId;
import da.refapps.marketdataservice.marketdatatypes.Observation;
import da.refapps.marketdataservice.marketdatatypes.ObservationReference;
import da.refapps.marketdataservice.marketdatatypes.ObservationValue;
import da.refapps.marketdataservice.marketdatatypes.observationvalue.CleanPrice;
import da.timeservice.timeservice.CurrentTime;
import da.timeservice.timeservice.TimeManager;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Collections;
import jsonapi.gson.SampleJsonSerializer;
import org.junit.Assert;
import org.junit.Test;

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
  public void serializeExerciseStartDataStream() {
    Observation observation = getObservation();
    EmptyDataStream.ContractId contractId = new EmptyDataStream.ContractId("cid1");
    ExerciseCommand exerciseCommand = contractId.exerciseStartDataStream(observation);
    // Real examples:
    // "observation":{
    //      "label":{"market":"US Bond Market",
    //      "instrumentId":{"unpack":"ISIN-288-2839"},
    //      "maturityDate":"2021-02-11"},
    //      "time":"2019-11-12T12:30:00Z",
    //      "value":{"tag":"CleanPrice","value":{"clean":"1.0"}}}}
    // "observation":{
    //      "label":{"market":"US Bond Market",
    //      "instrumentId":{"unpack":"ISIN-288-2839"},
    //      "maturityDate":"2021-02-11"},
    //      "time":"2019-11-12T12:30:00Z",
    //      "value":{
    //            "tag":"EnrichedCleanDirtyPrice",
    //            "value":{"rate":"0.02","couponDate":"2020-02-11","dirty":"1.0150136986",
    //                        "clean":"1.0","accrual":"0.0150136986"}}}}
    String expected =
        "{\"templateId\":\"b4eb9b86bb78db2acde90edf0a03d96e5d65cc7a7cc422f23b6d98a286e07c09:DA.RefApps.MarketDataService.DataStream:EmptyDataStream\",\"contractId\":\"cid1\",\"choice\":\"StartDataStream\",\"argument\":{\"newObservation\":"
            + "{\"label\":{\"market\":\"Market\",\"instrumentId\":{\"unpack\":\"ISIN 123 XYZ\"},\"maturityDate\":\"2019-05-10\"},\"time\":\"2019-05-03T09:15:30Z\",\"value\":{\"tag\":\"CleanPrice\",\"value\":{\"clean\":\"1.2\"}}}}}";
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

  @Test
  public void serializeParty() {
    Party party = new Party("Operator");
    Assert.assertEquals("\"Operator\"", sampleJsonSerializer.apply(party));
  }

  @Test
  public void serializeDate() {
    Date date = getDate();
    Assert.assertEquals("\"2020-02-08\"", sampleJsonSerializer.apply(date));
  }

  @Test
  public void serializeTimestamp() {
    Timestamp timestamp = Timestamp.fromInstant(Instant.parse("2020-02-08T12:30:00Z"));
    Assert.assertEquals("\"2020-02-08T12:30:00Z\"", sampleJsonSerializer.apply(timestamp));
  }

  @Test
  public void serializeVariant() {
    Variant variant = new Variant("VariantType", new Text("SomeValue"));
    Assert.assertEquals(
        "{\"tag\":\"VariantType\",\"value\":\"SomeValue\"}", sampleJsonSerializer.apply(variant));
  }

  @Test
  public void serializeRecord() {
    Record record = new Record(new Record.Field("newObserver", new Party("John Doe")));
    Assert.assertEquals("{\"newObserver\":\"John Doe\"}", sampleJsonSerializer.apply(record));
  }

  @Test
  public void serializeNumeric() {
    Numeric numeric = new Numeric(BigDecimal.valueOf(12.3));
    Assert.assertEquals("\"12.3\"", sampleJsonSerializer.apply(numeric));
  }

  private Date getDate() {
    Instant instant = Instant.parse("2020-02-08T00:00:00Z");
    int epochDay = (int)instant.getEpochSecond() / 3600 / 24;
    Date date = new Date(epochDay);
    Assert.assertEquals(LocalDate.parse("2020-02-08"), date.getValue());
    return date;
  }

  private Observation getObservation() {
    Instant observationTime = Instant.parse("2019-05-03T10:15:30.00Z");
    String marketName = "Market";
    InstrumentId instrumentId = new InstrumentId("ISIN 123 XYZ");
    LocalDate maturityDate = LocalDate.parse("2019-05-10");
    ObservationReference label = new ObservationReference(marketName, instrumentId, maturityDate);
    ObservationValue cleanPrice = new CleanPrice(BigDecimal.valueOf(1.2));
    return new Observation(label, observationTime.minusSeconds(3600), cleanPrice);
  }
}
