/*
 * Copyright (c) 2019, Digital Asset (Switzerland) GmbH and/or its affiliates. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package jsonapi.gson;

import com.daml.ledger.javaapi.data.Party;
import com.digitalasset.refapps.marketdataservice.extensions.RelTime;
import com.digitalasset.testing.junit4.Sandbox;
import com.digitalasset.testing.ledger.DefaultLedgerAdapter;
import com.digitalasset.testing.utils.ContractWithId;
import com.google.gson.Gson;
import com.google.protobuf.InvalidProtocolBufferException;
import da.timeservice.timeservice.CurrentTime;
import da.timeservice.timeservice.TimeConfiguration;
import da.timeservice.timeservice.TimeManager;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.Key;
import java.time.Duration;
import java.time.Instant;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import jsonapi.JsonApi;
import jsonapi.JsonLedgerClient;
import jsonapi.LedgerClient;
import jsonapi.apache.ApacheHttpClient;
import jsonapi.gson.GsonRegisteredAllDeserializers;
import jsonapi.gson.GsonSerializer;
import jsonapi.http.Api;
import jsonapi.http.HttpClient;
import jsonapi.http.HttpResponse;
import jsonapi.http.WebSocketClient;
import jsonapi.http.WebSocketResponse;
import jsonapi.tyrus.TyrusWebSocketClient;
import org.junit.Assert;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExternalResource;
import org.junit.rules.RuleChain;
import org.junit.rules.TestRule;

// TODO: Decide if this test is needed and act accordingly.
// This test is meant to validate if the actual serialized JSON representations can be processed
// by the ledger. Currently this is not extensive, and it is debated how useful it is and how costly
// it is to make it extensive and run it.
// JSON se/deserialization should be replaced with LF Value JSON and that would make this test and
// usage of Gson obsolete.
public class JsonSerializeExerciseIT {

  private static final Path RELATIVE_DAR_PATH = Paths.get("target/market-data-service.dar");
  private static final Party OPERATOR = new Party("Operator");

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

  private final Gson json = GsonRegisteredAllDeserializers.gson();

  private DefaultLedgerAdapter ledger;
  private HttpClient httpClient;
  private WebSocketClient webSocketClient;
  private Api api;

  @Before
  public void setUp() {
    ledger = sandbox.getLedgerAdapter();
    Key key = Keys.secretKeyFor(SignatureAlgorithm.HS256);
    Map<String, Object> claim = new HashMap<>();
    claim.put("ledgerId", sandbox.getClient().getLedgerId());
    claim.put("applicationId", "market-data-service");
    claim.put("actAs", Collections.singletonList("Operator"));
    Map<String, Object> claims = Collections.singletonMap("https://daml.com/ledger-api", claim);
    String jwt = Jwts.builder().setClaims(claims).signWith(key).compact();
    httpClient = new ApacheHttpClient(this::fromJson, new GsonSerializer(), jwt);
    webSocketClient = new TyrusWebSocketClient(this::fromJsonWs, new GsonSerializer(), jwt);
    api = new Api("localhost", 7575);
  }

  @Test
  public void AdvanceCurrentTimeIsCompletedByTheLedger() throws IOException {
    Instant startTime = Instant.parse("2020-02-04T22:57:29Z");
    Duration modelPeriodTime = Duration.ofHours(1);
    setupTimeContracts(startTime, modelPeriodTime);
    ContractWithId<TimeManager.ContractId> timeManagerWithId =
        ledger.getMatchedContract(OPERATOR, TimeManager.TEMPLATE_ID, TimeManager.ContractId::new);
    LedgerClient ledgerClient = new JsonLedgerClient(httpClient, webSocketClient, api);
    ledgerClient.exerciseChoice(timeManagerWithId.contractId.exerciseAdvanceCurrentTime());
    getNextCurrentTime();
    CurrentTime currentTime = getNextCurrentTime();
    Assert.assertEquals(startTime.plus(modelPeriodTime), currentTime.currentTime);
  }

  private CurrentTime getNextCurrentTime() {
    ContractWithId<CurrentTime.ContractId> currentTimeWithId =
        ledger.getMatchedContract(OPERATOR, CurrentTime.TEMPLATE_ID, CurrentTime.ContractId::new);
    return CurrentTime.fromValue(currentTimeWithId.record);
  }

  private void setupTimeContracts(Instant startTime, Duration modelPeriodTime)
      throws InvalidProtocolBufferException {
    CurrentTime currentTime =
        new CurrentTime(OPERATOR.getValue(), startTime, Collections.emptyList());
    ledger.createContract(OPERATOR, CurrentTime.TEMPLATE_ID, currentTime.toValue());
    TimeConfiguration timeConfiguration =
        new TimeConfiguration(OPERATOR.getValue(), true, RelTime.fromDuration(modelPeriodTime));
    ledger.createContract(OPERATOR, TimeConfiguration.TEMPLATE_ID, timeConfiguration.toValue());
    TimeManager timeManager = new TimeManager(OPERATOR.getValue());
    ledger.createContract(OPERATOR, TimeManager.TEMPLATE_ID, timeManager.toValue());
  }

  private HttpResponse fromJson(InputStream is) {
    return json.fromJson(new InputStreamReader(is), HttpResponse.class);
  }

  private WebSocketResponse fromJsonWs(InputStream is) {
    return json.fromJson(new InputStreamReader(is), WebSocketResponse.class);
  }
}
