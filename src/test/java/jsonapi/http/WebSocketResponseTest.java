/*
 * Copyright (c) 2019, Digital Asset (Switzerland) GmbH and/or its affiliates. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package jsonapi.http;

import static org.hamcrest.CoreMatchers.everyItem;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import jsonapi.events.CreatedEvent;
import jsonapi.events.Event;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class WebSocketResponseTest {
  @Test
  public void toEvents() {
    Collection<EventHolder> events = Collections.singletonList(new CreatedEventHolder(new CreatedEvent(null, null, null)));
    Collection<Event> actual =
        new WebSocketResponse(events, null, null).getEvents();
    assertEquals(1, actual.size());
    assertThat(actual, everyItem(instanceOf(CreatedEvent.class)));
  }
}
