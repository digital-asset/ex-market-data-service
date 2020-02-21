/*
 * Copyright (c) 2019, Digital Asset (Switzerland) GmbH and/or its affiliates. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package jsonapi.http;

import static org.junit.Assert.assertTrue;

import java.util.Collections;
import jsonapi.ActiveContractSet;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class WebSocketResponseTest {

  @Rule public ExpectedException exceptionRule = ExpectedException.none();

  @Test
  public void toActiveContractSetThrowsWithoutEventsAndError() {
    exceptionRule.expect(IllegalStateException.class);
    exceptionRule.expectMessage("no error nor events");
    new WebSocketResponse(null, null).toActiveContractSet();
  }

  @Test
  public void toActiveContractSetReturnsWhenNoError() {
    ActiveContractSet actual =
        new WebSocketResponse(Collections.emptyList(), null).toActiveContractSet();
    assertTrue(actual.isEmpty());
  }

  @Test
  public void toActiveContractSetThrowsOnError() {
    exceptionRule.expect(RuntimeException.class);
    exceptionRule.expectMessage("some error message");
    new WebSocketResponse(null, "some error message").toActiveContractSet();
  }
}
