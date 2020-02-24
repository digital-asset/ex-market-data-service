/*
 * Copyright (c) 2019, Digital Asset (Switzerland) GmbH and/or its affiliates. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package com.digitalasset.refapps.marketdataservice.publishing;

import com.daml.ledger.javaapi.data.Command;
import com.daml.ledger.javaapi.data.Identifier;
import com.google.common.collect.Sets;
import da.refapps.marketdataservice.datastream.DataStream;
import da.refapps.marketdataservice.datastream.EmptyDataStream;
import da.refapps.marketdataservice.marketdatatypes.Observation;
import da.refapps.marketdataservice.marketdatatypes.ObservationReference;
import da.refapps.marketdataservice.marketdatatypes.ObservationValue;
import da.timeservice.timeservice.CurrentTime;
import io.reactivex.Flowable;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;
import jsonapi.ActiveContractSet;
import jsonapi.Contract;
import jsonapi.ContractQuery;

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
    Collection<Command> commands = new ArrayList<>();
    getCurrentTime(activeContractSet)
        .ifPresent(
            currentTime -> {
              startAllEmptyDataStream(activeContractSet, currentTime, commands);
              updateAllDataStreams(activeContractSet, currentTime, commands);
            });
    return Flowable.fromIterable(commands);
  }

  public ContractQuery getContractQuery() {
    return new ContractQuery(templateSet);
  }

  public String getPartyName() {
    return partyName;
  }

  private void startAllEmptyDataStream(
      ActiveContractSet activeContractSet, Instant currentTime, Collection<Command> commands) {
    Stream<Contract<EmptyDataStream>> emptyDataStreams =
        activeContractSet.getActiveContracts(EmptyDataStream.TEMPLATE_ID, EmptyDataStream.class);

    emptyDataStreams
        .filter(emptyDataStream -> emptyDataStream.getContract().publisher.party.equals(partyName))
        // TODO: Use stream properly
        .forEach(
            emptyDataStream -> {
              ObservationReference label = emptyDataStream.getContract().reference;
              Optional<ObservationValue> optionalObservation =
                  publishingDataProvider.getObservation(activeContractSet, label, currentTime);
              optionalObservation.ifPresent(
                  observationValue -> {
                    Observation observation = new Observation(label, currentTime, observationValue);
                    commands.add(
                        new EmptyDataStream.ContractId(emptyDataStream.getContractId())
                            .exerciseStartDataStream(observation));
                  });
            });
  }

  private void updateAllDataStreams(
      ActiveContractSet activeContractSet, Instant currentTime, Collection<Command> commands) {
    Stream<Contract<DataStream>> dataStreams =
        activeContractSet.getActiveContracts(DataStream.TEMPLATE_ID, DataStream.class);

    dataStreams
        .filter(
            dataStream ->
                dataStream.getContract().publisher.party.equals(partyName)
                    && currentTime.isAfter(dataStream.getContract().observation.time))
        // TODO: Use stream properly
        .forEach(
            dataStream -> {
              Optional<ObservationValue> optionalObservation =
                  publishingDataProvider.getObservation(
                      activeContractSet, dataStream.getContract().observation.label, currentTime);
              final DataStream.ContractId dataStreamCid =
                  new DataStream.ContractId(dataStream.getContractId());
              if (optionalObservation.isPresent()) {
                commands.add(
                    dataStreamCid.exerciseUpdateObservation(
                        currentTime, optionalObservation.get()));
              } else if (!dataStream.getContract().lastUpdated.equals(currentTime)) {
                commands.add(dataStreamCid.exerciseUpdateLicenses());
              }
            });
  }

  private Optional<Instant> getCurrentTime(ActiveContractSet activeContractSet) {
    return activeContractSet
        .getActiveContracts(CurrentTime.TEMPLATE_ID, CurrentTime.class)
        .findFirst()
        .map(x -> x.getContract().currentTime);
  }
}
