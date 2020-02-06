/*
 * Copyright (c) 2019, Digital Asset (Switzerland) GmbH and/or its affiliates. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package jsonapi;

import static org.hamcrest.CoreMatchers.containsString;
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
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.Collections;
import jsonapi.gson.ExerciseCommandSerializer;
import jsonapi.gson.IdentifierSerializer;
import jsonapi.gson.InstantSerializer;
import jsonapi.gson.RecordSerializer;
import jsonapi.http.HttpResponse;
import jsonapi.http.WebSocketResponse;
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

  @Before
  public void setUp() {
    ledger = sandbox.getLedgerAdapter();
  }

  @Test
  public void getActiveContracts() throws IOException {
    CurrentTime currentTime =
        new CurrentTime(
            OPERATOR.getValue(), Instant.parse("2020-02-04T22:57:29Z"), Collections.emptyList());
    ledger.createContract(OPERATOR, CurrentTime.TEMPLATE_ID, currentTime.toValue());

    JsonLedgerClient ledger =
        new JsonLedgerClient(
            sandbox.getClient().getLedgerId(), this::toJson, this::fromJson, this::fromJsonWs);
    String result = ledger.getActiveContracts();

    assertThat(result, containsString("200"));
    //    assertThat(
    //        result,
    //        containsString(
    //
    // "{\"result\":[{\"observers\":[],\"agreementText\":\"\",\"payload\":{\"operator\":\"Operator\",\"currentTime\":\"2020-02-04T22:57:29Z\",\"observers\":[]},\"signatories\":[\"Operator\"],\"key\":\"Operator\",\"contractId\":\"#0:0\",\"templateId\":\"6f14cd82bbdbf637ae067f60af1d8da0b941de2e44f4b97b12e9fe7b5f13147a:DA.TimeService.TimeService:CurrentTime\"}],\"status\":200}"));
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
        new JsonLedgerClient(
            sandbox.getClient().getLedgerId(), this::toJson, this::fromJson, this::fromJsonWs);
    String result =
        ledger.exerciseChoice(
            currentTimeWithId.contractId.exerciseCurrentTime_AddObserver(OPERATOR.getValue()));

    assertThat(result, containsString("200"));
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
