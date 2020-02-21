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

  private final Collection<EventHolder> events;
  private final String error;

  public WebSocketResponse(Collection<EventHolder> events, String error) {
    this.events = events;
    this.error = error;
  }

  public Collection<Event> getEvents() {
    return events.stream().map(EventHolder::event).collect(Collectors.toList());
  }

  public String getError() {
    return error;
  }
}
