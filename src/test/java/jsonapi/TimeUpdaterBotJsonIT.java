/*
 * Copyright (c) 2019, Digital Asset (Switzerland) GmbH and/or its affiliates. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package jsonapi;

import static com.digitalasset.refapps.utils.EventuallyUtil.eventually;
import static org.junit.Assert.assertTrue;

import com.daml.ledger.javaapi.data.Party;
import com.digitalasset.refapps.marketdataservice.extensions.RelTime;
import com.digitalasset.refapps.marketdataservice.timeservice.JsonLedgerApiHandle;
import com.digitalasset.refapps.marketdataservice.timeservice.LedgerApiHandle;
import com.digitalasset.refapps.marketdataservice.timeservice.TimeUpdaterBot;
import com.digitalasset.refapps.marketdataservice.timeservice.TimeUpdaterBotExecutor;
import com.digitalasset.testing.junit4.Sandbox;
import com.digitalasset.testing.ledger.DefaultLedgerAdapter;
import com.digitalasset.testing.utils.ContractWithId;
import com.google.protobuf.InvalidProtocolBufferException;
import da.timeservice.timeservice.CurrentTime;
import da.timeservice.timeservice.CurrentTime.ContractId;
import da.timeservice.timeservice.TimeConfiguration;
import da.timeservice.timeservice.TimeManager;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.Instant;
import java.util.Collections;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import jsonapi.gson.GsonDeserializer;
import jsonapi.gson.GsonSerializer;
import jsonapi.http.HttpResponse;
import jsonapi.http.WebSocketResponse;
import jsonapi.json.JsonDeserializer;
import org.junit.After;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExternalResource;
import org.junit.rules.RuleChain;
import org.junit.rules.TestRule;

public class TimeUpdaterBotJsonIT {

  private static final Path RELATIVE_DAR_PATH = Paths.get("target/market-data-service.dar");
  private static final Party OPERATOR = new Party("Operator");
  private static final String APPLICATION_ID = "market-data-service";

  private static final Sandbox sandbox =
      Sandbox.builder()
          .dar(RELATIVE_DAR_PATH)
          .parties(OPERATOR.getValue())
          .useWallclockTime()
          .build();
  @ClassRule public static ExternalResource startSandbox = sandbox.getClassRule();

  @Rule
  public final TestRule processes =
      RuleChain.outerRule(sandbox.getRule()).around(new JsonApi(sandbox::getSandboxPort));

  private final GsonSerializer jsonSerializer = new GsonSerializer();
  private final GsonDeserializer jsonDeserializer = new GsonDeserializer();
  private final JsonDeserializer<HttpResponse> httpResponseDeserializer =
      jsonDeserializer.getHttpResponseDeserializer();
  private final JsonDeserializer<WebSocketResponse> webSocketResponseDeserializer =
      jsonDeserializer.getWebSocketResponseDeserializer();

  private ScheduledExecutorService scheduler;
  private DefaultLedgerAdapter ledger;
  private LedgerApiHandle apiHandle;

  @Before
  public void setup() {
    ledger = sandbox.getLedgerAdapter();
    String ledgerId = sandbox.getClient().getLedgerId();
    scheduler = Executors.newScheduledThreadPool(1);
    apiHandle =
        new JsonLedgerApiHandle(
            OPERATOR.getValue(),
            ledgerId,
            APPLICATION_ID,
            httpResponseDeserializer,
            jsonSerializer,
            webSocketResponseDeserializer);
  }

  @After
  public void tearDown() {
    scheduler.shutdown();
  }

  @Test
  public void updateTime() throws InterruptedException, InvalidProtocolBufferException {
    CurrentTime currentTime =
        new CurrentTime(
            OPERATOR.getValue(), Instant.parse("1955-11-12T10:04:00Z"), Collections.emptyList());
    ledger.createContract(OPERATOR, CurrentTime.TEMPLATE_ID, currentTime.toValue());
    ledger.createContract(
        OPERATOR,
        TimeConfiguration.TEMPLATE_ID,
        new TimeConfiguration(OPERATOR.getValue(), false, RelTime.fromDuration(Duration.ofHours(2)))
            .toValue());
    ledger.createContract(
        OPERATOR, TimeManager.TEMPLATE_ID, new TimeManager(OPERATOR.getValue()).toValue());
    TimeManager.ContractId managerCid =
        ledger.getCreatedContractId(OPERATOR, TimeManager.TEMPLATE_ID, TimeManager.ContractId::new);

    TimeUpdaterBot timeUpdaterBot = new TimeUpdaterBot(apiHandle);
    TimeUpdaterBotExecutor botExecutor = new TimeUpdaterBotExecutor(scheduler);
    botExecutor.start(timeUpdaterBot, Duration.ofSeconds(5));
    ledger.exerciseChoice(OPERATOR, managerCid.exerciseContinue());

    eventually(
        () -> {
          ContractWithId<ContractId> contractWithId =
              ledger.getMatchedContract(OPERATOR, CurrentTime.TEMPLATE_ID, ContractId::new);
          CurrentTime updatedTime = CurrentTime.fromValue(contractWithId.record);
          assertTrue(currentTime.currentTime.isBefore(updatedTime.currentTime));
        });
  }
}
