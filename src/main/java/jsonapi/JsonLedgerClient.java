/*
 * Copyright (c) 2019, Digital Asset (Switzerland) GmbH and/or its affiliates. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package jsonapi;

import com.daml.ledger.javaapi.data.ExerciseCommand;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import java.io.IOException;
import java.net.URI;
import java.security.Key;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.function.Function;
import javax.websocket.ClientEndpointConfig;
import javax.websocket.CloseReason;
import javax.websocket.DeploymentException;
import javax.websocket.Endpoint;
import javax.websocket.EndpointConfig;
import javax.websocket.MessageHandler.Whole;
import javax.websocket.Session;
import org.apache.http.client.fluent.Request;
import org.apache.http.entity.ContentType;
import org.glassfish.tyrus.client.ClientManager;
import org.glassfish.tyrus.container.jdk.client.JdkClientContainer;

public class JsonLedgerClient {
  private final String JWT_TOKEN;
  private final Function<Object, String> objectToJsonMapper;

  private Request exerciseCommand;
  private Request activeContracts;

  public JsonLedgerClient(String ledgerId, Function<Object, String> objectToJsonMapper) {
    this.objectToJsonMapper = objectToJsonMapper;
    Key key = Keys.secretKeyFor(SignatureAlgorithm.HS256);
    Map<String, Object> claim = new HashMap<>();
    claim.put("ledgerId", ledgerId);
    claim.put("applicationId", "market-data-service");
    claim.put("actAs", Collections.singletonList("Operator"));
    Map<String, Object> claims = Collections.singletonMap("https://daml.com/ledger-api", claim);
    this.JWT_TOKEN = Jwts.builder().setClaims(claims).signWith(key).compact();
    URI exercise = URI.create("http://localhost:7575/command/exercise");
    exerciseCommand =
        Request.Post(exercise)
            .addHeader("Content-Type", "application/json")
            .addHeader("Authorization", "Bearer " + JWT_TOKEN)
            .connectTimeout(1_000);
    URI contracts = URI.create("http://localhost:7575/contracts/search");
    activeContracts =
        Request.Get(contracts)
            .addHeader("Content-Type", "application/json")
            .addHeader("Authorization", "Bearer " + JWT_TOKEN)
            .connectTimeout(1_000);
  }

  public String exerciseChoice(ExerciseChoiceData exerciseChoiceData) throws IOException {
    String body = objectToJsonMapper.apply(exerciseChoiceData);
    return executeRequest(exerciseCommand.bodyString(body, ContentType.APPLICATION_JSON));
  }

  public String exerciseChoice(ExerciseCommand command) throws IOException {
    String body = objectToJsonMapper.apply(exerciseCommand);
    return executeRequest(exerciseCommand.bodyString(body, ContentType.APPLICATION_JSON));
  }

  public String getActiveContracts() throws IOException {
    return executeRequest(activeContracts);
  }

  private String executeRequest(Request request) throws IOException {
    return request.execute().returnContent().asString();
  }

  public void getActiveContractsViaWebSockets(CountDownLatch countdown)
      throws IOException, DeploymentException {
    URI wsActiveContracts = URI.create("ws://localhost:7575/contracts/searchForever");

    ClientManager client = ClientManager.createClient(JdkClientContainer.class.getName());
    Endpoint endpoint =
        new Endpoint() {
          @Override
          public void onOpen(Session session, EndpointConfig endpointConfig) {
            System.out.println("Connected.");
            try {
              session.addMessageHandler(
                  new Whole<String>() {
                    @Override
                    public void onMessage(String data) {
                      System.out.printf("Received message: %s.%n", data);
                      if (!data.contains("heartbeat")) {
                        countdown.countDown();
                      }
                    }
                  });
              session
                  .getBasicRemote()
                  .sendText(
                      "{\"templateIds\": [\"DA.TimeService.TimeService:CurrentTime\"]}", true);
            } catch (IOException e) {
              e.printStackTrace();
            }
          }

          @Override
          public void onClose(Session session, CloseReason closeReason) {
            System.out.println("Closed.");
            countdown.countDown();
          }
        };

    ClientEndpointConfig config =
        ClientEndpointConfig.Builder.create()
            .preferredSubprotocols(Arrays.asList("jwt.token." + JWT_TOKEN, "daml.ws.auth"))
            .build();

    client.connectToServer(endpoint, config, wsActiveContracts);
  }
}
