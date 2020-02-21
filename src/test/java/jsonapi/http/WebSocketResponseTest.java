/*
 * Copyright (c) 2019, Digital Asset (Switzerland) GmbH and/or its affiliates. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package jsonapi.http;

import static org.junit.Assert.assertEquals;

import java.util.Collection;
import java.util.Collections;
import jsonapi.events.Event;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class WebSocketResponseTest {

  @Rule public ExpectedException exceptionRule = ExpectedException.none();

  @Test
  public void cannotCreateWithoutEventsAndError() {
    exceptionRule.expect(RuntimeException.class);
    exceptionRule.expectMessage("both events and error are null");
    new WebSocketResponse(null, null);
  }

  @Test
  public void getEventsReturnsWhenNoError() {
    Collection<Event> actual = new WebSocketResponse(Collections.emptyList(), null).getEvents();
    assertEquals(Collections.emptyList(), actual);
  }

  @Test
  public void getEventsThrowsOnError() {
    exceptionRule.expect(RuntimeException.class);
    exceptionRule.expectMessage("some error message");
    new WebSocketResponse(null, "some error message").getEvents();
  }
}
