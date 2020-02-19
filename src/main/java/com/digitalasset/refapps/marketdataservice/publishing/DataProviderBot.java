/*
 * Copyright (c) 2019, Digital Asset (Switzerland) GmbH and/or its affiliates. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package com.digitalasset.refapps.marketdataservice.publishing;

import com.daml.ledger.javaapi.data.Filter;
import com.daml.ledger.javaapi.data.FiltersByParty;
import com.daml.ledger.javaapi.data.Identifier;
import com.daml.ledger.javaapi.data.InclusiveFilter;
import com.daml.ledger.javaapi.data.Template;
import com.daml.ledger.javaapi.data.TransactionFilter;
import com.daml.ledger.rxjava.components.helpers.CommandsAndPendingSet;
import com.daml.ledger.rxjava.components.helpers.CreatedContract;
import com.daml.ledger.rxjava.components.helpers.TemplateUtils;
import com.digitalasset.refapps.marketdataservice.utils.CommandsAndPendingSetBuilder;
import com.digitalasset.refapps.marketdataservice.utils.CommandsAndPendingSetBuilder.Factory;
import com.google.common.collect.Sets;
import da.refapps.marketdataservice.datasource.DataSource;
import da.refapps.marketdataservice.datastream.DataStream;
import da.refapps.marketdataservice.datastream.EmptyDataStream;
import da.refapps.marketdataservice.marketdatatypes.Observation;
import da.refapps.marketdataservice.marketdatatypes.ObservationReference;
import da.refapps.marketdataservice.marketdatatypes.ObservationValue;
import da.timeservice.timeservice.CurrentTime;
import io.reactivex.Flowable;
import java.time.Instant;
import java.util.Collections;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Stream;
import jsonapi.ActiveContractSet;
import jsonapi.Contract;
import jsonapi.ContractQuery;

/** An automation bot that publishes values on streams given by a data provider. */
public class DataProviderBot {

  private final CommandsAndPendingSetBuilder commandsAndPendingSetBuilder;
  private final TransactionFilter transactionFilter;

  private final String partyName;
  private final PublishingDataProvider publishingDataProvider;
  private final Set<Identifier> templateSet;

  public DataProviderBot(
      Factory commandsAndPendingSetBuilderFactory,
      String partyName,
      PublishingDataProvider publishingDataProvider) {
    this.partyName = partyName;
    this.publishingDataProvider = publishingDataProvider;
    String workflowId =
        "WORKFLOW-" + partyName + "-DataProviderBot-" + UUID.randomUUID().toString();
    commandsAndPendingSetBuilder =
        commandsAndPendingSetBuilderFactory.create(partyName, workflowId);

    templateSet =
        Sets.union(
            Sets.newHashSet(
                EmptyDataStream.TEMPLATE_ID, DataStream.TEMPLATE_ID, CurrentTime.TEMPLATE_ID),
            publishingDataProvider.getUsedTemplates());
    Filter streamFilter = new InclusiveFilter(templateSet);
    transactionFilter = new FiltersByParty(Collections.singletonMap(partyName, streamFilter));
  }

  // TODO: Return something else
  public Flowable<CommandsAndPendingSet> calculateCommands(ActiveContractSet activeContractSet) {
    CommandsAndPendingSetBuilder.Builder builder = commandsAndPendingSetBuilder.newBuilder();

    getCurrentTime(activeContractSet)
        .ifPresent(
            currentTime -> {
              startAllEmptyDataStream(activeContractSet, currentTime, builder);
              updateAllDataStreams(activeContractSet, currentTime, builder);
            });
    return builder.buildFlowable();
  }

  public TransactionFilter getTransactionFilter() {
    return transactionFilter;
  }

  public ContractQuery getContractQuery() {
    return new ContractQuery(templateSet);
  }

  public Template getContractInfo(CreatedContract createdContract) {
    //noinspection unchecked
    return TemplateUtils.contractTransformer(
            EmptyDataStream.class, DataStream.class, CurrentTime.class, DataSource.class)
        .apply(createdContract);
  }

  public String getPartyName() {
    return partyName;
  }

  private void startAllEmptyDataStream(
      ActiveContractSet activeContractSet,
      Instant currentTime,
      CommandsAndPendingSetBuilder.Builder cmdBuilder) {
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
                    cmdBuilder.addCommand(
                        new EmptyDataStream.ContractId(emptyDataStream.getContractId())
                            .exerciseStartDataStream(observation));
                  });
            });
  }

  private void updateAllDataStreams(
      ActiveContractSet activeContractSet,
      Instant currentTime,
      CommandsAndPendingSetBuilder.Builder cmdBuilder) {
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
                cmdBuilder.addCommand(
                    dataStreamCid.exerciseUpdateObservation(
                        currentTime, optionalObservation.get()));
              } else if (!dataStream.getContract().lastUpdated.equals(currentTime)) {
                cmdBuilder.addCommand(dataStreamCid.exerciseUpdateLicenses());
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
