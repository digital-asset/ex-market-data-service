/*
 * Copyright (c) 2019, Digital Asset (Switzerland) GmbH and/or its affiliates. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package com.digitalasset.refapps;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import io.reactivex.Flowable;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.Test;

public class FlowableTest {

  @Test
  public void multipleSubscribers() {
    Flowable<Integer> source = Flowable.range(0, 20);

    AtomicInteger counterA = new AtomicInteger(0);
    source.forEach(x -> counterA.incrementAndGet());

    AtomicInteger counterB = new AtomicInteger(0);
    source.forEach(x -> counterB.incrementAndGet());

    assertThat(counterA.get(), is(20));
    assertThat(counterA.get(), is(counterB.get()));
  }
}
