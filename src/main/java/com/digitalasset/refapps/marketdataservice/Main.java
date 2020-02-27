/*
 * Copyright (c) 2019, Digital Asset (Switzerland) GmbH and/or its affiliates. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package com.digitalasset.refapps.marketdataservice;

import com.daml.ledger.javaapi.data.Command;
import com.digitalasset.jsonapi.ActiveContractSet;
import com.digitalasset.jsonapi.ContractQuery;
import com.digitalasset.jsonapi.LedgerClient;
import com.digitalasset.jsonapi.Utils;
import com.digitalasset.refapps.marketdataservice.publishing.CachingCsvDataProvider;
import com.digitalasset.refapps.marketdataservice.publishing.DataProviderBot;
import com.digitalasset.refapps.marketdataservice.publishing.PublishingDataProvider;
import com.digitalasset.refapps.marketdataservice.timeservice.TimeUpdaterBot;
import com.digitalasset.refapps.marketdataservice.timeservice.TimeUpdaterBotExecutor;
import com.digitalasset.refapps.marketdataservice.utils.AppParties;
import com.digitalasset.refapps.marketdataservice.utils.CliOptions;
import io.reactivex.Flowable;
import java.net.URI;
import java.time.Duration;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import org.kohsuke.args4j.CmdLineException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Main {

  // application id used for sending commands
  private static final String APPLICATION_ID = "MarketDataService";
  private static final Logger logger = LoggerFactory.getLogger(Main.class);
  private static final Duration SYSTEM_PERIOD_TIME = Duration.ofSeconds(5);
  private static ScheduledExecutorService scheduler;

  public static void main(String[] args) throws InterruptedException {
    CliOptions cliOptions = null;
    try {
      cliOptions = CliOptions.parseArgs(args);
    } catch (CmdLineException e) {
      System.exit(1);
    }

    AppConfig appConfig =
        AppConfig.builder()
            .useCliOptions(cliOptions)
            .setApplicationId(APPLICATION_ID)
            .setSystemPeriodTime(SYSTEM_PERIOD_TIME)
            .create();

    waitForJsonApi(appConfig.getJsonApiUrl());

    runBots(appConfig);

    logger.info("Welcome to Market Data Service!");
    logger.info("Press Ctrl+C to shut down the program.");
    Thread.currentThread().join();
  }

  public static void runBots(AppConfig appConfig) {
    AppParties parties = appConfig.getAppParties();
    if (parties.hasMarketDataProvider1()) {
      logger.info("Starting automation for MarketDataProvider1.");
      PublishingDataProvider dataProvider = new CachingCsvDataProvider();
      DataProviderBot dataProviderBot =
          new DataProviderBot(parties.getMarketDataProvider1(), dataProvider);
      wire(
          appConfig,
          dataProviderBot.getPartyName(),
          dataProviderBot.getContractQuery(),
          dataProviderBot::getCommands);
    }

    if (parties.hasMarketDataProvider2()) {
      logger.info("Starting automation for MarketDataProvider2.");
      PublishingDataProvider dataProvider = new CachingCsvDataProvider();
      DataProviderBot dataProviderBot =
          new DataProviderBot(parties.getMarketDataProvider2(), dataProvider);
      wire(
          appConfig,
          dataProviderBot.getPartyName(),
          dataProviderBot.getContractQuery(),
          dataProviderBot::getCommands);
    }

    if (parties.hasOperator()) {
      logger.info("Starting automation for Operator.");
      LedgerClient ledgerClient = appConfig.getClientFor(parties.getOperator());
      TimeUpdaterBot timeUpdaterBot = new TimeUpdaterBot(ledgerClient);
      scheduler = Executors.newScheduledThreadPool(1);
      TimeUpdaterBotExecutor timeUpdaterBotExecutor = new TimeUpdaterBotExecutor(scheduler);
      timeUpdaterBotExecutor.start(timeUpdaterBot, appConfig.getSystemPeriodTime());
    }
  }

  private static void wire(
      AppConfig appConfig,
      String party,
      ContractQuery contractQuery,
      // TODO: Don't we need a Flowable<Collection<Command>>?
      Function<ActiveContractSet, Flowable<Command>> bot) {
    LedgerClient ledgerClient = appConfig.getClientFor(party);
    //noinspection ResultOfMethodCallIgnored
    ledgerClient
        .getActiveContracts(contractQuery)
        .flatMap(bot::apply)
        .forEach(command -> submitCommand(ledgerClient, command));
  }

  public static void terminateTimeUpdaterBot() {
    // From https://docs.oracle.com/javase/8/docs/api/java/util/concurrent/ExecutorService.html
    scheduler.shutdown();
    try {
      if (!scheduler.awaitTermination(1, TimeUnit.SECONDS)) {
        scheduler.shutdownNow();
        if (!scheduler.awaitTermination(1, TimeUnit.SECONDS))
          logger.error("Pool did not terminate");
      }
    } catch (InterruptedException e) {
      logger.error("Stopping", e);
      scheduler.shutdownNow();
      Thread.currentThread().interrupt();
    }
  }

  private static void submitCommand(LedgerClient ledgerClient, Command command) {
    command.asExerciseCommand().ifPresent(ledgerClient::exerciseChoice);
    command.asCreateCommand().ifPresent(ledgerClient::create);
  }

  private static void waitForJsonApi(URI uri) {
    try {
      Utils.waitForJsonApi(uri);
    } catch (Exception e) {
      System.exit(1);
    }
  }
}
