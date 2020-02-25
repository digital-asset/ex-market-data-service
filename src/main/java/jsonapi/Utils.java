/*
 * Copyright (c) 2019, Digital Asset (Switzerland) GmbH and/or its affiliates. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package jsonapi;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.Collections;
import jsonapi.apache.ApacheHttpClient;
import jsonapi.http.Api;
import jsonapi.http.HttpResponse;
import jsonapi.http.Jwt;
import jsonapi.http.WebSocketResponse;
import jsonapi.json.JsonDeserializer;
import jsonapi.json.JsonSerializer;
import jsonapi.tyrus.TyrusWebSocketClient;
import org.apache.http.client.fluent.Request;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Utils {
  private static final Logger log = LoggerFactory.getLogger("JsonAPI Util");

  public static void waitForJsonApi(String uri) throws Exception {
    Instant started = Instant.now();
    boolean isRunning = false;
    Duration timeout = Duration.ofSeconds(30);
    while (!isRunning && !hasPassedSince(started, timeout)) {
      log.info("Waiting for JSON API...");
      try {
        org.apache.http.HttpResponse response = Request.Options(uri).execute().returnResponse();
        if (response.getStatusLine().getStatusCode() < 500) {
          isRunning = true;
        }
      } catch (IOException ignored) {
      }
      Thread.sleep(1000);
    }
    if (!isRunning) {
      throw notAvailableWithin(timeout);
    }
    log.info("JSON API available.");
  }

  private static Exception notAvailableWithin(Duration timeout) {
    return new Exception("JSON API not available within " + timeout.toMillis() + "ms timout");
  }

  private static boolean hasPassedSince(Instant started, Duration timeout) {
    Duration elapsed = Duration.between(started, Instant.now());
    return elapsed.compareTo(timeout) > 0;
  }

  public static LedgerClient createJsonLedgerClient(
      String ledgerId,
      String party,
      String applicationId,
      JsonDeserializer<HttpResponse> httpResponseDeserializer,
      JsonSerializer jsonSerializer,
      JsonDeserializer<WebSocketResponse> webSocketResponseDeserializer,
      Api api) {
    String jwt = Jwt.createToken(ledgerId, applicationId, Collections.singletonList(party));
    ApacheHttpClient httpClient =
        new ApacheHttpClient(httpResponseDeserializer, jsonSerializer, jwt);
    TyrusWebSocketClient webSocketClient =
        new TyrusWebSocketClient(webSocketResponseDeserializer, jsonSerializer, jwt);
    return new JsonLedgerClient(httpClient, webSocketClient, api);
  }
}
