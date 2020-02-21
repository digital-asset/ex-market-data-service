/*
 * Copyright (c) 2019, Digital Asset (Switzerland) GmbH and/or its affiliates. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package com.digitalasset.refapps.marketdataservice.timeservice;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.daml.ledger.javaapi.data.ExerciseCommand;
import com.digitalasset.refapps.utils.ManualExecutorService;
import da.timeservice.timeservice.TimeManager;
import java.util.concurrent.TimeUnit;
import jsonapi.events.CreatedEvent;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class TimeUpdaterBotTest extends TimeUpdaterBotBaseTest {

  public TimeUpdaterBotTest() {
    this.operator = "John Doe";
  }

  @Test
  public void newCurrentTimeIsUpdated() {
    CreatedEvent eventManager = createTimeManager();
    CreatedEvent eventCurrentTime = createCurrentTime();
    when(ledgerClient.queryContracts(any()))
        .thenReturn(createContractResponse(eventManager))
        .thenReturn(createContractResponse(eventCurrentTime))
        .thenReturn(createContractResponse(eventCurrentTime));
    ManualExecutorService executor = new ManualExecutorService();

    TimeUpdaterBot bot = new TimeUpdaterBot(ledgerClient);
    executor.scheduleAtFixedRate(bot::updateModelTime, 1, 1, TimeUnit.SECONDS);
    executor.runScheduledNow();

    TimeManager.ContractId currentTimeCid =
        new TimeManager.ContractId(eventManager.getContractId());
    ExerciseCommand expectedCommand = currentTimeCid.exerciseAdvanceCurrentTime();
    verify(ledgerClient, times(1)).exerciseChoice(expectedCommand);
  }
}
