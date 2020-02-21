/*
 * Copyright (c) 2019, Digital Asset (Switzerland) GmbH and/or its affiliates. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package jsonapi.http;

import static org.junit.Assert.assertTrue;

import java.util.Collection;
import java.util.Collections;
import jsonapi.events.Event;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class WebSocketResponseTest {

  @Rule public ExpectedException exceptionRule = ExpectedException.none();

  @Test
  public void toEventsThrowsWithoutEventsAndError() {
    exceptionRule.expect(IllegalStateException.class);
    exceptionRule.expectMessage("no error nor events");
    new WebSocketResponse(null, null, null).toEvents();
  }

  @Test
  public void toEventsReturnsWhenNoError() {
    Collection<Event> actual =
        new WebSocketResponse(Collections.emptyList(), null, null).toEvents();
    assertTrue(actual.isEmpty());
  }

  @Test
  public void toEventsThrowsOnError() {
    exceptionRule.expect(RuntimeException.class);
    exceptionRule.expectMessage("some error message");
    new WebSocketResponse(null, "some error message", null).toEvents();
  }
}
