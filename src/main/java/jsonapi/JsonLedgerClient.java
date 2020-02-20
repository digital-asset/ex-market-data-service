/*
 * Copyright (c) 2019, Digital Asset (Switzerland) GmbH and/or its affiliates. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package jsonapi;

import com.daml.ledger.javaapi.data.CreateCommand;
import com.daml.ledger.javaapi.data.ExerciseCommand;
import io.reactivex.Flowable;
import jsonapi.http.Api;
import jsonapi.http.HttpClient;
import jsonapi.http.HttpResponse;
import jsonapi.http.HttpResponse.SearchResult;
import jsonapi.http.WebSocketClient;
import jsonapi.http.WebSocketResponse;
import jsonapi.json.JsonSerializer;

public class JsonLedgerClient {

  private final HttpClient httpClient;
  private final WebSocketClient webSocketClient;
  private final JsonSerializer toJson;
  private final Api api;

  public JsonLedgerClient(
      HttpClient httpClient, WebSocketClient webSocketClient, JsonSerializer toJson, Api api) {
    this.httpClient = httpClient;
    this.webSocketClient = webSocketClient;
    this.toJson = toJson;
    this.api = api;
  }

  public String create(CreateCommand command) {
    HttpResponse httpResponse = httpClient.post(api.createContract(), command);
    // TODO: Return type safe result
    return toJson.apply(httpResponse);
  }

  public String exerciseChoice(ExerciseCommand command) {
    HttpResponse httpResponse = httpClient.post(api.exercise(), command);
    if (httpResponse.getStatus() != 200)
      throw new RuntimeException(toJson.apply(httpResponse.getErrors()));
    // TODO: Return type safe result
    return toJson.apply(httpResponse);
  }

  public ActiveContractSet getActiveContracts() {
    HttpResponse httpResponse = httpClient.get(api.searchContract());
    ActiveContractSet acs = ActiveContractSet.empty();
    // TODO: Eliminate the need for casting.
    SearchResult searchResult = (SearchResult) httpResponse.getResult();
    return acs.update(searchResult.getCreatedEvents());
  }

  // TODO: Eliminate code duplication, fix interface
  public ActiveContractSet queryContracts(ContractQuery query) {
    HttpResponse httpResponse = httpClient.post(api.searchContract(), query);
    ActiveContractSet acs = ActiveContractSet.empty();
    // TODO: Eliminate the need for casting.
    SearchResult searchResult = (SearchResult) httpResponse.getResult();
    return acs.update(searchResult.getCreatedEvents());
  }

  public Flowable<ActiveContractSet> getActiveContracts(ContractQuery query) {
    Flowable<WebSocketResponse> response =
        webSocketClient.post(api.searchContractsForever(), query);
    // TODO: Convert to events (created, archive, error)
    return response
        .map(WebSocketResponse::getEvents)
        .scan(ActiveContractSet.empty(), ActiveContractSet::update);
  }
}
