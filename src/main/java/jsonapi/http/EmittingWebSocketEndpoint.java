/*
 * Copyright (c) 2019, Digital Asset (Switzerland) GmbH and/or its affiliates. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package jsonapi.http;

import io.reactivex.Emitter;
import java.io.IOException;
import javax.websocket.CloseReason;
import javax.websocket.EndpointConfig;
import javax.websocket.MessageHandler.Whole;
import javax.websocket.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EmittingWebSocketEndpoint extends javax.websocket.Endpoint {
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
      // TODO: Factor out initial message sending.
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
