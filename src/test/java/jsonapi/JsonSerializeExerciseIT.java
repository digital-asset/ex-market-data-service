/*
 * Copyright (c) 2019, Digital Asset (Switzerland) GmbH and/or its affiliates. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package jsonapi;

import com.daml.ledger.javaapi.data.ExerciseCommand;
import com.daml.ledger.javaapi.data.Identifier;
import com.daml.ledger.javaapi.data.Party;
import com.daml.ledger.javaapi.data.Record;
import com.digitalasset.refapps.marketdataservice.extensions.RelTime;
import com.digitalasset.testing.junit4.Sandbox;
import com.digitalasset.testing.ledger.DefaultLedgerAdapter;
import com.digitalasset.testing.utils.ContractWithId;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.protobuf.InvalidProtocolBufferException;
import da.timeservice.timeservice.CurrentTime;
import da.timeservice.timeservice.TimeConfiguration;
import da.timeservice.timeservice.TimeManager;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.Instant;
import java.util.Collections;
import jsonapi.gson.ExerciseCommandSerializer;
import jsonapi.gson.IdentifierSerializer;
import jsonapi.gson.RecordSerializer;
import org.junit.*;
import org.junit.rules.ExternalResource;
import org.junit.rules.RuleChain;
import org.junit.rules.TestRule;

public class JsonSerializeExerciseIT {
  private static final Path RELATIVE_DAR_PATH = Paths.get("target/market-data-service.dar");
  private static final Party OPERATOR = new Party("Operator");

  private static final Sandbox sandbox =
      Sandbox.builder()
          .dar(RELATIVE_DAR_PATH)
          .useWallclockTime()
          .parties(OPERATOR.getValue())
          .build();

  @ClassRule public static ExternalResource startSandbox = sandbox.getClassRule();

  @Rule
  public TestRule processes =
      RuleChain.outerRule(sandbox.getRule()).around(new JsonApi(sandbox::getSandboxPort));

  private DefaultLedgerAdapter ledger;

  @Before
  public void setUp() {
    ledger = sandbox.getLedgerAdapter();
  }

  @Test
  public void AdvanceCurrentTimeIsCompletedByTheLedger() throws IOException {
    Instant startTime = Instant.parse("2020-02-04T22:57:29Z");
    Duration modelPeriodTime = Duration.ofHours(1);
    setupTimeContracts(startTime, modelPeriodTime);
    ContractWithId<TimeManager.ContractId> timeManagerWithId =
        ledger.getMatchedContract(OPERATOR, TimeManager.TEMPLATE_ID, TimeManager.ContractId::new);
    JsonLedgerClient jsonLedgerClient =
        new JsonLedgerClient(sandbox.getClient().getLedgerId(), this::toJson);
    jsonLedgerClient.exerciseChoice(timeManagerWithId.contractId.exerciseAdvanceCurrentTime());
    getNextCurrentTime();
    CurrentTime currentTime = getNextCurrentTime();
    Assert.assertEquals(startTime.plus(modelPeriodTime), currentTime.currentTime);
  }

  private CurrentTime getNextCurrentTime() {
    ContractWithId<CurrentTime.ContractId> currentTimeWithId =
        ledger.getMatchedContract(OPERATOR, CurrentTime.TEMPLATE_ID, CurrentTime.ContractId::new);
    return CurrentTime.fromValue(currentTimeWithId.record);
  }

  private void setupTimeContracts(Instant startTime, Duration modelPeriodTime)
      throws InvalidProtocolBufferException {
    CurrentTime currentTime =
        new CurrentTime(OPERATOR.getValue(), startTime, Collections.emptyList());
    ledger.createContract(OPERATOR, CurrentTime.TEMPLATE_ID, currentTime.toValue());
    TimeConfiguration timeConfiguration =
        new TimeConfiguration(OPERATOR.getValue(), true, RelTime.fromDuration(modelPeriodTime));
    ledger.createContract(OPERATOR, TimeConfiguration.TEMPLATE_ID, timeConfiguration.toValue());
    TimeManager timeManager = new TimeManager(OPERATOR.getValue());
    ledger.createContract(OPERATOR, TimeManager.TEMPLATE_ID, timeManager.toValue());
  }

  private String toJson(Object o) {
    final Gson gson =
        new GsonBuilder()
            .registerTypeAdapter(ExerciseCommand.class, new ExerciseCommandSerializer())
            .registerTypeAdapter(Identifier.class, new IdentifierSerializer())
            .registerTypeAdapter(Record.class, new RecordSerializer())
            .create();
    return gson.toJson(o);
  }
}
