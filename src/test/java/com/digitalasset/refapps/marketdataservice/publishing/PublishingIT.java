/*
 * Copyright (c) 2019, Digital Asset (Switzerland) GmbH and/or its affiliates. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package com.digitalasset.refapps.marketdataservice.publishing;

import static com.digitalasset.refapps.marketdataservice.utils.AppParties.ALL_PARTIES;
import static com.digitalasset.refapps.utils.EventuallyUtil.eventually;

import com.daml.ledger.javaapi.data.Party;
import com.digitalasset.refapps.marketdataservice.Main;
import com.digitalasset.refapps.marketdataservice.utils.AppParties;
import com.digitalasset.testing.junit4.Sandbox;
import com.digitalasset.testing.ledger.DefaultLedgerAdapter;
import com.google.protobuf.InvalidProtocolBufferException;
import da.refapps.marketdataservice.datalicense.LiveStreamLicense;
import da.refapps.marketdataservice.marketdatatypes.InstrumentId;
import da.refapps.marketdataservice.marketdatatypes.Observation;
import da.refapps.marketdataservice.marketdatatypes.ObservationReference;
import da.refapps.marketdataservice.marketdatatypes.ObservationValue;
import da.refapps.marketdataservice.marketdatatypes.observationvalue.CleanPrice;
import da.refapps.marketdataservice.marketdatatypes.observationvalue.EnrichedCleanDirtyPrice;
import da.refapps.marketdataservice.publication.Publication;
import da.timeservice.timeservice.TimeManager;
import java.io.File;
import java.math.BigDecimal;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import jsonapi.JsonApi;
import org.junit.After;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExternalResource;
import org.junit.rules.RuleChain;
import org.junit.rules.TestRule;

public class PublishingIT {
  private static final Path RELATIVE_DAR_PATH = Paths.get("target/market-data-service.dar");

  private static final Party OPERATOR_PARTY = new Party("Operator");
  private static final Party MARKET_DATA_VENDOR_PARTY = new Party("MarketDataVendor");
  private static final Party ENDUSER_PARTY = new Party("EndUser");

  private static final ObservationReference US_BOND_1 =
      new ObservationReference(
          "US Bond Market", new InstrumentId("ISIN 288 2839"), LocalDate.of(2021, 2, 11));
  private static final ObservationReference EU_BOND_1 =
      new ObservationReference(
          "European Bond Market", new InstrumentId("ISIN 123 1244"), LocalDate.of(2021, 3, 20));

  private static final Duration systemPeriodTime = Duration.ofSeconds(5);

  private static final Sandbox sandbox =
      Sandbox.builder()
          .dar(RELATIVE_DAR_PATH)
          .parties(OPERATOR_PARTY.getValue())
          .useWallclockTime()
          .build();

  @ClassRule public static ExternalResource compile = sandbox.getClassRule();

  @Rule
  public final TestRule processes =
      RuleChain.outerRule(sandbox.getRule()).around(new JsonApi(sandbox::getSandboxPort));

  private Process marketSetupAndTriggers;
  private DefaultLedgerAdapter ledgerAdapter;

  @Before
  public void setUp() throws Throwable {
    Main.runBots(
        sandbox.getClient().getLedgerId(),
        "localhost",
        7575,
        new AppParties(ALL_PARTIES),
        systemPeriodTime);
    // Valid port is assigned only after the sandbox has been started.
    // Therefore trigger has to be configured at the point where this can be guaranteed.
    File log = new File("integration-marketSetupAndTriggers.log");
    File errLog = new File("integration-marketSetupAndTriggers.err.log");
    marketSetupAndTriggers =
        new ProcessBuilder()
            .command(
                "scripts/startTriggersNoRegister.sh",
                "localhost",
                Integer.toString(sandbox.getSandboxPort()),
                RELATIVE_DAR_PATH.toString())
            .redirectError(ProcessBuilder.Redirect.appendTo(errLog))
            .redirectOutput(ProcessBuilder.Redirect.appendTo(log))
            .start();
    ledgerAdapter = sandbox.getLedgerAdapter();
  }

  @After
  public void tearDown() {
    marketSetupAndTriggers.destroyForcibly();
    Main.terminateTimeUpdaterBot();
  }

  @Test
  public void dataShouldBePublishedAsTimeAdvances()
      throws InterruptedException, InvalidProtocolBufferException {

    waitForTheWholeSystemToSetup();

    observePublication(
        MARKET_DATA_VENDOR_PARTY,
        observation(US_BOND_1, "2019-11-12T12:30:00Z", cleanprice("1")),
        observation(EU_BOND_1, "2019-11-12T12:30:00Z", cleanprice("1000")));
    observePublication(
        ENDUSER_PARTY,
        observation(US_BOND_1, "2019-11-12T12:30:00Z", cleanprice("1")),
        observation(
            US_BOND_1,
            "2019-11-12T12:30:00Z",
            dirtyprice("1", "1.0150136986", "0.0150136986", LocalDate.of(2020, 2, 11), "0.02")));

    startModelClock();

    observePublication(
        ENDUSER_PARTY,
        observation(US_BOND_1, "2019-11-12T14:30:00Z", cleanprice("2")),
        observation(
            US_BOND_1,
            "2019-11-12T14:30:00Z",
            dirtyprice("2", "2.0150136986", "0.0150136986", LocalDate.of(2020, 2, 11), "0.02")));
  }

  /**
   * When the system has been set up and all the triggers and bots have been started, there should
   * be a Live Stream license on the ledger (as streaming has started).
   */
  private void waitForTheWholeSystemToSetup() throws InterruptedException {
    eventually(
        () ->
            ledgerAdapter.getCreatedContractId(
                ENDUSER_PARTY, LiveStreamLicense.TEMPLATE_ID, LiveStreamLicense.ContractId::new));
  }

  private void startModelClock() throws InvalidProtocolBufferException {
    TimeManager.ContractId timeManager =
        ledgerAdapter.getCreatedContractId(
            OPERATOR_PARTY, TimeManager.TEMPLATE_ID, TimeManager.ContractId::new);
    ledgerAdapter.exerciseChoice(OPERATOR_PARTY, timeManager.exerciseContinue());
  }

  private ObservationValue dirtyprice(
      String clean, String dirty, String accrual, LocalDate couponDate, String rate) {
    return new EnrichedCleanDirtyPrice(
        new BigDecimal(clean).setScale(10),
        new BigDecimal(dirty).setScale(10),
        new BigDecimal(accrual).setScale(10),
        Optional.of(couponDate),
        new BigDecimal(rate).setScale(10));
  }

  private ObservationValue cleanprice(String value) {
    return new CleanPrice(new BigDecimal(value).setScale(10));
  }

  private Observation observation(
      ObservationReference usBond1, String date, ObservationValue value) {
    return new Observation(usBond1, Instant.parse(date), value);
  }

  private void observePublication(Party party, Observation... expectedValues) {
    List<Predicate<Publication>> predicates =
        Arrays.stream(expectedValues)
            .map(expected -> createMatcher(party, expected))
            .collect(Collectors.toList());
    ledgerAdapter.observeMatchingContracts(
        party,
        Publication.TEMPLATE_ID,
        Publication::fromValue,
        false,
        predicates.toArray(new Predicate[] {}));
  }

  private Predicate<Publication> createMatcher(Party party, Observation expected) {
    return publication ->
        publication.consumer.party.equals(party.getValue())
            && expected.equals(publication.observation);
  }
}
