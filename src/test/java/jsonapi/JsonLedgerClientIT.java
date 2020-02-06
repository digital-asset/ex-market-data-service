/*
 * Copyright (c) 2019, Digital Asset (Switzerland) GmbH and/or its affiliates. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package jsonapi;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import com.daml.ledger.javaapi.data.ExerciseCommand;
import com.daml.ledger.javaapi.data.Identifier;
import com.daml.ledger.javaapi.data.Party;
import com.daml.ledger.javaapi.data.Record;
import com.digitalasset.testing.junit4.Sandbox;
import com.digitalasset.testing.ledger.DefaultLedgerAdapter;
import com.digitalasset.testing.utils.ContractWithId;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import da.timeservice.timeservice.CurrentTime;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.Key;
import java.time.Instant;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import jsonapi.apache.ApacheHttpClient;
import jsonapi.gson.ExerciseCommandSerializer;
import jsonapi.gson.IdentifierSerializer;
import jsonapi.gson.InstantSerializer;
import jsonapi.gson.RecordSerializer;
import jsonapi.http.Api;
import jsonapi.http.HttpClient;
import jsonapi.http.HttpResponse;
import jsonapi.http.WebSocketClient;
import jsonapi.http.WebSocketResponse;
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

  private final Gson json =
      new GsonBuilder()
          .registerTypeAdapter(Identifier.class, new IdentifierSerializer())
          .registerTypeAdapter(Instant.class, new InstantSerializer())
          .registerTypeAdapter(Record.class, new RecordSerializer())
          .registerTypeAdapter(ExerciseCommand.class, new ExerciseCommandSerializer())
          .create();

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
    httpClient = new ApacheHttpClient(this::fromJson, this::toJson, jwt);
    webSocketClient = new TyrusWebSocketClient(this::fromJsonWs, this::toJson, jwt);
    api = new Api("localhost", 7575);
  }

  @Test
  public void getActiveContracts() throws IOException {
    CurrentTime currentTime =
        new CurrentTime(
            OPERATOR.getValue(), Instant.parse("2020-02-04T22:57:29Z"), Collections.emptyList());
    ledger.createContract(OPERATOR, CurrentTime.TEMPLATE_ID, currentTime.toValue());

    JsonLedgerClient ledger = new JsonLedgerClient(httpClient, webSocketClient, this::toJson, api);
    String result = ledger.getActiveContracts();

    assertThat(
        result,
        is(
            "{\"status\":200,\"result\":[{\"observers\":[],\"agreementText\":\"\",\"payload\":{\"operator\":\"Operator\",\"currentTime\":\"2020-02-04T22:57:29Z\",\"observers\":[]},\"signatories\":[\"Operator\"],\"key\":\"Operator\",\"contractId\":\"#10:0\",\"templateId\":\"b4eb9b86bb78db2acde90edf0a03d96e5d65cc7a7cc422f23b6d98a286e07c09:DA.TimeService.TimeService:CurrentTime\"}]}"));
  }

  @Test
  public void exerciseChoice() throws IOException {
    CurrentTime currentTime =
        new CurrentTime(
            OPERATOR.getValue(), Instant.parse("2020-02-04T22:57:29Z"), Collections.emptyList());
    ledger.createContract(OPERATOR, CurrentTime.TEMPLATE_ID, currentTime.toValue());
    ContractWithId<CurrentTime.ContractId> currentTimeWithId =
        ledger.getMatchedContract(OPERATOR, CurrentTime.TEMPLATE_ID, CurrentTime.ContractId::new);

    JsonLedgerClient ledger = new JsonLedgerClient(httpClient, webSocketClient, this::toJson, api);
    String result =
        ledger.exerciseChoice(
            currentTimeWithId.contractId.exerciseCurrentTime_AddObserver(OPERATOR.getValue()));

    assertThat(
        result,
        is(
            "{\"status\":200,\"result\":{\"exerciseResult\":\"#12:1\",\"contracts\":[{\"archived\":{\"contractId\":\"#10:0\",\"templateId\":\"b4eb9b86bb78db2acde90edf0a03d96e5d65cc7a7cc422f23b6d98a286e07c09:DA.TimeService.TimeService:CurrentTime\"}},{\"created\":{\"observers\":[],\"agreementText\":\"\",\"payload\":{\"operator\":\"Operator\",\"currentTime\":\"2020-02-04T22:57:29Z\",\"observers\":[\"Operator\"]},\"signatories\":[\"Operator\"],\"key\":\"Operator\",\"contractId\":\"#12:1\",\"templateId\":\"b4eb9b86bb78db2acde90edf0a03d96e5d65cc7a7cc422f23b6d98a286e07c09:DA.TimeService.TimeService:CurrentTime\"}}]}}"));
  }

  private String toJson(Object o) {
    return json.toJson(o);
  }

  private HttpResponse fromJson(InputStream is) {
    return json.fromJson(new InputStreamReader(is), HttpResponse.class);
  }

  private WebSocketResponse fromJsonWs(InputStream is) {
    return json.fromJson(new InputStreamReader(is), WebSocketResponse.class);
  }
}
