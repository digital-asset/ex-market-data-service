/*
 * Copyright (c) 2019, Digital Asset (Switzerland) GmbH and/or its affiliates. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package com.digitalasset.refapps.marketdataservice.publishing;

import static com.digitalasset.refapps.marketdataservice.assertions.Assert.assertHasSingleExercise;
import static com.digitalasset.refapps.utils.LedgerTestViewUtil.createEmptyLedgerTestView;
import static org.junit.Assert.assertTrue;

import com.daml.ledger.javaapi.data.Identifier;
import com.daml.ledger.javaapi.data.Template;
import com.daml.ledger.rxjava.components.LedgerViewFlowable;
import com.daml.ledger.rxjava.components.LedgerViewFlowable.LedgerTestView;
import com.daml.ledger.rxjava.components.helpers.CommandsAndPendingSet;
import com.digitalasset.refapps.marketdataservice.utils.CommandsAndPendingSetBuilder;
import com.google.common.collect.Sets;
import da.refapps.marketdataservice.datastream.DataStream;
import da.refapps.marketdataservice.datastream.EmptyDataStream;
import da.refapps.marketdataservice.marketdatatypes.InstrumentId;
import da.refapps.marketdataservice.marketdatatypes.Observation;
import da.refapps.marketdataservice.marketdatatypes.ObservationReference;
import da.refapps.marketdataservice.marketdatatypes.ObservationValue;
import da.refapps.marketdataservice.marketdatatypes.Publisher;
import da.refapps.marketdataservice.marketdatatypes.observationvalue.CleanPrice;
import da.timeservice.timeservice.CurrentTime;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Collections;
import java.util.Optional;
import java.util.Set;
import org.junit.Test;

public class DataProviderBotTest {

  private static final String OPERATOR = "Operator1";
  private static final ObservationReference REFERENCE =
      new ObservationReference("Market1", new InstrumentId("ID-1"), LocalDate.MIN);
  private static final Publisher PUBLISHER = new Publisher("Publisher1");
  private static final ObservationValue OBSERVATION_VALUE_1 =
      new CleanPrice(new BigDecimal(BigInteger.TEN));

  private final CommandsAndPendingSetBuilder.Factory cmdsBuilderFactory =
      CommandsAndPendingSetBuilder.factory("AppId1", Clock::systemUTC, Duration.ofSeconds(2));

  private class TestDataProvider implements PublishingDataProvider {
    private final Optional value;

    TestDataProvider(Optional value) {
      this.value = value;
    }

    @Override
    public Set<Identifier> getUsedTemplates() {
      return Sets.newHashSet();
    }

    @Override
    public Optional<ObservationValue> getObservation(
        LedgerViewFlowable.LedgerView<Template> ledgerView,
        ObservationReference reference,
        Instant time) {
      return value;
    }
  }

  private final PublishingDataProvider publishingDataProvider =
      new TestDataProvider(Optional.of(OBSERVATION_VALUE_1));
  private final DataProviderBot bot =
      new DataProviderBot(cmdsBuilderFactory, PUBLISHER.party, publishingDataProvider);

  private final PublishingDataProvider nonpublishingDataProvider =
      new TestDataProvider(Optional.empty());
  private final DataProviderBot botNonpublishing =
      new DataProviderBot(cmdsBuilderFactory, PUBLISHER.party, nonpublishingDataProvider);

  @Test
  public void testEmptyStreamIsStarted() {
    EmptyDataStream emptyDataStream =
        new EmptyDataStream(OPERATOR, REFERENCE, Collections.emptyList(), PUBLISHER);
    CurrentTime currentTime =
        new CurrentTime(
            OPERATOR, Instant.parse("2020-01-03T10:15:30.00Z"), Collections.emptyList());
    final String emptyDataStreamCid = "cid2";

    LedgerTestView<Template> ledgerView =
        createEmptyLedgerTestView()
            .addActiveContract(CurrentTime.TEMPLATE_ID, "cid1", currentTime)
            .addActiveContract(EmptyDataStream.TEMPLATE_ID, emptyDataStreamCid, emptyDataStream);

    CommandsAndPendingSet result = bot.calculateCommands(ledgerView).blockingFirst();
    assertHasSingleExercise(result, emptyDataStreamCid, "StartDataStream");
  }

  @Test
  public void testEmptyStreamIsNotStartedIfNoPublicationAvailable() {
    EmptyDataStream emptyDataStream =
        new EmptyDataStream(OPERATOR, REFERENCE, Collections.emptyList(), PUBLISHER);
    CurrentTime currentTime =
        new CurrentTime(
            OPERATOR, Instant.parse("2020-01-03T10:15:30.00Z"), Collections.emptyList());
    final String emptyDataStreamCid = "cid2";

    LedgerTestView<Template> ledgerView =
        createEmptyLedgerTestView()
            .addActiveContract(CurrentTime.TEMPLATE_ID, "cid1", currentTime)
            .addActiveContract(EmptyDataStream.TEMPLATE_ID, emptyDataStreamCid, emptyDataStream);

    assertTrue(botNonpublishing.calculateCommands(ledgerView).isEmpty().blockingGet());
  }

  @Test
  public void testDataStreamPublicationHappensIfTimePassed() {
    Instant now = Instant.parse("2020-01-03T10:15:30.00Z");
    DataStream dataStream =
        new DataStream(
            new Observation(REFERENCE, now, new CleanPrice(BigDecimal.ONE)),
            Collections.emptyList(),
            PUBLISHER,
            now,
            OPERATOR,
            now);
    CurrentTime currentTime =
        new CurrentTime(OPERATOR, now.plus(Duration.ofSeconds(10)), Collections.emptyList());
    final String dataStreamCid = "cid2";

    LedgerTestView<Template> ledgerView =
        createEmptyLedgerTestView()
            .addActiveContract(CurrentTime.TEMPLATE_ID, "cid1", currentTime)
            .addActiveContract(DataStream.TEMPLATE_ID, dataStreamCid, dataStream);

    CommandsAndPendingSet result = bot.calculateCommands(ledgerView).blockingFirst();
    assertHasSingleExercise(result, dataStreamCid, "UpdateObservation");
  }

  @Test
  public void testDataStreamNoPublicationHappensIfNoTimeChange() {
    Instant now = Instant.parse("2020-01-03T10:15:30.00Z");
    DataStream dataStream =
        new DataStream(
            new Observation(REFERENCE, now, new CleanPrice(BigDecimal.TEN)),
            Collections.emptyList(),
            PUBLISHER,
            now,
            OPERATOR,
            now);
    CurrentTime currentTime = new CurrentTime(OPERATOR, now, Collections.emptyList());
    final String dataStreamCid = "cid2";

    LedgerTestView<Template> ledgerView =
        createEmptyLedgerTestView()
            .addActiveContract(CurrentTime.TEMPLATE_ID, "cid1", currentTime)
            .addActiveContract(DataStream.TEMPLATE_ID, dataStreamCid, dataStream);

    assertTrue(botNonpublishing.calculateCommands(ledgerView).isEmpty().blockingGet());
  }

  @Test
  public void testDataStreamUpdatesLicensesIfTimePassedButNoPublicationAvailable() {
    Instant now = Instant.parse("2020-01-03T10:15:30.00Z");
    DataStream dataStream =
        new DataStream(
            new Observation(REFERENCE, now, new CleanPrice(BigDecimal.ONE)),
            Collections.emptyList(),
            PUBLISHER,
            now,
            OPERATOR,
            now);
    CurrentTime currentTime =
        new CurrentTime(OPERATOR, now.plus(Duration.ofSeconds(10)), Collections.emptyList());
    final String dataStreamCid = "cid2";

    LedgerTestView<Template> ledgerView =
        createEmptyLedgerTestView()
            .addActiveContract(CurrentTime.TEMPLATE_ID, "cid1", currentTime)
            .addActiveContract(DataStream.TEMPLATE_ID, dataStreamCid, dataStream);

    CommandsAndPendingSet result = botNonpublishing.calculateCommands(ledgerView).blockingFirst();
    assertHasSingleExercise(result, dataStreamCid, "UpdateLicenses");
  }
}
