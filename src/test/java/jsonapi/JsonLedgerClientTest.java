/*
 * Copyright (c) 2019, Digital Asset (Switzerland) GmbH and/or its affiliates. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package jsonapi;

import io.reactivex.Flowable;
import java.net.URI;
import java.util.Collections;
import jsonapi.gson.GsonSerializer;
import jsonapi.http.Api;
import jsonapi.http.HttpClient;
import jsonapi.http.HttpResponse;
import jsonapi.http.WebSocketClient;
import jsonapi.http.WebSocketResponse;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class JsonLedgerClientTest {

  private static class DummyHttpClientGivingError implements HttpClient {
    @Override
    public HttpResponse get(URI resource) {
      return badHttpResponse;
    }

    @Override
    public HttpResponse post(URI resource, Object body) {
      return badHttpResponse;
    }
  }

  private static class WebSocketClientGivingError implements WebSocketClient {
    private final WebSocketResponse emulatedResponse;

    public WebSocketClientGivingError(WebSocketResponse emulatedResponse) {
      this.emulatedResponse = emulatedResponse;
    }

    @Override
    public Flowable<WebSocketResponse> post(URI resource, Object body) {
      return Flowable.just(emulatedResponse);
    }
  }

  private static final String someBadRequestErrorMessage = "some bad request error message";
  private static final HttpResponse badHttpResponse =
      new HttpResponse(400, null, Collections.singletonList(someBadRequestErrorMessage), null);
  private static final GsonSerializer jsonSerializer = new GsonSerializer();
  private static final HttpClient httpClient = new DummyHttpClientGivingError();
  private static final Api api = new Api(null, 0);

  @Rule public ExpectedException exceptionRule = ExpectedException.none();

  @Test
  public void createThrowsForHttpErrorAndIncludesMessage() {
    JsonLedgerClient ledger = new JsonLedgerClient(httpClient, null, jsonSerializer, api);
    exceptionRule.expectMessage(someBadRequestErrorMessage);
    ledger.create(null);
  }

  @Test
  public void exerciseChoiceThrowsForHttpErrorAndIncludesMessage() {
    JsonLedgerClient ledger = new JsonLedgerClient(httpClient, null, jsonSerializer, api);
    exceptionRule.expectMessage(someBadRequestErrorMessage);
    ledger.exerciseChoice(null);
  }

  @Test
  public void getActiveContractsThrowsForHttpErrorAndIncludesMessage() {
    JsonLedgerClient ledger = new JsonLedgerClient(httpClient, null, jsonSerializer, api);
    exceptionRule.expectMessage(someBadRequestErrorMessage);
    ledger.getActiveContracts();
  }

  @Test
  public void queryContractsThrowsForHttpErrorAndIncludesMessage() {
    JsonLedgerClient ledger = new JsonLedgerClient(httpClient, null, jsonSerializer, api);
    exceptionRule.expectMessage(someBadRequestErrorMessage);
    ledger.queryContracts(null);
  }

  @Test
  public void getActiveContractsThrowsForWebSocketErrorAndIncludesMessage() {
    WebSocketClientGivingError webSocketClient =
        new WebSocketClientGivingError(new WebSocketResponse(null, "some error", null));
    JsonLedgerClient ledger = new JsonLedgerClient(null, webSocketClient, jsonSerializer, api);
    exceptionRule.expectMessage("some error");
    Flowable<ActiveContractSet> activeContracts = ledger.getActiveContracts(null);
    activeContracts.blockingLast();
  }

  @Test
  public void getActiveContractsThrowsForWebSocketWarningAndIncludesMessage() {
    WebSocketClientGivingError webSocketClient =
        new WebSocketClientGivingError(new WebSocketResponse(null, null, "some warning"));
    JsonLedgerClient ledger = new JsonLedgerClient(null, webSocketClient, jsonSerializer, api);
    exceptionRule.expectMessage("some warning");
    Flowable<ActiveContractSet> activeContracts = ledger.getActiveContracts(null);
    activeContracts.blockingLast();
  }
}
