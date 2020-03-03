/*
 * Copyright (c) 2019, Digital Asset (Switzerland) GmbH and/or its affiliates. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package com.digitalasset.refapps.utils;

import static org.junit.Assert.fail;

import com.digitalasset.refapps.utils.Eventually.TimeoutExceeded;
import java.time.Duration;

public class EventuallyUtil {
  private static final Duration TOO_MUCH_TIME = Duration.ofMinutes(3L);
  private static final Eventually EVENTUALLY =
      new EventuallyBuilder().setTimeout(TOO_MUCH_TIME).create();

  public static void eventually(Runnable code) throws InterruptedException {
    try {
      EVENTUALLY.execute(code);
    } catch (TimeoutExceeded ignore) {
      fail("Code did not succeed within timeout.");
    }
  }
}
