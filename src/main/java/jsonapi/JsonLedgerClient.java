/*
 * Copyright (c) 2019, Digital Asset (Switzerland) GmbH and/or its affiliates. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package jsonapi;

import java.io.IOException;
import java.net.URI;
import java.util.Arrays;
import java.util.concurrent.CountDownLatch;
import java.util.function.Function;
import javax.websocket.ClientEndpointConfig;
import javax.websocket.ClientEndpointConfig.Builder;
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

  private static final String JWT_TOKEN =
      "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJodHRwczovL2RhbWwuY29tL2xlZGdlci1hcGkiOnsibGVkZ2VySWQiOiJTYW1wbGVMZWRnZXIiLCJhcHBsaWNhdGlvbklkIjoibWFya2V0LWRhdGEtc2VydmljZSIsImFjdEFzIjpbIk9wZXJhdG9yIl19fQ.zjSsXQVooI4Fe-hwYKiyZK3JnZp540Rtno5kh9iwJVA";

  private final Function<Object, String> objectToJsonMapper;

  private URI contracts = URI.create("http://localhost:7575/contracts/search");
  private URI exercise = URI.create("http://localhost:7575/command/exercise");
  private Request exerciseCommand =
      Request.Post(exercise)
          .addHeader("Content-Type", "application/json")
          .addHeader("Authorization", "Bearer " + JWT_TOKEN)
          .connectTimeout(1_000);
  private Request activeContracts =
      Request.Get(contracts)
          .addHeader("Content-Type", "application/json")
          .addHeader("Authorization", "Bearer " + JWT_TOKEN)
          .connectTimeout(1_000);

  public JsonLedgerClient(Function<Object, String> objectToJsonMapper) {
    this.objectToJsonMapper = objectToJsonMapper;
  }

  public String exerciseChoice(ExerciseChoiceData exerciseChoiceData) throws IOException {
    String body = objectToJsonMapper.apply(exerciseChoiceData);
    return exerciseCommand
        .bodyString(body, ContentType.APPLICATION_JSON)
        .execute()
        .returnContent()
        .asString();
  }

  public String getActiveContracts() throws IOException {
    return activeContracts.execute().returnContent().asString();
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
        Builder.create()
            .preferredSubprotocols(Arrays.asList("jwt.token." + JWT_TOKEN, "daml.ws.auth"))
            .build();

    client.connectToServer(endpoint, config, wsActiveContracts);
  }
}
