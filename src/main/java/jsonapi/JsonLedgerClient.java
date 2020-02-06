/*
 * Copyright (c) 2019, Digital Asset (Switzerland) GmbH and/or its affiliates. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package jsonapi;

import com.daml.ledger.javaapi.data.ExerciseCommand;
import io.reactivex.Flowable;
import java.util.concurrent.CountDownLatch;
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

  public void getActiveContractsViaWebSockets(CountDownLatch countdown) {
    Flowable<WebSocketResponse> webSocketResponse =
        webSocketClient.post(api.searchContractsForever(), null);
    webSocketResponse.subscribe(x -> countdown.countDown());
  }
}
