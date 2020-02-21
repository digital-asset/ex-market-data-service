/*
 * Copyright (c) 2019, Digital Asset (Switzerland) GmbH and/or its affiliates. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package com.digitalasset.refapps.marketdataservice;

import com.daml.ledger.javaapi.data.Command;
import com.digitalasset.refapps.marketdataservice.publishing.CachingCsvDataProvider;
import com.digitalasset.refapps.marketdataservice.publishing.DataProviderBot;
import com.digitalasset.refapps.marketdataservice.publishing.PublishingDataProvider;
import com.digitalasset.refapps.marketdataservice.timeservice.TimeUpdaterBot;
import com.digitalasset.refapps.marketdataservice.timeservice.TimeUpdaterBotExecutor;
import com.digitalasset.refapps.marketdataservice.utils.AppParties;
import com.digitalasset.refapps.marketdataservice.utils.CliOptions;
import io.reactivex.Flowable;
import java.time.Duration;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import jsonapi.ActiveContractSet;
import jsonapi.ContractQuery;
import jsonapi.JsonLedgerClient;
import jsonapi.LedgerClient;
import jsonapi.Utils;
import jsonapi.gson.GsonDeserializer;
import jsonapi.gson.GsonSerializer;
import jsonapi.http.HttpResponse;
import jsonapi.http.WebSocketResponse;
import jsonapi.json.JsonDeserializer;
import org.kohsuke.args4j.CmdLineException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Main {

  // application id used for sending commands
  private static final String APPLICATION_ID = "MarketDataService";
  private static final Logger logger = LoggerFactory.getLogger(Main.class);
  private static final Duration SYSTEM_PERIOD_TIME = Duration.ofSeconds(5);
  private static final GsonSerializer jsonSerializer = new GsonSerializer();
  private static final GsonDeserializer jsonDeserializer = new GsonDeserializer();
  private static final JsonDeserializer<HttpResponse> httpResponseDeserializer =
      jsonDeserializer.getHttpResponseDeserializer();
  private static final JsonDeserializer<WebSocketResponse> webSocketResponseDeserializer =
      jsonDeserializer.getWebSocketResponseDeserializer();
  private static ScheduledExecutorService scheduler;
  private static TimeUpdaterBotExecutor timeUpdaterBotExecutor;

  public static void main(String[] args) throws InterruptedException {

    CliOptions cliOptions = null;
    try {
      cliOptions = CliOptions.parseArgs(args);
    } catch (CmdLineException e) {
      System.exit(1);
    }

    waitForJsonApi(cliOptions.getSandboxHost(), cliOptions.getSandboxPort());

    AppParties appParties = new AppParties(cliOptions.getParties());
    runBots(cliOptions.getLedgerId(), appParties, SYSTEM_PERIOD_TIME);

    logger.info("Welcome to Market Data Service!");
    logger.info("Press Ctrl+C to shut down the program.");
    Thread.currentThread().join();
  }

  public static void runBots(String ledgerId, AppParties parties, Duration systemPeriodTime) {
    if (parties.hasMarketDataProvider1()) {
      logger.info("Starting automation for MarketDataProvider1.");
      PublishingDataProvider dataProvider = new CachingCsvDataProvider();
      DataProviderBot dataProviderBot =
          new DataProviderBot(parties.getMarketDataProvider1(), dataProvider);
      wire(
          ledgerId,
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
          ledgerId,
          dataProviderBot.getPartyName(),
          dataProviderBot.getContractQuery(),
          dataProviderBot::getCommands);
    }

    if (parties.hasOperator()) {
      logger.info("Starting automation for Operator.");
      LedgerClient ledgerClient = createLedgerClient(ledgerId, parties.getOperator());
      TimeUpdaterBot timeUpdaterBot = new TimeUpdaterBot(ledgerClient);
      scheduler = Executors.newScheduledThreadPool(1);
      timeUpdaterBotExecutor = new TimeUpdaterBotExecutor(scheduler);
      timeUpdaterBotExecutor.start(timeUpdaterBot, systemPeriodTime);
    }
  }

  private static void wire(
      String ledgerId,
      String party,
      ContractQuery contractQuery,
      Function<ActiveContractSet, Flowable<Command>> bot) {
    LedgerClient ledgerClient = createLedgerClient(ledgerId, party);
    ledgerClient
        .getActiveContracts(contractQuery)
        .flatMap(bot::apply)
        .forEach(command -> submitCommand(ledgerClient, command));
  }

  private static LedgerClient createLedgerClient(String ledgerId, String operator) {
    return JsonLedgerClient.create(
        ledgerId,
        operator,
        APPLICATION_ID,
        httpResponseDeserializer,
        jsonSerializer,
        webSocketResponseDeserializer);
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

  public static void waitForJsonApi(String host, int port) {
    String jsonApiUri = String.format("http://%s:%d", host, port);
    try {
      Utils.waitForJsonApi(jsonApiUri);
    } catch (Exception e) {
      System.exit(1);
    }
  }
}
