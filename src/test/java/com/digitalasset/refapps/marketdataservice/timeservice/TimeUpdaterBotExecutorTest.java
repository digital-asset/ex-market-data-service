/*
 * Copyright (c) 2019, Digital Asset (Switzerland) GmbH and/or its affiliates. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package com.digitalasset.refapps.marketdataservice.timeservice;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.daml.ledger.javaapi.data.ExerciseCommand;
import com.digitalasset.jsonapi.events.CreatedEvent;
import com.digitalasset.refapps.utils.ManualExecutorService;
import da.timeservice.timeservice.CurrentTime;
import da.timeservice.timeservice.TimeManager;
import java.time.Duration;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class TimeUpdaterBotExecutorTest extends TimeUpdaterBotBaseTest {

  @Test(expected = IllegalArgumentException.class)
  public void cannotStartWithTooShortSystemPeriodTime() {
    TimeUpdaterBotExecutor botExecutor = new TimeUpdaterBotExecutor(null);
    botExecutor.start(null, Duration.ofMillis(100 - 1));
  }

  @Test
  public void startSchedulesBotAtFixedRate() {
    ScheduledExecutorService executorService = mock(ScheduledExecutorService.class);
    TimeUpdaterBot dummyBot = new TimeUpdaterBot(null);

    TimeUpdaterBotExecutor botExecutor = new TimeUpdaterBotExecutor(executorService);
    botExecutor.start(dummyBot, Duration.ofMillis(142));

    verify(executorService)
        .scheduleAtFixedRate(
            any(Runnable.class), eq(142_000_000L), eq(142_000_000L), eq(TimeUnit.NANOSECONDS));
  }

  @Test
  public void scheduledBotUpdatesCurrentTime() {
    CreatedEvent eventManager = createTimeManager("123");
    CreatedEvent eventCurrentTime = createCurrentTime();
    when(ledgerClient.queryContracts(queryFor(TimeManager.TEMPLATE_ID)))
        .thenReturn(createContractResponse(eventManager));
    when(ledgerClient.queryContracts(queryFor(CurrentTime.TEMPLATE_ID)))
        .thenReturn(createContractResponse(eventCurrentTime));
    ManualExecutorService scheduler = new ManualExecutorService();

    TimeUpdaterBot bot = new TimeUpdaterBot(ledgerClient);
    TimeUpdaterBotExecutor botExecutor = new TimeUpdaterBotExecutor(scheduler);
    botExecutor.start(bot, Duration.ofMillis(101));
    scheduler.runScheduledNow();
    scheduler.runScheduledNow();

    TimeManager.ContractId timeManagerCid = new TimeManager.ContractId("123");
    ExerciseCommand expectedCommand = timeManagerCid.exerciseAdvanceCurrentTime();
    verify(ledgerClient, times(2)).exerciseChoice(expectedCommand);
  }
}
