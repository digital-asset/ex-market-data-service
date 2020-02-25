/*
 * Copyright (c) 2019, Digital Asset (Switzerland) GmbH and/or its affiliates. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package jsonapi.http;

import io.reactivex.FlowableSubscriber;
import javax.websocket.CloseReason;
import javax.websocket.EndpointConfig;
import javax.websocket.MessageHandler.Whole;
import javax.websocket.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EmittingWebSocketEndpoint extends javax.websocket.Endpoint {
  private final Logger log = LoggerFactory.getLogger(getClass());

  private final FlowableSubscriber<String> subscriber;

  public EmittingWebSocketEndpoint(FlowableSubscriber<String> subscriber) {
    this.subscriber = subscriber;
  }

  @Override
  public void onOpen(Session session, EndpointConfig endpointConfig) {
    log.debug("Connected.");
    Whole<String> messageHandler = new MessageHandler();
    session.addMessageHandler(messageHandler);
  }

  @Override
  public void onError(Session session, Throwable error) {
    subscriber.onError(error);
  }

  @Override
  public void onClose(Session session, CloseReason closeReason) {
    log.debug("Closed.");
    subscriber.onComplete();
  }

  private class MessageHandler implements Whole<String> {

    @Override
    public void onMessage(String message) {
      log.trace("Received message: {}.", message);
      subscriber.onNext(message);
    }
  }
}
