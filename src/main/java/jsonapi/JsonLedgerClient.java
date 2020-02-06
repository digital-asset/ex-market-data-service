/*
 * Copyright (c) 2019, Digital Asset (Switzerland) GmbH and/or its affiliates. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package jsonapi;

import com.daml.ledger.javaapi.data.ExerciseCommand;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import io.reactivex.Flowable;
import java.security.Key;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import jsonapi.apache.ApacheHttpClient;
import jsonapi.http.Api;
import jsonapi.http.HttpClient;
import jsonapi.http.HttpResponse;
import jsonapi.http.WebSocketClient;
import jsonapi.http.WebSocketResponse;
import jsonapi.json.JsonDeserializer;
import jsonapi.json.JsonSerializer;
import jsonapi.tyrus.TyrusWebSocketClient;

public class JsonLedgerClient {

  private HttpClient httpClient;
  private WebSocketClient webSocketClient;
  private JsonSerializer toJson;
  private Api api = new Api("localhost", 7575);

  public JsonLedgerClient(
      String ledgerId,
      JsonSerializer toJson,
      JsonDeserializer<HttpResponse> fromJson,
      JsonDeserializer<WebSocketResponse> fromJsonWs) {
    this.toJson = toJson;
    Key key = Keys.secretKeyFor(SignatureAlgorithm.HS256);
    Map<String, Object> claim = new HashMap<>();
    claim.put("ledgerId", ledgerId);
    claim.put("applicationId", "market-data-service");
    claim.put("actAs", Collections.singletonList("Operator"));
    Map<String, Object> claims = Collections.singletonMap("https://daml.com/ledger-api", claim);
    String JWT_TOKEN = Jwts.builder().setClaims(claims).signWith(key).compact();
    this.httpClient = new ApacheHttpClient(fromJson, toJson, JWT_TOKEN);
    this.webSocketClient = new TyrusWebSocketClient(fromJsonWs, toJson, JWT_TOKEN);
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
