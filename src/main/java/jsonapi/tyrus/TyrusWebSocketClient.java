/*
 * Copyright (c) 2019, Digital Asset (Switzerland) GmbH and/or its affiliates. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package jsonapi.tyrus;

import io.reactivex.Flowable;
import io.reactivex.processors.PublishProcessor;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.Arrays;
import java.util.List;
import javax.websocket.ClientEndpointConfig;
import javax.websocket.DeploymentException;
import javax.websocket.Session;
import jsonapi.http.EmittingWebSocketEndpoint;
import jsonapi.http.WebSocketClient;
import jsonapi.http.WebSocketResponse;
import jsonapi.json.JsonDeserializer;
import jsonapi.json.JsonSerializer;
import org.glassfish.tyrus.client.ClientManager;
import org.glassfish.tyrus.container.jdk.client.JdkClientContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TyrusWebSocketClient implements WebSocketClient {
  private final Logger logger = LoggerFactory.getLogger(getClass().getCanonicalName());

  private final JsonDeserializer<WebSocketResponse> fromJson;
  private final JsonSerializer toJson;
  private final ClientEndpointConfig config;
  private final ClientManager client;

  public TyrusWebSocketClient(
      JsonDeserializer<WebSocketResponse> fromJson, JsonSerializer toJson, String jwt) {
    this.fromJson = fromJson;
    this.toJson = toJson;
    List<String> subProtocols = Arrays.asList("jwt.token." + jwt, "daml.ws.auth");
    this.config = ClientEndpointConfig.Builder.create().preferredSubprotocols(subProtocols).build();
    this.client = ClientManager.createClient(JdkClientContainer.class.getName());
  }

  TyrusWebSocketClient(
      JsonDeserializer<WebSocketResponse> fromJson,
      JsonSerializer toJson,
      ClientEndpointConfig config,
      ClientManager client) {
    this.fromJson = fromJson;
    this.toJson = toJson;
    this.config = config;
    this.client = client;
  }

  @Override
  public Flowable<WebSocketResponse> post(URI resource, Object body) {
    String query = toJson.apply(body);
    Flowable<WebSocketResponse> source =
        Flowable.defer(() -> createWebSocketPublisher(resource, query));
    return source.publish().autoConnect();
  }

  private Flowable<WebSocketResponse> createWebSocketPublisher(URI resource, String query)
      throws IOException, DeploymentException {
    PublishProcessor<String> broadcaster = PublishProcessor.create();
    Session session =
        client.connectToServer(new EmittingWebSocketEndpoint(broadcaster), config, resource);
    session.getBasicRemote().sendText(query, true);
    return broadcaster.map(this::toWebSocketResponse);
  }

  private WebSocketResponse toWebSocketResponse(String message) {
    logger.trace("Received WebSocketResponse: {}", message);
    InputStream json = new ByteArrayInputStream(message.getBytes());
    return fromJson.apply(json);
  }
}
