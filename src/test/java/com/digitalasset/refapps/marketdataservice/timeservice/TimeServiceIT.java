/*
 * Copyright (c) 2019, Digital Asset (Switzerland) GmbH and/or its affiliates. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package com.digitalasset.refapps.marketdataservice.timeservice;

import static com.digitalasset.refapps.utils.EventuallyUtil.eventually;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import com.daml.ledger.javaapi.data.Party;
import com.digitalasset.jsonapi.JsonApi;
import com.digitalasset.jsonapi.LedgerClient;
import com.digitalasset.refapps.marketdataservice.AppConfig;
import com.digitalasset.refapps.marketdataservice.extensions.RelTime;
import com.digitalasset.testing.junit4.Sandbox;
import com.digitalasset.testing.ledger.DefaultLedgerAdapter;
import com.digitalasset.testing.utils.ContractWithId;
import com.google.protobuf.InvalidProtocolBufferException;
import da.timeservice.timeservice.CurrentTime;
import da.timeservice.timeservice.TimeConfiguration;
import da.timeservice.timeservice.TimeManager;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.Instant;
import java.util.Collections;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import org.junit.After;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExternalResource;
import org.junit.rules.RuleChain;
import org.junit.rules.TestRule;

public class TimeServiceIT {

  private static final Path RELATIVE_DAR_PATH = Paths.get("target/market-data-service.dar");
  private static final Party OPERATOR = new Party("Operator");
  private static final String APPLICATION_ID = "market-data-service";

  private static final Sandbox sandbox =
      Sandbox.builder()
          .dar(RELATIVE_DAR_PATH)
          .parties(OPERATOR.getValue())
          .useWallclockTime()
          .build();
  @ClassRule public static ExternalResource startSandbox = sandbox.getClassRule();

  @Rule
  public final TestRule processes =
      RuleChain.outerRule(sandbox.getRule()).around(new JsonApi(sandbox::getSandboxPort));

  private ScheduledExecutorService scheduler;
  private DefaultLedgerAdapter ledger;
  private LedgerClient ledgerClient;

  @Before
  public void setup() {
    ledger = sandbox.getLedgerAdapter();
    String ledgerId = sandbox.getClient().getLedgerId();
    scheduler = Executors.newScheduledThreadPool(1);
    AppConfig appConfig =
        AppConfig.builder()
            .setLedgerId(ledgerId)
            .setApplicationId(APPLICATION_ID)
            .setJsonApiHost("localhost")
            .setJsonApiPort(7575)
            .create();
    ledgerClient = appConfig.getClientFor(OPERATOR.getValue());
  }

  @After
  public void tearDown() {
    scheduler.shutdown();
  }

  private Instant getCurrentTimeInstant() {
    ContractWithId<CurrentTime.ContractId> currentTimeCid =
        ledger.getMatchedContract(OPERATOR, CurrentTime.TEMPLATE_ID, CurrentTime.ContractId::new);
    return CurrentTime.fromValue(currentTimeCid.record).currentTime;
  }

  private TimeManager.ContractId setupTimeServiceContracts(
      Instant initialTime, Duration modelPeriodTime) throws InvalidProtocolBufferException {
    CurrentTime currentTime =
        new CurrentTime(OPERATOR.getValue(), initialTime, Collections.emptyList());
    TimeConfiguration timeConfiguration =
        new TimeConfiguration(OPERATOR.getValue(), false, RelTime.fromDuration(modelPeriodTime));
    TimeManager timeManager = new TimeManager(OPERATOR.getValue());
    ledger.createContract(OPERATOR, CurrentTime.TEMPLATE_ID, currentTime.toValue());
    ledger.createContract(OPERATOR, TimeConfiguration.TEMPLATE_ID, timeConfiguration.toValue());
    ledger.createContract(OPERATOR, TimeManager.TEMPLATE_ID, timeManager.toValue());
    return ledger.getCreatedContractId(
        OPERATOR, TimeManager.TEMPLATE_ID, TimeManager.ContractId::new);
  }

  private void changeModelPeriodTime(
      TimeManager.ContractId timeManagerCid, Duration newModelPeriodTime)
      throws InvalidProtocolBufferException {
    ledger.exerciseChoice(
        OPERATOR,
        timeManagerCid.exerciseSetModelPeriodTime(RelTime.fromDuration(newModelPeriodTime)));
  }

  private void verifyModelPeriodTime(Duration newModelPeriodTime) throws InterruptedException {
    eventually(
        () -> {
          Instant time1 = getCurrentTimeInstant();
          Instant time2 = getCurrentTimeInstant();
          assertThat(time2, is(time1.plus(newModelPeriodTime)));
        });
  }

  @Test
  public void modelPeriodTimeCanBeChanged()
      throws InvalidProtocolBufferException, InterruptedException {
    Instant initialTime = Instant.parse("1955-11-12T10:04:00Z");
    Duration modelPeriodTime = Duration.ofHours(2);
    TimeManager.ContractId managerCid = setupTimeServiceContracts(initialTime, modelPeriodTime);

    TimeUpdaterBot timeUpdaterBot = new TimeUpdaterBot(ledgerClient);
    TimeUpdaterBotExecutor botExecutor = new TimeUpdaterBotExecutor(scheduler);
    botExecutor.start(timeUpdaterBot, Duration.ofSeconds(1));
    ledger.exerciseChoice(OPERATOR, managerCid.exerciseContinue());

    Duration newModelPeriodTime = modelPeriodTime.plusHours(3);
    changeModelPeriodTime(managerCid, newModelPeriodTime);

    verifyModelPeriodTime(newModelPeriodTime);
  }

  @Test
  public void updateTime() throws InterruptedException, InvalidProtocolBufferException {
    Instant initialTime = Instant.parse("1955-11-12T10:04:00Z");
    Duration modelPeriodTime = Duration.ofHours(2);
    TimeManager.ContractId managerCid = setupTimeServiceContracts(initialTime, modelPeriodTime);

    TimeUpdaterBot timeUpdaterBot = new TimeUpdaterBot(ledgerClient);
    TimeUpdaterBotExecutor botExecutor = new TimeUpdaterBotExecutor(scheduler);
    botExecutor.start(timeUpdaterBot, Duration.ofSeconds(1));
    ledger.exerciseChoice(OPERATOR, managerCid.exerciseContinue());

    verifyModelPeriodTime(modelPeriodTime);
  }
}
