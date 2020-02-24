/*
 * Copyright (c) 2019, Digital Asset (Switzerland) GmbH and/or its affiliates. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package com.digitalasset.refapps.marketdataservice.publishing;

import static com.digitalasset.refapps.marketdataservice.assertions.Assert.assertEmpty;
import static com.digitalasset.refapps.marketdataservice.assertions.Assert.assertOptionalValue;

import da.refapps.marketdataservice.datasource.DataSource;
import da.refapps.marketdataservice.marketdatatypes.InstrumentId;
import da.refapps.marketdataservice.marketdatatypes.ObservationReference;
import da.refapps.marketdataservice.marketdatatypes.ObservationValue;
import da.refapps.marketdataservice.marketdatatypes.observationvalue.CleanPrice;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Collections;
import java.util.Optional;
import jsonapi.ActiveContract;
import jsonapi.ActiveContractSet;
import org.junit.Test;

public class CachingCsvDataProviderTest {
  private static final String someParty = "party1";
  private static final ObservationReference reference =
      new ObservationReference("market1", new InstrumentId("instrument1"), LocalDate.ofEpochDay(0));
  private static final String content =
      String.join(
          "\\n",
          "2007-12-03T10:15:30.00Z, 1  ",
          "2007-12-04T09:15:30.00Z, 2  ",
          "2007-12-05T11:15:30.00Z, 3  ",
          "2007-12-07T13:15:30.00Z, 4  ");
  private static String path = "path";
  private static final DataSource dataSource =
      new DataSource(someParty, someParty, Collections.emptyList(), reference, path);
  private static final ActiveContractSet ACTIVE_CONTRACT_SET =
      ActiveContractSet.empty().add(new ActiveContract(DataSource.TEMPLATE_ID, "cid1", dataSource));

  @Test
  public void correctlySpecifiedTimeHasOneObservationValue() {
    PublishingDataProvider sut = new CachingCsvDataProvider(path -> content);
    Instant currentTime = Instant.parse("2007-12-03T11:00:30.00Z");

    Optional<ObservationValue> result =
        sut.getObservation(ACTIVE_CONTRACT_SET, reference, currentTime);
    assertOptionalValue(cleanPrice("1"), result);
  }

  @Test
  public void exactTimeHasOneObservationValue() {
    PublishingDataProvider sut = new CachingCsvDataProvider(path -> content);
    Instant currentTime = Instant.parse("2007-12-03T10:15:30.00Z");

    Optional<ObservationValue> result =
        sut.getObservation(ACTIVE_CONTRACT_SET, reference, currentTime);
    assertOptionalValue(cleanPrice("1"), result);
  }

  @Test
  public void tooEarlyCurrentTimeHasNoObservationValue() {
    PublishingDataProvider sut = new CachingCsvDataProvider(path -> content);
    Instant currentTime = Instant.parse("2007-12-01T11:00:30.00Z");

    Optional<ObservationValue> result =
        sut.getObservation(ACTIVE_CONTRACT_SET, reference, currentTime);
    assertEmpty(result);
  }

  @Test
  public void continuousConsumptionAlwaysYieldsOneObservationValue() {
    PublishingDataProvider sut = new CachingCsvDataProvider(path -> content);
    Instant currentTime = Instant.parse("2007-12-03T11:00:30.00Z");

    Optional<ObservationValue> result =
        sut.getObservation(ACTIVE_CONTRACT_SET, reference, currentTime);
    assertOptionalValue(cleanPrice("1"), result);

    currentTime = Instant.parse("2007-12-04T11:00:30.00Z");

    result = sut.getObservation(ACTIVE_CONTRACT_SET, reference, currentTime);
    assertOptionalValue(cleanPrice("2"), result);

    currentTime = Instant.parse("2007-12-05T20:00:30.00Z");

    result = sut.getObservation(ACTIVE_CONTRACT_SET, reference, currentTime);
    assertOptionalValue(cleanPrice("3"), result);

    currentTime = Instant.parse("2007-12-07T15:00:30.00Z");

    result = sut.getObservation(ACTIVE_CONTRACT_SET, reference, currentTime);
    assertOptionalValue(cleanPrice("4"), result);
  }

  @Test
  public void latestObservationValueIsReturned() {
    PublishingDataProvider sut = new CachingCsvDataProvider(path -> content);
    Instant currentTime = Instant.parse("2007-12-04T11:00:30.00Z");

    Optional<ObservationValue> result =
        sut.getObservation(ACTIVE_CONTRACT_SET, reference, currentTime);

    assertOptionalValue(cleanPrice("2"), result);
  }

  @Test
  public void noObservationValueWhenContentIsEmpty() {
    PublishingDataProvider sut = new CachingCsvDataProvider(path -> "");
    ObservationReference reference =
        new ObservationReference(
            "market1", new InstrumentId("instrument2"), LocalDate.ofEpochDay(0));
    DataSource emptyDataSource =
        new DataSource(someParty, someParty, Collections.emptyList(), reference, "empty-path");
    ActiveContractSet activeContractSet =
        ActiveContractSet.empty()
            .add(new ActiveContract(DataSource.TEMPLATE_ID, "emptyCid", emptyDataSource));
    Instant currentTime = Instant.parse("2007-12-04T11:00:30.00Z");

    Optional<ObservationValue> result =
        sut.getObservation(activeContractSet, reference, currentTime);

    assertEmpty(result);
  }

  @Test(expected = IllegalArgumentException.class)
  public void exceptionIsThrownWhenCsvCannotBeParsed() {
    PublishingDataProvider sut = new CachingCsvDataProvider(path -> "gibberish");
    ObservationReference reference =
        new ObservationReference(
            "market1", new InstrumentId("instrument3"), LocalDate.ofEpochDay(0));
    DataSource badDataSource =
        new DataSource(someParty, someParty, Collections.emptyList(), reference, "gibberish-path");
    ActiveContractSet activeContractSet =
        ActiveContractSet.empty()
            .add(new ActiveContract(DataSource.TEMPLATE_ID, "badCid", badDataSource));
    Instant currentTime = Instant.parse("2007-12-04T11:00:30.00Z");

    sut.getObservation(activeContractSet, reference, currentTime);
  }

  private CleanPrice cleanPrice(String i) {
    return new CleanPrice(new BigDecimal(i));
  }
}
