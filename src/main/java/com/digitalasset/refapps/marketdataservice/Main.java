/*
 * Copyright (c) 2019, Digital Asset (Switzerland) GmbH and/or its affiliates. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package com.digitalasset.refapps.marketdataservice;

import com.daml.ledger.javaapi.data.Command;
import com.daml.ledger.javaapi.data.Template;
import com.daml.ledger.javaapi.data.TransactionFilter;
import com.daml.ledger.rxjava.DamlLedgerClient;
import com.daml.ledger.rxjava.LedgerClient;
import com.daml.ledger.rxjava.components.Bot;
import com.daml.ledger.rxjava.components.LedgerViewFlowable.LedgerTestView;
import com.daml.ledger.rxjava.components.LedgerViewFlowable.LedgerView;
import com.daml.ledger.rxjava.components.helpers.CommandsAndPendingSet;
import com.daml.ledger.rxjava.components.helpers.CreatedContract;
import com.digitalasset.refapps.marketdataservice.publishing.CachingCsvDataProvider;
import com.digitalasset.refapps.marketdataservice.publishing.DataProviderBot;
import com.digitalasset.refapps.marketdataservice.publishing.PublishingDataProvider;
import com.digitalasset.refapps.marketdataservice.timeservice.GrpcLedgerApiHandle;
import com.digitalasset.refapps.marketdataservice.timeservice.JsonLedgerApiHandle;
import com.digitalasset.refapps.marketdataservice.timeservice.LedgerApiHandle;
import com.digitalasset.refapps.marketdataservice.timeservice.TimeUpdaterBot;
import com.digitalasset.refapps.marketdataservice.timeservice.TimeUpdaterBotExecutor;
import com.digitalasset.refapps.marketdataservice.utils.AppParties;
import com.digitalasset.refapps.marketdataservice.utils.CliOptions;
import com.digitalasset.refapps.marketdataservice.utils.CommandsAndPendingSetBuilder;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.reactivex.Flowable;
import java.time.Clock;
import java.time.Duration;
import java.util.Collections;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import jsonapi.ActiveContract;
import jsonapi.ActiveContractSet;
import jsonapi.ContractQuery;
import jsonapi.JsonLedgerClient;
import jsonapi.apache.ApacheHttpClient;
import jsonapi.gson.GsonDeserializer;
import jsonapi.gson.GsonSerializer;
import jsonapi.http.Api;
import jsonapi.http.HttpResponse;
import jsonapi.http.Jwt;
import jsonapi.http.WebSocketResponse;
import jsonapi.json.JsonDeserializer;
import jsonapi.tyrus.TyrusWebSocketClient;
import org.pcollections.HashTreePMap;
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
    runBots(appParties, SYSTEM_PERIOD_TIME, new GrpcWirer()).accept(client, channel);

    logger.info("Welcome to Market Data Service!");
    logger.info("Press Ctrl+C to shut down the program.");
    Thread.currentThread().join();
  }

  public interface Wirer {
    void wire(
        String ledgerId,
        String party,
        ContractQuery contractQuery,
        String applicationId,
        LedgerClient ledgerClient,
        TransactionFilter transactionFilter,
        Function<LedgerView<Template>, Flowable<CommandsAndPendingSet>> bot,
        Function<CreatedContract, Template> transform);
  }

  public static class GrpcWirer implements Wirer {
    @Override
    public void wire(
        String ledgerId,
        String party,
        ContractQuery contractQuery,
        String applicationId,
        LedgerClient ledgerClient,
        TransactionFilter transactionFilter,
        Function<LedgerView<Template>, Flowable<CommandsAndPendingSet>> bot,
        Function<CreatedContract, Template> transform) {
      Bot.wire(applicationId, ledgerClient, transactionFilter, bot, transform);
    }
  }

  public static class JsonWirer implements Wirer {

    @Override
    public void wire(
        String ledgerId,
        String party,
        ContractQuery contractQuery,
        String applicationId,
        LedgerClient ledgerClient,
        TransactionFilter transactionFilter,
        Function<LedgerView<Template>, Flowable<CommandsAndPendingSet>> bot,
        Function<CreatedContract, Template> transform) {
      Main.wire(ledgerId, party, contractQuery, bot);
    }
  }

  public static BiConsumer<DamlLedgerClient, ManagedChannel> runBots(
      AppParties parties, Duration systemPeriodTime, Wirer wirer) {
    return (DamlLedgerClient client, ManagedChannel channel) -> {
      Function<CommandsAndPendingSetBuilder.Factory, LedgerApiHandle> handlerFactory =
          commandBuilderFactory ->
              new GrpcLedgerApiHandle(client, commandBuilderFactory, parties.getOperator());
      runBotsWithGrpcApi(client, parties, systemPeriodTime, wirer, handlerFactory);
    };
  }

  public static void runBotsWithGrpcApi(
      DamlLedgerClient client,
      AppParties parties,
      Duration systemPeriodTime,
      Wirer wirer,
      Function<CommandsAndPendingSetBuilder.Factory, LedgerApiHandle> handlerFactory) {
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
      wirer.wire(
          null,
          null,
          null,
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
      wirer.wire(
          null,
          null,
          null,
          APPLICATION_ID,
          client,
          dataProviderBot.getTransactionFilter(),
          dataProviderBot::calculateCommands,
          dataProviderBot::getContractInfo);
    }

    if (parties.hasOperator()) {
      logger.info("Starting automation for Operator.");
      TimeUpdaterBot timeUpdaterBot =
          new TimeUpdaterBot(handlerFactory.apply(commandBuilderFactory));
      scheduler = Executors.newScheduledThreadPool(1);
      timeUpdaterBotExecutor = new TimeUpdaterBotExecutor(scheduler);
      timeUpdaterBotExecutor.start(timeUpdaterBot, systemPeriodTime);
    }
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

  public static void runBotsWithJsonApi(
      String ledgerId,
      AppParties parties,
      Duration systemPeriodTime,
      Wirer wirer,
      Function<CommandsAndPendingSetBuilder.Factory, LedgerApiHandle> handlerFactory) {
    Duration mrt = Duration.ofSeconds(10);
    CommandsAndPendingSetBuilder.Factory commandBuilderFactory =
        CommandsAndPendingSetBuilder.factory(APPLICATION_ID, Clock::systemUTC, mrt);

    if (parties.hasMarketDataProvider1()) {
      logger.info("Starting automation for MarketDataProvider1.");
      PublishingDataProvider dataProvider = new CachingCsvDataProvider();
      DataProviderBot dataProviderBot =
          new DataProviderBot(
              commandBuilderFactory, parties.getMarketDataProvider1(), dataProvider);
      wirer.wire(
          ledgerId,
          dataProviderBot.getPartyName(),
          dataProviderBot.getContractQuery(),
          null,
          null,
          null,
          dataProviderBot::calculateCommands,
          null);
    }

    if (parties.hasMarketDataProvider2()) {
      logger.info("Starting automation for MarketDataProvider2.");
      PublishingDataProvider dataProvider = new CachingCsvDataProvider();
      DataProviderBot dataProviderBot =
          new DataProviderBot(
              commandBuilderFactory, parties.getMarketDataProvider2(), dataProvider);
      wirer.wire(
          ledgerId,
          dataProviderBot.getPartyName(),
          dataProviderBot.getContractQuery(),
          null,
          null,
          null,
          dataProviderBot::calculateCommands,
          null);
    }

    if (parties.hasOperator()) {
      logger.info("Starting automation for Operator.");
      TimeUpdaterBot timeUpdaterBot =
          new TimeUpdaterBot(handlerFactory.apply(commandBuilderFactory));
      scheduler = Executors.newScheduledThreadPool(1);
      timeUpdaterBotExecutor = new TimeUpdaterBotExecutor(scheduler);
      timeUpdaterBotExecutor.start(timeUpdaterBot, systemPeriodTime);
    }
  }

  public static void wire(
      String ledgerId,
      String party,
      ContractQuery contractQuery,
      Function<LedgerView<Template>, Flowable<CommandsAndPendingSet>> bot) {

    String jwt = Jwt.createToken(ledgerId, APPLICATION_ID, Collections.singletonList(party));
    ApacheHttpClient httpClient =
        new ApacheHttpClient(httpResponseDeserializer, jsonSerializer, jwt);
    TyrusWebSocketClient webSocketClient =
        new TyrusWebSocketClient(webSocketResponseDeserializer, jsonSerializer, jwt);
    Api api = new Api("localhost", 7575);

    JsonLedgerClient ledgerClient =
        new JsonLedgerClient(httpClient, webSocketClient, jsonSerializer, api);

    ledgerClient
        .getActiveContracts(contractQuery)
        .map(Main::toLedgerView)
        .flatMap(bot::apply)
        .forEach(
            cps ->
                cps.getSubmitCommandsRequest().getCommands().forEach(submitCommand(ledgerClient)));
  }

  private static Consumer<? super Command> submitCommand(JsonLedgerClient ledgerClient) {
    return command -> {
      command.asExerciseCommand().ifPresent(ledgerClient::exerciseChoice);
      command.asCreateCommand().ifPresent(ledgerClient::create);
    };
  }

  static LedgerView<Template> toLedgerView(ActiveContractSet activeContractSet) {
    return activeContractSet
        .getActiveContracts()
        .reduce(createEmptyLedgerView(), Main::addActiveContract, (x, y) -> x);
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

  private static LedgerTestView<Template> addActiveContract(
      LedgerTestView<Template> emptyLedgerView, ActiveContract activeContract) {
    return emptyLedgerView.addActiveContract(
        activeContract.getIdentifier(),
        activeContract.getContractId(),
        activeContract.getTemplate());
  }

  private static LedgerTestView<Template> createEmptyLedgerView() {
    return new LedgerTestView<>(
        HashTreePMap.empty(), HashTreePMap.empty(), HashTreePMap.empty(), HashTreePMap.empty());
  }

  private static void logPackages(DamlLedgerClient client) {
    StringBuilder sb = new StringBuilder("Listing packages:");
    client.getPackageClient().listPackages().forEach(id -> sb.append("\n").append(id));
    logger.debug(sb.toString());
  }
}
