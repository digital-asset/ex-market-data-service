/*
 * Copyright (c) 2019, Digital Asset (Switzerland) GmbH and/or its affiliates. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package com.digitalasset.refapps.utils;

import java.time.Duration;
import java.time.Instant;
import java.util.function.Supplier;

public class Eventually {

  public static final Eventually DEFAULT_EVENTUALLY =
      new Eventually(Duration.ofSeconds(30), Duration.ofMillis(200), Instant::now);
  private final Duration timeout;
  private final Duration interval;
  private final Supplier<Instant> now;

  Eventually(Duration timeout, Duration interval, Supplier<Instant> now) {
    this.timeout = timeout;
    this.interval = interval;
    this.now = now;
  }

  public void execute(Runnable code) throws InterruptedException {
    Instant started = now.get();
    boolean finished = false;
    while (!finished && !hasTimedOutSince(started)) {
      try {
        code.run();
        finished = true;
      } catch (Throwable ignored) {
        Thread.sleep(interval.toMillis());
      }
    }
    if (!finished) {
      throw new TimeoutExceeded();
    }
  }

  private boolean hasTimedOutSince(Instant start) {
    Duration elapsed = Duration.between(start, now.get());
    return elapsed.compareTo(timeout) > 0;
  }

  public static void eventually(Runnable code) throws InterruptedException {
    DEFAULT_EVENTUALLY.execute(code);
  }

  public static class TimeoutExceeded extends RuntimeException {}
}
