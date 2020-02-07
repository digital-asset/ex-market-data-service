/*
 * Copyright (c) 2019, Digital Asset (Switzerland) GmbH and/or its affiliates. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package jsonapi.http;

import java.util.Collection;
import jsonapi.events.Event;

@SuppressWarnings("PMD.DataClass")
public class WebSocketResponse {

  private final Collection<Event> events;

  public WebSocketResponse(Collection<Event> events) {
    this.events = events;
  }

  public Collection<Event> getEvents() {
    return events;
  }
}
