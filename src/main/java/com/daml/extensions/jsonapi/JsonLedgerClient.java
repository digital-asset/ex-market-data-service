/*
 * Copyright (c) 2019, Digital Asset (Switzerland) GmbH and/or its affiliates. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package com.daml.extensions.jsonapi;

import com.daml.ledger.javaapi.data.CreateCommand;
import com.daml.ledger.javaapi.data.ExerciseCommand;
import com.daml.extensions.jsonapi.events.Event;
import com.daml.extensions.jsonapi.http.Api;
import com.daml.extensions.jsonapi.http.HttpClient;
import com.daml.extensions.jsonapi.http.HttpResponse;
import com.daml.extensions.jsonapi.http.HttpResponse.SearchResult;
import com.daml.extensions.jsonapi.http.WebSocketClient;
import com.daml.extensions.jsonapi.http.WebSocketResponse;
import io.reactivex.Flowable;
import java.util.Collection;
import java.util.Collections;

public class JsonLedgerClient implements LedgerClient {

  private final HttpClient httpClient;
  private final WebSocketClient webSocketClient;
  private final Api api;

  private void throwIfStatusIsNot200(HttpResponse httpResponse) throws RuntimeException {
    if (httpResponse.getStatus() != 200) {
      String errors = String.join(";", httpResponse.getErrors());
      throw new RuntimeException(errors);
    }
  }

  private Collection<Event> throwOrGetEvents(WebSocketResponse webSocketResponse) {
    webSocketResponse
        .getError()
        .ifPresent(
            errors -> {
              throw new RuntimeException(errors);
            });
    webSocketResponse
        .getWarnings()
        .ifPresent(
            warnings -> {
              throw new RuntimeException(warnings.toString());
            });
    if (webSocketResponse.getHeartbeat().isPresent() || webSocketResponse.getLive().isPresent())
      return Collections.emptyList();
    return webSocketResponse.getEvents().get();
  }

  public JsonLedgerClient(HttpClient httpClient, WebSocketClient webSocketClient, Api api) {
    this.httpClient = httpClient;
    this.webSocketClient = webSocketClient;
    this.api = api;
  }

  @Override
  public void create(CreateCommand command) {
    HttpResponse httpResponse = httpClient.post(api.createContract(), command);
    throwIfStatusIsNot200(httpResponse);
  }

  @Override
  public void exerciseChoice(ExerciseCommand command) {
    HttpResponse httpResponse = httpClient.post(api.exercise(), command);
    throwIfStatusIsNot200(httpResponse);
  }

  @Override
  public ActiveContractSet queryContracts(ContractQuery query) {
    HttpResponse httpResponse = httpClient.post(api.searchContract(), query);
    throwIfStatusIsNot200(httpResponse);
    ActiveContractSet acs = ActiveContractSet.empty();
    SearchResult searchResult = (SearchResult) httpResponse.getResult();
    return acs.update(searchResult.getCreatedEvents());
  }

  @Override
  public Flowable<ActiveContractSet> getActiveContracts(ContractQuery query) {
    Flowable<WebSocketResponse> response =
        webSocketClient.post(api.searchContractsForever(), query);
    return response
        .map(this::throwOrGetEvents)
        .scan(ActiveContractSet.empty(), ActiveContractSet::update);
  }
}
