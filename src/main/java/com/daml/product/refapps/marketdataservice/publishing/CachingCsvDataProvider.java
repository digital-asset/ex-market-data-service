/*
 * Copyright (c) 2019, Digital Asset (Switzerland) GmbH and/or its affiliates. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package com.daml.product.refapps.marketdataservice.publishing;

import com.daml.ledger.javaapi.data.Identifier;
import com.daml.extensions.jsonapi.ActiveContractSet;
import com.daml.extensions.jsonapi.Contract;
import com.google.common.collect.Sets;
import da.refapps.marketdataservice.datasource.DataSource;
import da.refapps.marketdataservice.marketdatatypes.ObservationReference;
import da.refapps.marketdataservice.marketdatatypes.ObservationValue;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.Function;
import java.util.stream.Stream;

public class CachingCsvDataProvider implements PublishingDataProvider {

  private static final String DATA_DIR = "data";
  private final ConcurrentHashMap<
          ObservationReference, ConcurrentLinkedQueue<ObservationTimeWithValue>>
      cache = new ConcurrentHashMap<>();
  private final Function<String, String> readFile;

  public CachingCsvDataProvider() {
    this.readFile = CachingCsvDataProvider::readFileFromDataDir;
  }

  public CachingCsvDataProvider(Function<String, String> readFile) {
    this.readFile = readFile;
  }

  @Override
  public Set<Identifier> getUsedTemplates() {
    return Sets.newHashSet(DataSource.TEMPLATE_ID);
  }

  @Override
  public Optional<ObservationValue> getObservation(
      ActiveContractSet activeContractSet, ObservationReference reference, Instant time) {
    if (!cache.containsKey(reference)) {
      initCache(activeContractSet);
    }
    ConcurrentLinkedQueue<ObservationTimeWithValue> dataForReference =
        cache.getOrDefault(reference, new ConcurrentLinkedQueue<>());
    return selectDataInActualTimeWindow(dataForReference, time);
  }

  public static String readFileFromDataDir(String path) {
    try {
      return new String(Files.readAllBytes(Paths.get(DATA_DIR, path)));
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  private void initCache(ActiveContractSet activeContractSet) {
    Stream<DataSource> dataSources = getDataSources(activeContractSet);
    dataSources.forEach(
        dataSource -> cache.computeIfAbsent(dataSource.reference, x -> parseData(dataSource)));
  }

  private ConcurrentLinkedQueue<ObservationTimeWithValue> parseData(DataSource dataSource) {
    return new ConcurrentLinkedQueue<>(CsvParser.parseData(readFile.apply(dataSource.path)));
  }

  private Stream<DataSource> getDataSources(ActiveContractSet activeContractSet) {
    return activeContractSet
        .getActiveContracts(DataSource.TEMPLATE_ID, DataSource.class)
        .map(Contract::getContract);
  }

  private Optional<ObservationValue> selectDataInActualTimeWindow(
      ConcurrentLinkedQueue<ObservationTimeWithValue> data, Instant time) {
    ObservationTimeWithValue nextValue = data.peek();
    ObservationValue result = null;
    while (nextValue != null && !nextValue.time.isAfter(time)) {
      result = data.poll().observationValue;
      nextValue = data.peek();
    }

    return Optional.ofNullable(result);
  }
}
