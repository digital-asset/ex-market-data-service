/*
 * Copyright (c) 2019, Digital Asset (Switzerland) GmbH and/or its affiliates. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package com.digitalasset.jsonapi.tyrus;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;

import com.digitalasset.jsonapi.http.EmittingWebSocketEndpoint;
import com.digitalasset.jsonapi.http.WebSocketResponse;
import io.reactivex.Flowable;
import java.io.IOException;
import javax.websocket.DeploymentException;
import org.glassfish.tyrus.client.ClientManager;
import org.junit.Test;

public class TyrusWebSocketClientTest {
  @Test
  public void webSocketIsConnectedOnlyOnce() throws IOException, DeploymentException {
    ClientManager webSocketClient = mock(ClientManager.class);
    TyrusWebSocketClient client = new TyrusWebSocketClient(null, o -> null, null, webSocketClient);

    Flowable<WebSocketResponse> response = client.post(null, null);
    response.subscribe();
    response.subscribe();
    response.subscribe();

    verify(webSocketClient, times(1))
        .connectToServer(any(EmittingWebSocketEndpoint.class), any(), any());
  }

  @Test
  public void webSocketIsConnectedOnlyOnFirstSubscription() {
    ClientManager webSocketClient = mock(ClientManager.class);
    TyrusWebSocketClient client = new TyrusWebSocketClient(null, o -> null, null, webSocketClient);

    client.post(null, null);
    // No subscription

    verifyZeroInteractions(webSocketClient);
  }
}
