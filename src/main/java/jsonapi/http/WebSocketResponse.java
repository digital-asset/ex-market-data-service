/*
 * Copyright (c) 2019, Digital Asset (Switzerland) GmbH and/or its affiliates. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package jsonapi.http;

import java.util.Collection;
import java.util.stream.Collectors;
import jsonapi.events.Event;

@SuppressWarnings("PMD.DataClass")
public class WebSocketResponse {

  // We keep them nullable references instead of using Optional for easier Gson parsing
  private final Collection<EventHolder> events;
  private final String error;

  public WebSocketResponse(Collection<EventHolder> events, String error) {
    if (events == null && error == null) {
      throw new RuntimeException(
          "WebSocketResponse precondition failure: both events and error are null");
    }
    this.events = events;
    this.error = error;
  }

  public Collection<Event> getEvents() {
    if (error != null) {
      throw new RuntimeException("Websocket: " + error);
    } else {
      return events.stream().map(EventHolder::event).collect(Collectors.toList());
    }
  }

  public String getError() {
    return error;
  }
}
