/*
 * Copyright (c) 2019, Digital Asset (Switzerland) GmbH and/or its affiliates. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package com.daml.extensions.jsonapi;

import com.daml.extensions.jsonapi.http.Api;
import com.daml.extensions.jsonapi.http.HttpClient;
import com.daml.extensions.jsonapi.http.HttpResponse;
import com.daml.extensions.jsonapi.http.WebSocketClient;
import com.daml.extensions.jsonapi.http.WebSocketResponse;
import io.reactivex.Flowable;
import java.net.URI;
import java.util.Collections;
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
  private static final HttpClient httpClient = new DummyHttpClientGivingError();
  private static final Api api = new Api(null, 0);

  @Rule public ExpectedException exceptionRule = ExpectedException.none();

  @Test
  public void createThrowsForHttpErrorAndIncludesMessage() {
    JsonLedgerClient ledger = new JsonLedgerClient(httpClient, null, api);
    exceptionRule.expectMessage(someBadRequestErrorMessage);
    ledger.create(null);
  }

  @Test
  public void exerciseChoiceThrowsForHttpErrorAndIncludesMessage() {
    JsonLedgerClient ledger = new JsonLedgerClient(httpClient, null, api);
    exceptionRule.expectMessage(someBadRequestErrorMessage);
    ledger.exerciseChoice(null);
  }

  @Test
  public void queryContractsThrowsForHttpErrorAndIncludesMessage() {
    JsonLedgerClient ledger = new JsonLedgerClient(httpClient, null, api);
    exceptionRule.expectMessage(someBadRequestErrorMessage);
    ledger.queryContracts(null);
  }

  @Test
  public void getActiveContractsThrowsForWebSocketErrorAndIncludesMessage() {
    WebSocketClientGivingError webSocketClient =
        new WebSocketClientGivingError(new WebSocketResponse(null, "some error", null, null, null));
    JsonLedgerClient ledger = new JsonLedgerClient(null, webSocketClient, api);
    exceptionRule.expectMessage("some error");
    Flowable<ActiveContractSet> activeContracts = ledger.getActiveContracts(null);
    activeContracts.blockingLast();
  }

  @Test
  public void getActiveContractsThrowsForWebSocketWarningAndIncludesMessage() {
    WebSocketClientGivingError webSocketClient =
        new WebSocketClientGivingError(
            new WebSocketResponse(null, null, "some warning", null, null));
    JsonLedgerClient ledger = new JsonLedgerClient(null, webSocketClient, api);
    exceptionRule.expectMessage("some warning");
    Flowable<ActiveContractSet> activeContracts = ledger.getActiveContracts(null);
    activeContracts.blockingLast();
  }
}
