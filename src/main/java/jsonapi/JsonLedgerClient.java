/*
 * Copyright (c) 2019, Digital Asset (Switzerland) GmbH and/or its affiliates. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package jsonapi;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.Builder;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.net.http.WebSocket;
import java.net.http.WebSocket.Listener;
import java.time.Duration;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Future;

public class JsonLedgerClient {

  private static final String JWT_TOKEN =
      "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJsZWRnZXJJZCI6IlNhbXBsZUxlZGdlciIsImFwcGxpY2F0aW9uSWQiOiJtYXJrZXQtZGF0YS1zZXJ2aWNlIiwicGFydHkiOiJPcGVyYXRvciJ9.hhRLtiyZ4kUkuEGkJZw9cjAoVkKB4MbYg85VJkoo4yo";
  private HttpClient http = HttpClient.newHttpClient();
  private URI contracts = URI.create("http://localhost:7575/contracts/search");
  private Builder requestBuilder =
      HttpRequest.newBuilder()
          .header("Content-Type", "application/json")
          .header("Authorization", "Bearer " + JWT_TOKEN)
          .timeout(Duration.ofSeconds(1));

  public Future<HttpResponse<String>> getActiveContracts() {
    var request = requestBuilder.uri(contracts).GET().build();
    return http.sendAsync(request, BodyHandlers.ofString());
  }

  public WebSocket getActiveContractsViaWebSockets(CountDownLatch countdown) {
    var activeContracts = URI.create("ws://localhost:7575/contracts/searchForever");

    Listener listener =
        new Listener() {
          @Override
          public void onOpen(WebSocket webSocket) {
            System.out.println("Connected.");
            Listener.super.onOpen(webSocket);
          }

          @Override
          public CompletionStage<?> onText(WebSocket webSocket, CharSequence data, boolean last) {
            System.out.printf("Received message: %s%n.", data);
            return Listener.super.onText(webSocket, data, last);
          }

          @Override
          public CompletionStage<?> onClose(WebSocket webSocket, int statusCode, String reason) {
            System.out.printf("Closed. Status %d, reason: %s%n.", statusCode, reason);
            countdown.countDown();
            return Listener.super.onClose(webSocket, statusCode, reason);
          }
        };

    return http.newWebSocketBuilder()
        .subprotocols("jwt.token." + JWT_TOKEN, "daml.ws.auth")
        .buildAsync(activeContracts, listener)
        .join();
  }
}
