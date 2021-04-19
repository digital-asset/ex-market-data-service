/*
 * Copyright (c) 2019, Digital Asset (Switzerland) GmbH and/or its affiliates. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */

package com.daml.product.refapps.utils;

import java.time.Duration;
import java.time.Instant;
import java.util.function.Supplier;

public class EventuallyBuilder {

  private final Supplier<Instant> now = Instant::now;
  private Duration timeout = Duration.ofSeconds(30);
  private Duration interval = Duration.ofMillis(200);

  public EventuallyBuilder setTimeout(Duration timeout) {
    this.timeout = timeout;
    return this;
  }

  public EventuallyBuilder setInterval(Duration interval) {
    this.interval = interval;
    return this;
  }

  public Eventually create() {
    return new Eventually(timeout, interval, now);
  }
}
