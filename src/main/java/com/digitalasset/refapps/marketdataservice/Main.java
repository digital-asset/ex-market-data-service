/*
 * Copyright (c) 2019, Digital Asset (Switzerland) GmbH and/or its affiliates. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package com.digitalasset.refapps.marketdataservice;

import com.daml.ledger.javaapi.data.Template;
import com.daml.ledger.javaapi.data.TransactionFilter;
import com.daml.ledger.rxjava.DamlLedgerClient;
import com.daml.ledger.rxjava.components.Bot;
import com.daml.ledger.rxjava.components.LedgerViewFlowable.LedgerTestView;
import com.daml.ledger.rxjava.components.LedgerViewFlowable.LedgerView;
import com.daml.ledger.rxjava.components.helpers.CommandsAndPendingSet;
import com.digitalasset.refapps.marketdataservice.publishing.CachingCsvDataProvider;
import com.digitalasset.refapps.marketdataservice.publishing.DataProviderBot;
import com.digitalasset.refapps.marketdataservice.publishing.PublishingDataProvider;
import com.digitalasset.refapps.marketdataservice.timeservice.TimeUpdaterBot;
import com.digitalasset.refapps.marketdataservice.timeservice.TimeUpdaterBotExecutor;
import com.digitalasset.refapps.marketdataservice.utils.AppParties;
import com.digitalasset.refapps.marketdataservice.utils.CliOptions;
import com.digitalasset.refapps.marketdataservice.utils.CommandsAndPendingSetBuilder;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import java.time.Clock;
import java.time.Duration;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;
import java.util.function.Function;
import jsonapi.ActiveContract;
import jsonapi.JsonLedgerClient;
import org.pcollections.HashTreePMap;
import org.reactivestreams.Publisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Main {

  // application id used for sending commands
  private static final String APPLICATION_ID = "MarketDataService";
  private static final Logger logger = LoggerFactory.getLogger(Main.class);
  private static final Duration SYSTEM_PERIOD_TIME = Duration.ofSeconds(5);
  private static ScheduledExecutorService scheduler;
  private static TimeUpdaterBotExecutor timeUpdaterBotExecutor;

  public static void main(String[] args) throws InterruptedException {

    CliOptions cliOptions = CliOptions.parseArgs(args);

    ManagedChannel channel =
        ManagedChannelBuilder.forAddress(cliOptions.getSandboxHost(), cliOptions.getSandboxPort())
            .usePlaintext()
            .maxInboundMessageSize(Integer.MAX_VALUE)
            .build();

    DamlLedgerClient client =
        DamlLedgerClient.newBuilder(cliOptions.getSandboxHost(), cliOptions.getSandboxPort())
            .build();

    waitForSandbox(cliOptions.getSandboxHost(), cliOptions.getSandboxPort(), client);

    logPackages(client);
    AppParties appParties = new AppParties(cliOptions.getParties());
    runBots(appParties, SYSTEM_PERIOD_TIME).accept(client, channel);

    logger.info("Welcome to Market Data Service!");
    logger.info("Press Ctrl+C to shut down the program.");
    Thread.currentThread().join();
  }

  public static BiConsumer<DamlLedgerClient, ManagedChannel> runBots(
      AppParties parties, Duration systemPeriodTime) {
    return (DamlLedgerClient client, ManagedChannel channel) -> {
      logPackages(client);

      Duration mrt = Duration.ofSeconds(10);
      CommandsAndPendingSetBuilder.Factory commandBuilderFactory =
          CommandsAndPendingSetBuilder.factory(APPLICATION_ID, Clock::systemUTC, mrt);

      if (parties.hasMarketDataProvider1()) {
        logger.info("Starting automation for MarketDataProvider1.");
        PublishingDataProvider dataProvider = new CachingCsvDataProvider();
        DataProviderBot dataProviderBot =
            new DataProviderBot(
                commandBuilderFactory, parties.getMarketDataProvider1(), dataProvider);
        Bot.wire(
            APPLICATION_ID,
            client,
            dataProviderBot.getTransactionFilter(),
            dataProviderBot::calculateCommands,
            dataProviderBot::getContractInfo);
      }

      if (parties.hasMarketDataProvider2()) {
        logger.info("Starting automation for MarketDataProvider2.");
        PublishingDataProvider dataProvider = new CachingCsvDataProvider();
        DataProviderBot dataProviderBot =
            new DataProviderBot(
                commandBuilderFactory, parties.getMarketDataProvider2(), dataProvider);
        Bot.wire(
            APPLICATION_ID,
            client,
            dataProviderBot.getTransactionFilter(),
            dataProviderBot::calculateCommands,
            dataProviderBot::getContractInfo);
      }

      if (parties.hasOperator()) {
        logger.info("Starting automation for Operator.");
        TimeUpdaterBot timeUpdaterBot =
            new TimeUpdaterBot(client, commandBuilderFactory, parties.getOperator());
        scheduler = Executors.newScheduledThreadPool(1);
        timeUpdaterBotExecutor = new TimeUpdaterBotExecutor(scheduler);
        timeUpdaterBotExecutor.start(timeUpdaterBot, systemPeriodTime);
      }
    };
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

  private static void logPackages(DamlLedgerClient client) {
    StringBuilder sb = new StringBuilder("Listing packages:");
    client.getPackageClient().listPackages().forEach(id -> sb.append("\n").append(id));
    logger.debug(sb.toString());
  }

  public static void waitForSandbox(String host, int port, DamlLedgerClient client) {
    boolean connected = false;
    while (!connected) {
      try {
        client.connect();
        connected = true;
      } catch (Exception _ignored) {
        logger.info(String.format("Connecting to sandbox at %s:%s", host, port));
        try {
          Thread.sleep(1000);
        } catch (InterruptedException ignored) {
        }
      }
    }
  }

  public static BiConsumer<JsonLedgerClient, ManagedChannel> runBotsWithJsonApi(
      AppParties parties, Duration systemPeriodTime) {
    return (JsonLedgerClient client, ManagedChannel channel) -> {
      Duration mrt = Duration.ofSeconds(10);
      CommandsAndPendingSetBuilder.Factory commandBuilderFactory =
          CommandsAndPendingSetBuilder.factory(APPLICATION_ID, Clock::systemUTC, mrt);

      if (parties.hasMarketDataProvider1()) {
        logger.info("Starting automation for MarketDataProvider1.");
        PublishingDataProvider dataProvider = new CachingCsvDataProvider();
        DataProviderBot dataProviderBot =
            new DataProviderBot(
                commandBuilderFactory, parties.getMarketDataProvider1(), dataProvider);
        wire(client, dataProviderBot.getTransactionFilter(), dataProviderBot::calculateCommands);
      }
    };
  }

  public static void wire(
      JsonLedgerClient ledgerClient,
      TransactionFilter transactionFilter,
      Function<LedgerView<Template>, Publisher<CommandsAndPendingSet>> bot) {

    ledgerClient
        .getActiveContracts(transactionFilter)
        .map(Main::toLedgerView)
        .flatMap(bot::apply)
        .forEach(
            cps -> {
              // TODO: Send commands
              // TODO: Handle pending
            });
  }

  static LedgerView<Template> toLedgerView(Set<ActiveContract> events) {
    LedgerTestView<Template> emptyLedgerView = createEmptyLedgerView();
    for (ActiveContract event : events) {
      emptyLedgerView = addActiveContract(emptyLedgerView, event);
    }
    return emptyLedgerView;
  }

  private static LedgerTestView<Template> addActiveContract(
      LedgerTestView<Template> emptyLedgerView, ActiveContract event) {
    return emptyLedgerView.addActiveContract(
        event.getIdentifier(), event.getContractId(), event.getTemplate());
  }

  private static LedgerTestView<Template> createEmptyLedgerView() {
    return new LedgerTestView<>(
        HashTreePMap.empty(), HashTreePMap.empty(), HashTreePMap.empty(), HashTreePMap.empty());
  }
}
