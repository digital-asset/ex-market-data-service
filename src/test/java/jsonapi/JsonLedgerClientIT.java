/*
 * Copyright (c) 2019, Digital Asset (Switzerland) GmbH and/or its affiliates. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package jsonapi;

import static com.digitalasset.refapps.marketdataservice.utils.AppParties.ALL_PARTIES;
import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.Assert.assertThat;

import com.daml.ledger.javaapi.data.Party;
import com.daml.ledger.rxjava.DamlLedgerClient;
import com.digitalasset.refapps.marketdataservice.Main;
import com.digitalasset.refapps.marketdataservice.utils.AppParties;
import com.digitalasset.testing.junit4.Sandbox;
import com.digitalasset.testing.ledger.DefaultLedgerAdapter;
import com.digitalasset.testing.utils.ContractWithId;
import da.timeservice.timeservice.CurrentTime;
import io.grpc.ManagedChannel;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.Collections;
import jsonapi.apache.ApacheHttpClient;
import jsonapi.gson.GsonDeserializer;
import jsonapi.gson.GsonSerializer;
import jsonapi.http.Api;
import jsonapi.http.HttpClient;
import jsonapi.http.HttpResponse;
import jsonapi.http.Jwt;
import jsonapi.http.WebSocketClient;
import jsonapi.http.WebSocketResponse;
import jsonapi.json.JsonDeserializer;
import jsonapi.tyrus.TyrusWebSocketClient;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExternalResource;
import org.junit.rules.RuleChain;
import org.junit.rules.TestRule;

public class JsonLedgerClientIT {

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

  private DefaultLedgerAdapter ledger;
  private HttpClient httpClient;
  private WebSocketClient webSocketClient;
  private Api api;
  private String ledgerId;

  @Before
  public void setUp() {
    ledger = sandbox.getLedgerAdapter();
    ledgerId = sandbox.getClient().getLedgerId();
    String jwt =
        Jwt.createToken(ledgerId, APPLICATION_ID, Collections.singletonList(OPERATOR.getValue()));
    httpClient = new ApacheHttpClient(httpResponseDeserializer, jsonSerializer, jwt);
    webSocketClient = new TyrusWebSocketClient(webSocketResponseDeserializer, jsonSerializer, jwt);
    api = new Api("localhost", 7575);
  }

  @Ignore
  @Test
  public void getActiveContracts() throws IOException {
    CurrentTime currentTime =
        new CurrentTime(
            OPERATOR.getValue(), Instant.parse("2020-02-04T22:57:29Z"), Collections.emptyList());
    ledger.createContract(OPERATOR, CurrentTime.TEMPLATE_ID, currentTime.toValue());

    JsonLedgerClient ledger =
        new JsonLedgerClient(httpClient, webSocketClient, jsonSerializer, api);
    String result = ledger.getActiveContracts();

    assertThat(result, containsString("\"status\":200"));
    assertThat(
        result,
        containsString(
            "\"payload\":{\"operator\":\"Operator\",\"currentTime\":\"2020-02-04T22:57:29Z\",\"observers\":[]}"));
    assertThat(
        result,
        containsString(
            "\"templateId\":\"b4eb9b86bb78db2acde90edf0a03d96e5d65cc7a7cc422f23b6d98a286e07c09:DA.TimeService.TimeService:CurrentTime\""));
  }

  @Test
  public void exerciseChoice() throws IOException {
    CurrentTime currentTime =
        new CurrentTime(
            OPERATOR.getValue(), Instant.parse("2020-02-04T22:57:29Z"), Collections.emptyList());
    ledger.createContract(OPERATOR, CurrentTime.TEMPLATE_ID, currentTime.toValue());
    ContractWithId<CurrentTime.ContractId> currentTimeWithId =
        ledger.getMatchedContract(OPERATOR, CurrentTime.TEMPLATE_ID, CurrentTime.ContractId::new);

    JsonLedgerClient ledger =
        new JsonLedgerClient(httpClient, webSocketClient, jsonSerializer, api);
    String result =
        ledger.exerciseChoice(
            currentTimeWithId.contractId.exerciseCurrentTime_AddObserver(OPERATOR.getValue()));

    assertThat(result, containsString("\"status\":200"));
    assertThat(
        result,
        containsString(
            "\"payload\":{\"operator\":\"Operator\",\"currentTime\":\"2020-02-04T22:57:29Z\",\"observers\":[\"Operator\"]}"));
  }

  @Test
  public void usingDataProviderBot()
      throws NoSuchFieldException, IllegalAccessException, InterruptedException {
    String marketDataProvider1 = new AppParties(ALL_PARTIES).getMarketDataProvider1();
    String jwt =
        Jwt.createToken(ledgerId, APPLICATION_ID, Collections.singletonList(marketDataProvider1));
    httpClient = new ApacheHttpClient(httpResponseDeserializer, jsonSerializer, jwt);
    webSocketClient = new TyrusWebSocketClient(webSocketResponseDeserializer, jsonSerializer, jwt);
    api = new Api("localhost", 7575);

    JsonLedgerClient ledger =
        new JsonLedgerClient(httpClient, webSocketClient, jsonSerializer, api);
    Main.runBotsWithJsonApi(new AppParties(ALL_PARTIES), null)
        .accept(ledger, getManagedChannel(sandbox.getClient()));

    // TODO: Proper test and assertion.
    //    Thread.currentThread().join();
  }

  private ManagedChannel getManagedChannel(DamlLedgerClient client)
      throws NoSuchFieldException, IllegalAccessException {
    Field channel = client.getClass().getDeclaredField("channel");
    channel.setAccessible(true);
    return (ManagedChannel) channel.get(client);
  }
}
