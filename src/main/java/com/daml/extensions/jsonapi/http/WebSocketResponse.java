/*
 * Copyright (c) 2019, Digital Asset (Switzerland) GmbH and/or its affiliates. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package com.daml.extensions.jsonapi.http;

import com.daml.extensions.jsonapi.events.Event;
import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;

@SuppressWarnings("PMD.DataClass")
public class WebSocketResponse {

  // We keep them nullable references instead of using Optional for easier Gson parsing
  // A better design could be to have polymorphic classes but the grammar (i.e. the allowed
  // combination of non-null fields) is not precisely defined yet. Or use builder pattern here.
  private final Collection<EventHolder> events;
  private final String error;
  private final Object warnings;
  private final String heartbeat;
  private final Boolean live;

  public WebSocketResponse(
      Collection<EventHolder> events,
      String error,
      Object warnings,
      String heartbeat,
      Boolean live) {
    this.events = events;
    this.error = error;
    this.warnings = warnings;
    this.heartbeat = heartbeat;
    this.live = live;
  }

  // Should not return empty list for null events, because events==null means
  // some application error in this ref app (because the websocket error should have been detected
  // and escalated). In other words, returning empty list for events==null would silently swallow an
  // error.
  public Optional<Collection<Event>> getEvents() {
    return Optional.ofNullable(events)
        .map(events -> events.stream().map(EventHolder::event).collect(Collectors.toList()));
  }

  public Optional<String> getError() {
    return Optional.ofNullable(error);
  }

  public Optional<Object> getWarnings() {
    return Optional.ofNullable(warnings);
  }

  public Optional<String> getHeartbeat() {
    return Optional.ofNullable(heartbeat);
  }

  public Optional<Boolean> getLive() {
    return Optional.ofNullable(live);
  }
}
