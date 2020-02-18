/*
 * Copyright (c) 2019, Digital Asset (Switzerland) GmbH and/or its affiliates. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package com.digitalasset.refapps.marketdataservice.timeservice;

import static com.digitalasset.refapps.marketdataservice.utils.AppParties.ALL_PARTIES;
import static com.digitalasset.refapps.utils.EventuallyUtil.eventually;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import com.daml.ledger.javaapi.data.Party;
import com.digitalasset.refapps.marketdataservice.Main;
import com.digitalasset.refapps.marketdataservice.extensions.RelTime;
import com.digitalasset.refapps.marketdataservice.utils.AppParties;
import com.digitalasset.refapps.utils.DamlScript;
import com.digitalasset.testing.junit4.Sandbox;
import com.digitalasset.testing.ledger.DefaultLedgerAdapter;
import com.digitalasset.testing.utils.ContractWithId;
import com.google.protobuf.InvalidProtocolBufferException;
import da.timeservice.timeservice.CurrentTime;
import da.timeservice.timeservice.TimeManager;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.Instant;
import org.junit.After;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExternalResource;

public class TimeServiceIT {
  private static final Path RELATIVE_DAR_PATH = Paths.get("./target/market-data-service.dar");

  private static final Party OPERATOR_PARTY = new Party("Operator");

  private static final Duration systemPeriodTime = Duration.ofMillis(100); // must be non-zero

  private static final Sandbox sandbox =
      Sandbox.builder()
          .dar(RELATIVE_DAR_PATH)
          .parties(OPERATOR_PARTY.getValue())
          .useWallclockTime()
          // TODO: Run with JSON API
          .setupAppCallback(Main.runBotsWithGrpc(new AppParties(ALL_PARTIES), systemPeriodTime))
          .build();

  @ClassRule public static ExternalResource compile = sandbox.getClassRule();
  @Rule public ExternalResource sandboxRule = sandbox.getRule();

  private DamlScript script;
  private DefaultLedgerAdapter ledgerAdapter;
  private ContractWithId<TimeManager.ContractId> timeManager;

  @Before
  public void setUp() throws Throwable {
    // Valid port is assigned only after the sandbox has been started.
    // Therefore trigger has to be configured at the point where this can be guaranteed.
    script =
        DamlScript.builder()
            .dar(Paths.get("./target/market-data-service.dar"))
            .scriptName("DA.RefApps.MarketDataService.MarketSetupScript:setupMarketForSandbox")
            .sandboxPort(sandbox.getSandboxPort())
            .useWallclockTime()
            .build();
    script.run();
    ledgerAdapter = sandbox.getLedgerAdapter();
    timeManager = getTimeManagerCid();
  }

  @After
  public void tearDown() {
    script.kill();
    Main.terminateTimeUpdaterBot();
  }

  private Instant getCurrentTimeInstant() {
    ContractWithId<CurrentTime.ContractId> currentTimeCid =
        ledgerAdapter.getMatchedContract(
            OPERATOR_PARTY, CurrentTime.TEMPLATE_ID, CurrentTime.ContractId::new);
    return CurrentTime.fromValue(currentTimeCid.record).currentTime;
  }

  private ContractWithId<TimeManager.ContractId> getTimeManagerCid() {
    return ledgerAdapter.getMatchedContract(
        OPERATOR_PARTY, TimeManager.TEMPLATE_ID, TimeManager.ContractId::new);
  }

  private void changeModelPeriodTime(Duration newModelPeriodTime)
      throws InvalidProtocolBufferException, InterruptedException {
    ledgerAdapter.exerciseChoice(
        OPERATOR_PARTY,
        timeManager.contractId.exerciseSetModelPeriodTime(
            RelTime.fromDuration(newModelPeriodTime)));
  }

  private void verifyModelPeriodTime(Duration newModelPeriodTime) throws InterruptedException {
    // Passes when the difference between two subsequent times will be equal to newModelPeriodTime.
    eventually(
        () -> {
          Instant time1 = getCurrentTimeInstant();
          Instant time2 = getCurrentTimeInstant();
          assertEquals(time1.plus(newModelPeriodTime), time2);
        });
  }

  @Test
  public void modelPeriodTimeCanBeChanged()
      throws InvalidProtocolBufferException, InterruptedException {
    ledgerAdapter.exerciseChoice(OPERATOR_PARTY, timeManager.contractId.exerciseContinue());
    Duration newModelPeriodTime1 = Duration.ofHours(3);
    Duration newModelPeriodTime2 = Duration.ofHours(5);
    assertNotEquals(newModelPeriodTime1, newModelPeriodTime2);
    // The current model period is not known. To test the change two distinct values are used and
    // verified.
    changeModelPeriodTime(newModelPeriodTime1);
    verifyModelPeriodTime(newModelPeriodTime1);
    changeModelPeriodTime(newModelPeriodTime2);
    verifyModelPeriodTime(newModelPeriodTime2);
  }
}
