/*
 * Copyright (c) 2019, Digital Asset (Switzerland) GmbH and/or its affiliates. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package jsonapi;

import com.daml.ledger.javaapi.data.CreateCommand;
import com.daml.ledger.javaapi.data.ExerciseCommand;
import io.reactivex.Flowable;
import java.util.Collection;
import java.util.Collections;
import jsonapi.events.Event;
import jsonapi.http.Api;
import jsonapi.http.HttpClient;
import jsonapi.http.HttpResponse;
import jsonapi.http.HttpResponse.SearchResult;
import jsonapi.http.WebSocketClient;
import jsonapi.http.WebSocketResponse;
import jsonapi.json.JsonSerializer;

public class JsonLedgerClient implements LedgerClient {

  private final HttpClient httpClient;
  private final WebSocketClient webSocketClient;
  private final JsonSerializer toJson;
  private final Api api;

  private void throwIfStatusIsNot200(HttpResponse httpResponse) throws RuntimeException {
    if (httpResponse.getStatus() != 200) {
      throw new RuntimeException(toJson.apply(httpResponse.getErrors()));
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

  public JsonLedgerClient(
      HttpClient httpClient, WebSocketClient webSocketClient, JsonSerializer toJson, Api api) {
    this.httpClient = httpClient;
    this.webSocketClient = webSocketClient;
    this.toJson = toJson;
    this.api = api;
  }

  @Override
  public String create(CreateCommand command) {
    HttpResponse httpResponse = httpClient.post(api.createContract(), command);
    throwIfStatusIsNot200(httpResponse);
    // TODO: Return type safe result
    return toJson.apply(httpResponse);
  }

  @Override
  public String exerciseChoice(ExerciseCommand command) {
    HttpResponse httpResponse = httpClient.post(api.exercise(), command);
    throwIfStatusIsNot200(httpResponse);
    // TODO: Return type safe result
    return toJson.apply(httpResponse);
  }

  @Override
  public ActiveContractSet getActiveContracts() {
    HttpResponse httpResponse = httpClient.get(api.searchContract());
    throwIfStatusIsNot200(httpResponse);
    ActiveContractSet acs = ActiveContractSet.empty();
    // TODO: Eliminate the need for casting.
    SearchResult searchResult = (SearchResult) httpResponse.getResult();
    return acs.update(searchResult.getCreatedEvents());
  }

  // TODO: Eliminate code duplication, fix interface

  @Override
  public ActiveContractSet queryContracts(ContractQuery query) {
    HttpResponse httpResponse = httpClient.post(api.searchContract(), query);
    throwIfStatusIsNot200(httpResponse);
    ActiveContractSet acs = ActiveContractSet.empty();
    // TODO: Eliminate the need for casting.
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
