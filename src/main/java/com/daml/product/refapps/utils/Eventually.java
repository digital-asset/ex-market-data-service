/*
 * Copyright (c) 2019, Digital Asset (Switzerland) GmbH and/or its affiliates. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package com.daml.product.refapps.utils;

import java.time.Duration;
import java.time.Instant;
import java.util.function.BooleanSupplier;
import java.util.function.Supplier;

public class Eventually {

  private final Duration timeout;
  private final Duration interval;
  private final Supplier<Instant> now;

  Eventually(Duration timeout, Duration interval, Supplier<Instant> now) {
    this.timeout = timeout;
    this.interval = interval;
    this.now = now;
  }

  public void execute(Runnable code) throws InterruptedException {
    execute(
        () -> {
          code.run();
          return true;
        });
  }

  public void execute(BooleanSupplier code) throws InterruptedException {
    Instant started = now.get();
    boolean finished = false;
    while (!finished && !hasTimedOutSince(started)) {
      try {
        finished = code.getAsBoolean();
      } catch (Throwable ignored) {
      }
      if (!finished) {
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

  public static class TimeoutExceeded extends RuntimeException {}
}
