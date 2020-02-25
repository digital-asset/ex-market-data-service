/*
 * Copyright (c) 2019, Digital Asset (Switzerland) GmbH and/or its affiliates. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package com.digitalasset.refapps.marketdataservice.timeservice;

import com.daml.ledger.javaapi.data.Identifier;
import da.timeservice.timeservice.CurrentTime;
import da.timeservice.timeservice.TimeManager;
import java.time.Instant;
import java.util.Collections;
import jsonapi.ActiveContractSet;
import jsonapi.ContractQuery;
import jsonapi.LedgerClient;
import jsonapi.events.CreatedEvent;
import org.mockito.Mock;

public abstract class TimeUpdaterBotBaseTest {

  @Mock protected LedgerClient ledgerClient;

  String operator = "Dummy Operator";

  CreatedEvent createTimeManager() {
    TimeManager timeManager = new TimeManager(operator);
    return new CreatedEvent(TimeManager.TEMPLATE_ID, "123", timeManager);
  }

  CreatedEvent createCurrentTime() {
    CurrentTime currentTime = new CurrentTime(operator, Instant.MIN, Collections.emptyList());
    return new CreatedEvent(CurrentTime.TEMPLATE_ID, "123", currentTime);
  }

  ActiveContractSet createContractResponse(CreatedEvent event) {
    return event.update(ActiveContractSet.empty());
  }

  protected ContractQuery queryFor(Identifier templateId) {
    return new ContractQuery(Collections.singletonList(templateId));
  }
}
