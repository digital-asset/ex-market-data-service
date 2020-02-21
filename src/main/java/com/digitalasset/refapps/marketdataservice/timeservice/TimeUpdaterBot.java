/*
 * Copyright (c) 2019, Digital Asset (Switzerland) GmbH and/or its affiliates. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package com.digitalasset.refapps.marketdataservice.timeservice;

import com.google.common.collect.Iterables;
import da.timeservice.timeservice.CurrentTime;
import da.timeservice.timeservice.TimeManager;
import da.timeservice.timeservice.TimeManager.ContractId;
import io.reactivex.Flowable;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import jsonapi.ActiveContractSet;
import jsonapi.Contract;
import jsonapi.ContractQuery;
import jsonapi.LedgerClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TimeUpdaterBot {

  private static final Logger logger = LoggerFactory.getLogger(TimeUpdaterBot.class);

  private final LedgerClient ledger;
  private final ContractQuery timeManagerFilter;
  private final ContractQuery currentTimeFilter;

  public TimeUpdaterBot(LedgerClient ledger) {
    this.ledger = ledger;
    this.timeManagerFilter = new ContractQuery(Collections.singleton(TimeManager.TEMPLATE_ID));
    this.currentTimeFilter = new ContractQuery(Collections.singleton(CurrentTime.TEMPLATE_ID));
  }

  void updateModelTime() {
    try {
      logger.debug("Updating current time...");
      TimeManager.ContractId manager = getTimeManager();
      logger.debug("Old time: {}", getModelCurrentTime());
      ledger.exerciseChoice(manager.exerciseAdvanceCurrentTime());
      logger.info("New time: {}", getModelCurrentTime());
      logger.info("Updated current time");
    } catch (Throwable e) {
      logger.error("Exception observed during update: ", e);
      throw e;
    }
  }

  private Instant getModelCurrentTime() {
    Flowable<ActiveContractSet> activeContractSet = getContracts(currentTimeFilter);
    List<Contract<CurrentTime>> currentTimes =
        activeContractSet
            .blockingFirst()
            .getActiveContracts(CurrentTime.TEMPLATE_ID, CurrentTime.class)
            .collect(Collectors.toList());
    Contract<CurrentTime> currentTime = Iterables.getOnlyElement(currentTimes);
    return currentTime.getContract().currentTime;
  }

  private TimeManager.ContractId getTimeManager() {
    Flowable<ActiveContractSet> activeContractSet = getContracts(timeManagerFilter);
    List<Contract<TimeManager>> timeManagers =
        activeContractSet
            .blockingFirst()
            .getActiveContracts(TimeManager.TEMPLATE_ID, TimeManager.class)
            .collect(Collectors.toList());
    Contract<TimeManager> timeManager = Iterables.getOnlyElement(timeManagers);
    return new ContractId(timeManager.getContractId());
  }

  // TODO: Rework polling from HTTP and TimeUpdaterBot scheduling.
  private Flowable<ActiveContractSet> getContracts(ContractQuery query) {
    return Flowable.interval(100, TimeUnit.MILLISECONDS)
        .map(x -> ledger.queryContracts(query))
        .filter(xs -> !xs.isEmpty());
  }
}
