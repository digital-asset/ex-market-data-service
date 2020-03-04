/*
 * Copyright (c) 2019, Digital Asset (Switzerland) GmbH and/or its affiliates. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package com.digitalasset.refapps.marketdataservice.publishing;

import com.daml.ledger.javaapi.data.Command;
import com.daml.ledger.javaapi.data.Identifier;
import com.digitalasset.jsonapi.ActiveContractSet;
import com.digitalasset.jsonapi.Contract;
import com.digitalasset.jsonapi.ContractQuery;
import com.google.common.collect.Sets;
import da.refapps.marketdataservice.datastream.DataStream;
import da.refapps.marketdataservice.datastream.EmptyDataStream;
import da.refapps.marketdataservice.marketdatatypes.Observation;
import da.refapps.marketdataservice.marketdatatypes.ObservationReference;
import da.refapps.marketdataservice.marketdatatypes.Publisher;
import da.timeservice.timeservice.CurrentTime;
import io.reactivex.Flowable;
import java.time.Instant;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

/** An automation bot that publishes values on streams given by a data provider. */
public class DataProviderBot {

  private final String partyName;
  private final PublishingDataProvider publishingDataProvider;
  private final Set<Identifier> templateSet;

  public DataProviderBot(String partyName, PublishingDataProvider publishingDataProvider) {
    this.partyName = partyName;
    this.publishingDataProvider = publishingDataProvider;

    templateSet =
        Sets.union(
            Sets.newHashSet(
                EmptyDataStream.TEMPLATE_ID, DataStream.TEMPLATE_ID, CurrentTime.TEMPLATE_ID),
            publishingDataProvider.getUsedTemplates());
  }

  public Flowable<Command> getCommands(ActiveContractSet activeContractSet) {
    Stream<Command> commands =
        getCurrentTime(activeContractSet)
            .map(
                currentTime ->
                    Stream.concat(
                        startAllEmptyDataStream(activeContractSet, currentTime),
                        updateAllDataStreams(activeContractSet, currentTime)))
            .orElse(Stream.empty());
    return Flowable.fromIterable(commands::iterator);
  }

  public ContractQuery getContractQuery() {
    return new ContractQuery(templateSet);
  }

  public String getPartyName() {
    return partyName;
  }

  private Stream<Command> startAllEmptyDataStream(
      ActiveContractSet activeContractSet, Instant currentTime) {
    Stream<Contract<EmptyDataStream>> emptyDataStreams =
        activeContractSet.getActiveContracts(EmptyDataStream.TEMPLATE_ID, EmptyDataStream.class);
    return emptyDataStreams
        .filter(x -> isOwnStream(x.getContract()))
        .map(x -> getStartEmptyDataStreamCommand(activeContractSet, currentTime, x))
        .filter(Optional::isPresent)
        .map(Optional::get);
  }

  private Optional<Command> getStartEmptyDataStreamCommand(
      ActiveContractSet activeContractSet,
      Instant currentTime,
      Contract<EmptyDataStream> emptyDataStream) {
    ObservationReference label = emptyDataStream.getContract().reference;
    Optional<Observation> observation = getObservation(activeContractSet, currentTime, label);
    return observation.map(x -> startEmptyDataStream(emptyDataStream, x));
  }

  private Command startEmptyDataStream(
      Contract<EmptyDataStream> emptyDataStream, Observation observation) {
    EmptyDataStream.ContractId contractId =
        new EmptyDataStream.ContractId(emptyDataStream.getContractId());
    return contractId.exerciseStartDataStream(observation);
  }

  private Stream<Command> updateAllDataStreams(
      ActiveContractSet activeContractSet, Instant currentTime) {
    Stream<Contract<DataStream>> dataStreams =
        activeContractSet.getActiveContracts(DataStream.TEMPLATE_ID, DataStream.class);
    return dataStreams
        .filter(x -> isOwnStream(x.getContract()) && isStale(currentTime, x.getContract()))
        .map(x -> getUpdateDataStreamCommand(activeContractSet, currentTime, x))
        .filter(Optional::isPresent)
        .map(Optional::get);
  }

  private Optional<Command> getUpdateDataStreamCommand(
      ActiveContractSet activeContractSet, Instant currentTime, Contract<DataStream> dataStream) {
    ObservationReference label = dataStream.getContract().observation.label;
    Optional<Observation> observation = getObservation(activeContractSet, currentTime, label);
    return updateDataStream(currentTime, dataStream, observation);
  }

  private Optional<Command> updateDataStream(
      Instant currentTime, Contract<DataStream> dataStream, Optional<Observation> observation) {
    DataStream.ContractId contractId = new DataStream.ContractId(dataStream.getContractId());
    if (observation.isPresent()) {
      return observation.map(x -> contractId.exerciseUpdateObservation(currentTime, x.value));
    } else if (!dataStream.getContract().lastUpdated.equals(currentTime)) {
      return Optional.of(contractId.exerciseUpdateLicenses());
    } else {
      return Optional.empty();
    }
  }

  private boolean isOwnStream(EmptyDataStream emptyDataStream) {
    return isPublishedByParty(emptyDataStream.publisher);
  }

  private boolean isOwnStream(DataStream dataStream) {
    return isPublishedByParty(dataStream.publisher);
  }

  private boolean isStale(Instant currentTime, DataStream dataStream) {
    return currentTime.isAfter(dataStream.observation.time);
  }

  private boolean isPublishedByParty(Publisher publisher) {
    return publisher.party.equals(partyName);
  }

  private Optional<Observation> getObservation(
      ActiveContractSet activeContractSet, Instant currentTime, ObservationReference label) {
    return publishingDataProvider
        .getObservation(activeContractSet, label, currentTime)
        .map(x -> new Observation(label, currentTime, x));
  }

  private Optional<Instant> getCurrentTime(ActiveContractSet activeContractSet) {
    return activeContractSet
        .getActiveContracts(CurrentTime.TEMPLATE_ID, CurrentTime.class)
        .map(x -> x.getContract().currentTime)
        .findFirst();
  }
}
