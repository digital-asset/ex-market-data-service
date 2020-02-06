/*
 * Copyright (c) 2019, Digital Asset (Switzerland) GmbH and/or its affiliates. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package jsonapi.tyrus;

import io.reactivex.BackpressureStrategy;
import io.reactivex.Emitter;
import io.reactivex.Flowable;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.Arrays;
import java.util.List;
import javax.websocket.ClientEndpointConfig;
import javax.websocket.CloseReason;
import javax.websocket.Endpoint;
import javax.websocket.EndpointConfig;
import javax.websocket.MessageHandler.Whole;
import javax.websocket.Session;
import jsonapi.http.WebSocketClient;
import jsonapi.http.WebSocketResponse;
import jsonapi.json.JsonDeserializer;
import jsonapi.json.JsonSerializer;
import org.glassfish.tyrus.client.ClientManager;
import org.glassfish.tyrus.container.jdk.client.JdkClientContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
    return source.filter(this::nonHeartbeat).map(this::toWebSocketResponse);
  }

  private boolean nonHeartbeat(String message) {
    return !message.contains("heartbeat");
  }

  private WebSocketResponse toWebSocketResponse(String message) {
    InputStream json = new ByteArrayInputStream(message.getBytes());
    return fromJson.apply(json);
  }

  private static class EmittingWebSocketEndpoint extends javax.websocket.Endpoint {
    private final Logger log = LoggerFactory.getLogger(getClass());

    private final Emitter<String> emitter;
    private final String query;

    public EmittingWebSocketEndpoint(Emitter<String> emitter, String query) {
      this.emitter = emitter;
      this.query = query;
    }

    @Override
    public void onOpen(Session session, EndpointConfig endpointConfig) {
      log.debug("Connected.");
      try {
        Whole<String> messageHandler = new MessageHandler();
        session.addMessageHandler(messageHandler);
        session.getBasicRemote().sendText(query, true);
      } catch (IOException e) {
        e.printStackTrace();
      }
    }

    @Override
    public void onError(Session session, Throwable error) {
      emitter.onError(error);
    }

    @Override
    public void onClose(Session session, CloseReason closeReason) {
      log.debug("Closed.");
      emitter.onComplete();
    }

    private class MessageHandler implements Whole<String> {

      @Override
      public void onMessage(String message) {
        log.trace("Received message: {}.", message);
        emitter.onNext(message);
      }
    }
  }
}
