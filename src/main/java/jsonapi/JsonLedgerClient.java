/*
 * Copyright (c) 2019, Digital Asset (Switzerland) GmbH and/or its affiliates. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package jsonapi;

import com.daml.ledger.javaapi.data.ExerciseCommand;
import com.daml.ledger.javaapi.data.TransactionFilter;
import io.reactivex.Flowable;
import java.util.Set;
import jsonapi.http.Api;
import jsonapi.http.HttpClient;
import jsonapi.http.HttpResponse;
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

  public String exerciseChoice(ExerciseCommand command) {
    HttpResponse httpResponse = httpClient.post(api.exercise(), command);
    // TODO: Return type safe result
    return toJson.apply(httpResponse);
  }

  public String getActiveContracts() {
    HttpResponse httpResponse = httpClient.get(api.searchContract());
    // TODO: Return type safe result
    return toJson.apply(httpResponse);
  }

  public Flowable<Set<ActiveContract>> getActiveContracts(TransactionFilter transactionFilter) {
    Flowable<WebSocketResponse> response = webSocketClient.post(api.searchContractsForever(), null);
    // TODO: Convert to events (created, archive, error)
    // TODO: Calculate the actual ACS (may include contracts received in earlier responses)
    return response.map(this::toTemplates);
  }

  private Set<ActiveContract> toTemplates(WebSocketResponse x) {
    throw new UnsupportedOperationException("Not yet implemented.");
  }
}
