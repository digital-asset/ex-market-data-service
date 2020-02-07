/*
 * Copyright (c) 2019, Digital Asset (Switzerland) GmbH and/or its affiliates. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package jsonapi.http;

import java.util.Collection;

@SuppressWarnings("PMD.DataClass")
public class WebSocketResponse {

  private final Collection<Object> events;

  public WebSocketResponse(Collection<Object> events) {
    this.events = events;
  }

  public Collection<Object> getEvents() {
    return events;
  }
}
