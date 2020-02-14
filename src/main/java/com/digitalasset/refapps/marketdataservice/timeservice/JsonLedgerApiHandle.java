/*
 * Copyright (c) 2019, Digital Asset (Switzerland) GmbH and/or its affiliates. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package com.digitalasset.refapps.marketdataservice.timeservice;

import com.daml.ledger.javaapi.data.Command;
import io.reactivex.Flowable;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import jsonapi.ActiveContract;
import jsonapi.ActiveContractSet;
import jsonapi.ContractQuery;
import jsonapi.JsonLedgerClient;
import jsonapi.apache.ApacheHttpClient;
import jsonapi.http.Api;
import jsonapi.http.HttpResponse;
import jsonapi.http.Jwt;
import jsonapi.http.WebSocketResponse;
import jsonapi.json.JsonDeserializer;
import jsonapi.json.JsonSerializer;
import jsonapi.tyrus.TyrusWebSocketClient;

public class JsonLedgerApiHandle implements LedgerApiHandle {
  private final JsonLedgerClient ledgerClient;
  private String party;

  public JsonLedgerApiHandle(
      String party,
      String ledgerId,
      String applicationId,
      JsonDeserializer<HttpResponse> httpResponseDeserializer,
      JsonSerializer jsonSerializer,
      JsonDeserializer<WebSocketResponse> webSocketResponseDeserializer) {
    this.party = party;
    String jwt = Jwt.createToken(ledgerId, applicationId, Collections.singletonList(party));
    ApacheHttpClient httpClient =
        new ApacheHttpClient(httpResponseDeserializer, jsonSerializer, jwt);
    TyrusWebSocketClient webSocketClient =
        new TyrusWebSocketClient(webSocketResponseDeserializer, jsonSerializer, jwt);
    Api api = new Api("localhost", 7575);

    ledgerClient = new JsonLedgerClient(httpClient, webSocketClient, jsonSerializer, api);
  }

  @Override
  public void submitCommand(Command command) {
    command.asCreateCommand().ifPresent(ledgerClient::create);
    command.asExerciseCommand().ifPresent(ledgerClient::exerciseChoice);
  }

  @Override
  public Flowable<List<Contract>> getCreatedEvents(ContractQuery query) {
    // TODO: Every client should have its own Flowable instance, and we should have to handle this
    // at all.
//    if (activeContractSet == null) {
//      activeContractSet =
//          ledgerClient
//              .getActiveContracts(query)
//              .filter(
//                  acs -> {
//                    System.out.println(acs);
//                    return !acs.isEmpty();
//                  });
//    }
    // TODO: Do we need to handle subscriptions, i.e. do we have to unsubscribe?
    return ledgerClient
        .getActiveContracts(query)
        .map(ActiveContractSet::getActiveContracts)
        .map(acsStream -> acsStream.map(this::getContract))
        .map(xs -> xs.collect(Collectors.toList()));
  }

  private Contract getContract(ActiveContract activeContract) {
    // TODO: Eliminate Contract type.
    return new Contract(
        activeContract.getContractId(), activeContract.getTemplate().create().getCreateArguments());
  }

  @Override
  public String getOperatingParty() {
    return party;
  }
}
