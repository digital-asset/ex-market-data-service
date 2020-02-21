/*
 * Copyright (c) 2019, Digital Asset (Switzerland) GmbH and/or its affiliates. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package com.digitalasset.refapps.marketdataservice.timeservice;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Duration;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import jsonapi.events.CreatedEvent;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class TimeUpdaterBotIT extends TimeUpdaterBotBaseTest {

  private ScheduledExecutorService scheduler;

  @Before
  public void setup() {
    scheduler = Executors.newScheduledThreadPool(1);
  }

  @After
  public void tearDown() {
    scheduler.shutdown();
  }

  @Test
  public void componentTest() throws InterruptedException {
    CreatedEvent eventManager = createTimeManager();
    CreatedEvent eventCurrentTime = createCurrentTime();
    when(ledgerClient.queryContracts(any()))
        .thenReturn(createContractResponse(eventManager))
        .thenReturn(createContractResponse(eventCurrentTime))
        .thenReturn(createContractResponse(eventCurrentTime));

    Duration systemPeriodTime = Duration.ofMillis(1000);

    TimeUpdaterBot bot = newBotBuilder().build();
    TimeUpdaterBotExecutor timeUpdaterBotExecutor = new TimeUpdaterBotExecutor(scheduler);
    timeUpdaterBotExecutor.start(bot, systemPeriodTime);

    TimeUnit.MILLISECONDS.sleep(2500);

    verify(ledgerClient, times(2)).exerciseChoice(any());
  }
}
