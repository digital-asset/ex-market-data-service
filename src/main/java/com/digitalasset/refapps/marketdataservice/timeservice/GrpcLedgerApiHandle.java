/*
 * Copyright (c) 2019, Digital Asset (Switzerland) GmbH and/or its affiliates. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package com.digitalasset.refapps.marketdataservice.timeservice;

import com.daml.ledger.javaapi.data.*;
import com.daml.ledger.rxjava.LedgerClient;
import com.digitalasset.refapps.marketdataservice.utils.CommandsAndPendingSetBuilder;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import jsonapi.ContractQuery;

public class GrpcLedgerApiHandle implements LedgerApiHandle {

  private final LedgerClient client;
  private final String party;
  private final CommandsAndPendingSetBuilder commandsAndPendingSetBuilder;

  public GrpcLedgerApiHandle(
      LedgerClient client,
      CommandsAndPendingSetBuilder.Factory commandsAndPendingSetBuilderFactory,
      String party) {
    this(client, commandsAndPendingSetBuilderFactory, party, UUID.randomUUID().toString());
  }

  public GrpcLedgerApiHandle(
      LedgerClient client,
      CommandsAndPendingSetBuilder.Factory commandsAndPendingSetBuilderFactory,
      String party,
      String workflowId) {
    this.client = client;
    this.party = party;
    this.commandsAndPendingSetBuilder =
        commandsAndPendingSetBuilderFactory.create(party, workflowId);
  }

  public void submitCommand(Command command) {
    List<Command> commands = Collections.singletonList(command);
    SubmitCommandsRequest submitCommandsRequest =
        commandsAndPendingSetBuilder
            .newBuilder()
            .addCommand(command)
            .build()
            .get()
            .getSubmitCommandsRequest();
    client
        .getCommandSubmissionClient()
        .submit(
            submitCommandsRequest.getWorkflowId(),
            submitCommandsRequest.getApplicationId(),
            submitCommandsRequest.getCommandId(),
            submitCommandsRequest.getParty(),
            submitCommandsRequest.getLedgerEffectiveTime(),
            submitCommandsRequest.getMaximumRecordTime(),
            commands)
        .blockingGet();
  }

  public List<Contract> getCreatedEvents(ContractQuery contractQuery) {
    FiltersByParty filter =
        createFilter(
            getOperatingParty(),
            contractQuery.getTemplateIds().stream().collect(Collectors.toSet()));
    GetActiveContractsResponse activeContractSetResponse =
        client
            .getActiveContractSetClient()
            .getActiveContracts(filter, false)
            .timeout(5, TimeUnit.SECONDS)
            .blockingFirst();
    if (activeContractSetResponse.getCreatedEvents().isEmpty()) {
      LedgerOffset.Absolute offset =
          new LedgerOffset.Absolute(activeContractSetResponse.getOffset().get());
      Transaction transaction =
          client
              .getTransactionsClient()
              .getTransactions(offset, filter, false)
              .filter(x -> !x.getEvents().isEmpty())
              .blockingFirst();
      return transaction.getEvents().stream()
          .filter(x -> x instanceof CreatedEvent)
          .map(x -> new Contract(x.getContractId(), ((CreatedEvent) x).getArguments()))
          .collect(Collectors.toList());
    }
    return activeContractSetResponse.getCreatedEvents().stream()
        .map(event -> new Contract(event.getContractId(), event.getArguments()))
        .collect(Collectors.toList());
  }

  @Override
  public String getOperatingParty() {
    return party;
  }

  private static FiltersByParty createFilter(String party, Set<Identifier> templateIds) {
    return new FiltersByParty(Collections.singletonMap(party, new InclusiveFilter(templateIds)));
  }
}
