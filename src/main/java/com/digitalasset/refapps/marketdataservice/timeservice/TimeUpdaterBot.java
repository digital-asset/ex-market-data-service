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
import java.util.concurrent.TimeUnit;
import jsonapi.ContractQuery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TimeUpdaterBot {

  private static final Logger logger = LoggerFactory.getLogger(TimeUpdaterBot.class);

  private final LedgerApiHandle handle;
  private final ContractQuery timeManagerFilter;
  private final ContractQuery currentTimeFilter;

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
    Flowable<List<Contract>> currentTimeEvents = getContracts(currentTimeFilter);
    List<LedgerApiHandle.Contract> currentTimes = currentTimeEvents.blockingFirst();
    LedgerApiHandle.Contract currentTime = Iterables.getOnlyElement(currentTimes);
    return CurrentTime.fromValue(currentTime.getArguments()).currentTime;
  }

  private TimeManager.ContractId getTimeManager() {
    Flowable<List<Contract>> timeManagerEvents = getContracts(timeManagerFilter);
    List<LedgerApiHandle.Contract> timeManagers = timeManagerEvents.blockingFirst();
    LedgerApiHandle.Contract timeManager = Iterables.getOnlyElement(timeManagers);
    return new TimeManager.ContractId(timeManager.getContractId());
  }

  // TODO: Rework polling from HTTP and TimeUpdaterBot scheduling.
  private Flowable<List<Contract>> getContracts(ContractQuery query) {
    return Flowable.interval(100, TimeUnit.MILLISECONDS)
        .map(x -> handle.getContracts(query))
        .filter(xs -> !xs.isEmpty());
  }
}
