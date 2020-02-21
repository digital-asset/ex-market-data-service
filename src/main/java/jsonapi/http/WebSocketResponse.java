/*
 * Copyright (c) 2019, Digital Asset (Switzerland) GmbH and/or its affiliates. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package jsonapi.http;

import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;
import jsonapi.events.Event;

@SuppressWarnings("PMD.DataClass")
public class WebSocketResponse {

  // We keep them nullable references instead of using Optional for easier Gson parsing
  private final Collection<EventHolder> events;
  private final String error;
  private final Object warnings;

  public WebSocketResponse(Collection<EventHolder> events, String error, Object warnings) {
    this.events = events;
    this.error = error;
    this.warnings = warnings;
  }

  // TODO return Optional. Should not return empty list for null events, because events==null means
  // some application error in this ref app (because the websocket error should have been detected
  // and escalated). In other words, returning empty list for events==null would silently swallow an
  // error.
  public Collection<Event> getEvents() {
    return events.stream().map(EventHolder::event).collect(Collectors.toList());
  }

  public Optional<String> getError() {
    return Optional.ofNullable(error);
  }

  public Optional<Object> getWarnings() {
    return Optional.ofNullable(warnings);
  }
}
