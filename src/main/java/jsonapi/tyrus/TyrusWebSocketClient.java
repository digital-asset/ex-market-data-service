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
import jsonapi.http.EmittingWebSocketEndpoint;
import jsonapi.http.WebSocketClient;
import jsonapi.http.WebSocketResponse;
import jsonapi.json.JsonDeserializer;
import jsonapi.json.JsonSerializer;
import org.glassfish.tyrus.client.ClientManager;
import org.glassfish.tyrus.container.jdk.client.JdkClientContainer;

public class TyrusWebSocketClient implements WebSocketClient {

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

  // TODO: Visible only for testing. Likely should refactor.
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
    client.connectToServer(new EmittingWebSocketEndpoint(broadcaster, query), config, resource);
    return broadcaster
        // TODO: Either deserialize or ignore heartbeats, so that we can emit InputStreams directly.
        .filter(this::nonHeartbeat)
        .map(this::toWebSocketResponse);
  }

  private boolean nonHeartbeat(String message) {
    return !message.contains("heartbeat");
  }

  private WebSocketResponse toWebSocketResponse(String message) {
    InputStream json = new ByteArrayInputStream(message.getBytes());
    return fromJson.apply(json);
  }
}
