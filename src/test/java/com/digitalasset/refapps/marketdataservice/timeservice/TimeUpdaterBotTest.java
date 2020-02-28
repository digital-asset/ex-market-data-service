/*
 * Copyright (c) 2019, Digital Asset (Switzerland) GmbH and/or its affiliates. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package com.digitalasset.refapps.marketdataservice.timeservice;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.daml.ledger.javaapi.data.ExerciseCommand;
import com.digitalasset.jsonapi.events.CreatedEvent;
import da.timeservice.timeservice.CurrentTime;
import da.timeservice.timeservice.TimeManager;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class TimeUpdaterBotTest extends TimeUpdaterBotBaseTest {

  @Test
  public void updateModelTimeAdvancesCurrentTime() {
    CreatedEvent eventManager = createTimeManager();
    CreatedEvent eventCurrentTime = createCurrentTime();
    when(ledgerClient.queryContracts(queryFor(TimeManager.TEMPLATE_ID)))
        .thenReturn(createContractResponse(eventManager));
    when(ledgerClient.queryContracts(queryFor(CurrentTime.TEMPLATE_ID)))
        .thenReturn(createContractResponse(eventCurrentTime));

    TimeUpdaterBot bot = new TimeUpdaterBot(ledgerClient);
    bot.updateModelTime();

    TimeManager.ContractId timeManagerCid =
        new TimeManager.ContractId(eventManager.getContractId());
    ExerciseCommand expectedCommand = timeManagerCid.exerciseAdvanceCurrentTime();
    verify(ledgerClient).exerciseChoice(expectedCommand);
  }
}
