/*
 * Copyright (c) 2019, Digital Asset (Switzerland) GmbH and/or its affiliates. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package jsonapi;

import static com.digitalasset.refapps.marketdataservice.Main.runBotsWithJson;
import static com.digitalasset.refapps.marketdataservice.utils.AppParties.ALL_PARTIES;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import com.daml.ledger.javaapi.data.Party;
import com.digitalasset.refapps.marketdataservice.utils.AppParties;
import com.digitalasset.testing.junit4.Sandbox;
import com.digitalasset.testing.ledger.DefaultLedgerAdapter;
import com.digitalasset.testing.utils.ContractWithId;
import da.timeservice.timeservice.CurrentTime;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
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

  @Test
  public void getActiveContracts() throws IOException {
    CurrentTime currentTime =
        new CurrentTime(
            OPERATOR.getValue(), Instant.parse("2020-02-04T22:57:29Z"), Collections.emptyList());
    ledger.createContract(OPERATOR, CurrentTime.TEMPLATE_ID, currentTime.toValue());

    JsonLedgerClient ledger =
        new JsonLedgerClient(httpClient, webSocketClient, jsonSerializer, api);
    ActiveContractSet result = ledger.getActiveContracts();

    List<Contract<CurrentTime>> currentTimes =
        result
            .getActiveContracts(CurrentTime.TEMPLATE_ID, CurrentTime.class)
            .collect(Collectors.toList());
    assertThat(currentTimes.size(), is(1));
    assertThat(currentTimes.get(0).getContract(), is(currentTime));
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
  public void usingDataProviderBot() {
    Duration systemPeriodTime = Duration.ofSeconds(5);
    AppParties parties = new AppParties(ALL_PARTIES);
    runBotsWithJson(ledgerId, parties, systemPeriodTime);
    // TODO: Proper test and assertion.
    //    Thread.currentThread().join();
  }
}
