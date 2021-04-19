/*
 * Copyright (c) 2019, Digital Asset (Switzerland) GmbH and/or its affiliates. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package com.daml.extensions.jsonapi.http;

import static org.hamcrest.CoreMatchers.everyItem;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

import com.daml.extensions.jsonapi.events.CreatedEvent;
import com.daml.extensions.jsonapi.events.Event;
import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
import org.junit.Test;

public class WebSocketResponseTest {

  @Test
  public void toEvents() {
    Collection<EventHolder> events =
        Collections.singletonList(new CreatedEventHolder(new CreatedEvent(null, null, null)));
    Collection<Event> actual =
        new WebSocketResponse(events, null, null, null, null).getEvents().get();
    assertEquals(1, actual.size());
    assertThat(actual, everyItem(instanceOf(CreatedEvent.class)));
  }

  @Test
  public void toEventsDoesNotReturnAListForNonData() {
    WebSocketResponse webSocketResponse = new WebSocketResponse(null, null, null, null, null);
    assertEquals(Optional.empty(), webSocketResponse.getEvents());
  }
}
