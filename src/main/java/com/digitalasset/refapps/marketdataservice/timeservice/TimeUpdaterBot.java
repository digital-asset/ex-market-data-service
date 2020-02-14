/*
 * Copyright (c) 2019, Digital Asset (Switzerland) GmbH and/or its affiliates. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package com.digitalasset.refapps.marketdataservice.timeservice;

import com.digitalasset.refapps.marketdataservice.timeservice.LedgerApiHandle.Contract;
import com.google.common.collect.Iterables;
import da.timeservice.timeservice.CurrentTime;
import da.timeservice.timeservice.TimeManager;
import io.reactivex.Flowable;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import jsonapi.ContractQuery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TimeUpdaterBot {

  private static final Logger logger = LoggerFactory.getLogger(TimeUpdaterBot.class);

  private final LedgerApiHandle handle;
  private final ContractQuery timeManagerFilter;
  private final ContractQuery currentTimeFilter;
  private Flowable<List<Contract>> timeManagerEvents;
  private Flowable<List<Contract>> currentTimeEvents;

  public TimeUpdaterBot(LedgerApiHandle handle) {
    this.handle = handle;
    this.timeManagerFilter = new ContractQuery(Collections.singleton(TimeManager.TEMPLATE_ID));
    this.currentTimeFilter = new ContractQuery(Collections.singleton(CurrentTime.TEMPLATE_ID));
  }

  void updateModelTime() {
    try {
      logger.debug("Updating current time...");
      TimeManager.ContractId manager = getTimeManager();
      logger.debug("Old time: {}", getModelCurrentTime());
      handle.submitCommand(manager.exerciseAdvanceCurrentTime());
      logger.info("New time: {}", getModelCurrentTime());
      logger.info("Updated current time");
    } catch (Throwable e) {
      logger.error("Exception observed during update: ", e);
      throw e;
    }
  }

  private Instant getModelCurrentTime() {
    // TODO: Do we really need to handle this and in this way?
    if (currentTimeEvents == null) {
      currentTimeEvents = handle.getCreatedEvents(currentTimeFilter).filter(xs -> !xs.isEmpty());
    }
    List<LedgerApiHandle.Contract> currentTimes = currentTimeEvents.blockingFirst();
    LedgerApiHandle.Contract currentTime = Iterables.getOnlyElement(currentTimes);
    return CurrentTime.fromValue(currentTime.getArguments()).currentTime;
  }

  private TimeManager.ContractId getTimeManager() {
    // TODO: Do we really need to handle this and in this way?
    if (timeManagerEvents == null) {
      timeManagerEvents = handle.getCreatedEvents(timeManagerFilter).filter(xs -> !xs.isEmpty());
    }
    List<LedgerApiHandle.Contract> timeManagers = timeManagerEvents.blockingFirst();
    LedgerApiHandle.Contract timeManager = Iterables.getOnlyElement(timeManagers);
    return new TimeManager.ContractId(timeManager.getContractId());
  }
}
