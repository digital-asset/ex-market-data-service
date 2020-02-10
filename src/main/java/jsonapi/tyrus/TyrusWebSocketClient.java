/*
 * Copyright (c) 2019, Digital Asset (Switzerland) GmbH and/or its affiliates. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package jsonapi.tyrus;

import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import io.reactivex.processors.PublishProcessor;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.URI;
import java.util.Arrays;
import java.util.List;
import javax.websocket.ClientEndpointConfig;
import javax.websocket.Endpoint;
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

  @Override
  public Flowable<WebSocketResponse> post(URI resource, Object body) {
    Flowable<String> source =
        Flowable.create(
            emitter -> {
              String query = toJson.apply(body);
              Endpoint endpoint = new EmittingWebSocketEndpoint(emitter, query);
              client.connectToServer(endpoint, config, resource);
            },
            BackpressureStrategy.LATEST);
    return source
        // TODO: Either deserialize or ignore heartbeats, so that we can emit InputStreams directly.
        .filter(this::nonHeartbeat)
        .map(this::toWebSocketResponse)
        .subscribeWith(PublishProcessor.create());
  }

  private boolean nonHeartbeat(String message) {
    return !message.contains("heartbeat");
  }

  private WebSocketResponse toWebSocketResponse(String message) {
    InputStream json = new ByteArrayInputStream(message.getBytes());
    return fromJson.apply(json);
  }
}
