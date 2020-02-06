/*
 * Copyright (c) 2019, Digital Asset (Switzerland) GmbH and/or its affiliates. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package jsonapi.tyrus;

import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import io.reactivex.FlowableEmitter;
import io.reactivex.FlowableOnSubscribe;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.Arrays;
import java.util.List;
import javax.websocket.ClientEndpointConfig;
import javax.websocket.CloseReason;
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

  private final Logger log = LoggerFactory.getLogger(getClass());

  private final JsonDeserializer<WebSocketResponse> fromJson;
  private final JsonSerializer toJson;
  private ClientEndpointConfig config;

  public TyrusWebSocketClient(
      JsonDeserializer<WebSocketResponse> fromJson, JsonSerializer toJson, String jwt) {
    this.fromJson = fromJson;
    this.toJson = toJson;
    List<String> subProtocols = Arrays.asList("jwt.token." + jwt, "daml.ws.auth");
    this.config = ClientEndpointConfig.Builder.create().preferredSubprotocols(subProtocols).build();
  }

  @Override
  public Flowable<WebSocketResponse> post(URI resource, Object body) {
    WebSocketSource source = new WebSocketSource(resource, body);
    return Flowable.create(source, BackpressureStrategy.LATEST);
  }

  private class WebSocketSource implements FlowableOnSubscribe<WebSocketResponse> {

    private final URI resource;
    private final Object body;
    // TODO: How to keep session alive?
    private Session session;

    public WebSocketSource(URI resource, Object body) {
      this.resource = resource;
      this.body = body;
    }

    @Override
    public void subscribe(FlowableEmitter<WebSocketResponse> emitter) throws Exception {
      ClientManager client = ClientManager.createClient(JdkClientContainer.class.getName());
      session = client.connectToServer(new Endpoint(emitter), config, resource);
    }

    private class EmittingHandler implements Whole<String> {

      private final FlowableEmitter<WebSocketResponse> emitter;

      public EmittingHandler(FlowableEmitter<WebSocketResponse> emitter) {
        this.emitter = emitter;
      }

      @Override
      public void onMessage(String data) {
        log.debug("Received message.");
        log.trace("Message: {}", data);

        // TODO: Implement properly
        if (data.contains("heartbeat")) {
          log.trace("Heartbeat received.");
        } else {
          InputStream json = new ByteArrayInputStream(data.getBytes());
          WebSocketResponse response = fromJson.apply(json);
          emitter.onNext(response);
        }
      }
    }

    private class Endpoint extends javax.websocket.Endpoint {

      private final FlowableEmitter<WebSocketResponse> emitter;

      public Endpoint(FlowableEmitter<WebSocketResponse> emitter) {
        this.emitter = emitter;
      }

      @Override
      public void onOpen(Session session, EndpointConfig endpointConfig) {
        log.debug("Connected.");
        try {
          session.addMessageHandler(new EmittingHandler(emitter));
          String query = toJson.apply(body);
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
      }
    }
  }
}
