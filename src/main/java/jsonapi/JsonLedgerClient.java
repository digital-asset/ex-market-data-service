/*
 * Copyright (c) 2019, Digital Asset (Switzerland) GmbH and/or its affiliates. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package jsonapi;

import com.daml.ledger.javaapi.data.ExerciseCommand;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.Builder;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.net.http.WebSocket;
import java.net.http.WebSocket.Listener;
import java.security.Key;
import java.time.Duration;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Future;
import java.util.function.Function;

public class JsonLedgerClient {
  //
  // "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJodHRwczovL2RhbWwuY29tL2xlZGdlci1hcGkiOnsibGVkZ2VySWQiOiJTYW1wbGVMZWRnZXIiLCJhcHBsaWNhdGlvbklkIjoibWFya2V0LWRhdGEtc2VydmljZSIsImFjdEFzIjpbIk9wZXJhdG9yIl19fQ.zjSsXQVooI4Fe-hwYKiyZK3JnZp540Rtno5kh9iwJVA";
  private final String JWT_TOKEN;
  private final Function<Object, String> objectToJsonMapper;

  private HttpClient http = HttpClient.newHttpClient();
  private URI contractByKey = URI.create("http://localhost:7575/contracts/lookup");
  private URI contracts = URI.create("http://localhost:7575/contracts/search");
  private URI exercise = URI.create("http://localhost:7575/command/exercise");
  private Builder requestBuilder;

  public JsonLedgerClient(String ledgerId, Function<Object, String> objectToJsonMapper)
      throws UnsupportedEncodingException {
    this.objectToJsonMapper = objectToJsonMapper;
    Key key = Keys.secretKeyFor(SignatureAlgorithm.HS256);
    Map<String, Object> claims =
        Collections.singletonMap(
            "https://daml.com/ledger-api",
            Map.of(
                "ledgerId",
                ledgerId,
                "applicationId",
                "market-data-service",
                "actAs",
                Arrays.asList("Operator")));
    this.JWT_TOKEN = Jwts.builder().setClaims(claims).signWith(key).compact();
    System.out.println(JWT_TOKEN);
    this.requestBuilder =
        HttpRequest.newBuilder()
            .header("Content-Type", "application/json")
            .header("Authorization", "Bearer " + JWT_TOKEN)
            .timeout(Duration.ofSeconds(1));
  }

  public Future<HttpResponse<String>> exerciseChoice(ExerciseChoiceData exerciseChoiceData) {
    HttpRequest.BodyPublisher body =
        HttpRequest.BodyPublishers.ofString(objectToJsonMapper.apply(exerciseChoiceData));
    var request = requestBuilder.uri(exercise).POST(body).build();
    return http.sendAsync(request, BodyHandlers.ofString());
  }

  public Future<HttpResponse<String>> getContractByKey(HttpRequest.BodyPublisher post) {
    var request = requestBuilder.uri(contractByKey).POST(post).build();
    return http.sendAsync(request, BodyHandlers.ofString());
  }

  public Future<HttpResponse<String>> exerciseChoice(ExerciseCommand exerciseCommand) {
    HttpRequest.BodyPublisher body =
        HttpRequest.BodyPublishers.ofString(objectToJsonMapper.apply(exerciseCommand));
    var request = requestBuilder.uri(exercise).POST(body).build();
    return http.sendAsync(request, BodyHandlers.ofString());
  }

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
            webSocket.sendText(
                "{\"templateIds\": [\"DA.TimeService.TimeService:CurrentTime\"]}", true);
            Listener.super.onOpen(webSocket);
          }

          @Override
          public CompletionStage<?> onText(WebSocket webSocket, CharSequence data, boolean last) {
            System.out.printf("Received message: %s.%n", data);
            if (!data.toString().contains("heartbeat")) {
              countdown.countDown();
            }
            return Listener.super.onText(webSocket, data, last);
          }

          @Override
          public CompletionStage<?> onClose(WebSocket webSocket, int statusCode, String reason) {
            System.out.printf("Closed. Status %d, reason: %s.%n", statusCode, reason);
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
