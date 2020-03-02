/*
 * Copyright (c) 2019, Digital Asset (Switzerland) GmbH and/or its affiliates. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package com.digitalasset.jsonapi.apache;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import com.daml.ledger.javaapi.data.ExerciseCommand;
import com.daml.ledger.javaapi.data.Identifier;
import com.daml.ledger.javaapi.data.Party;
import com.daml.ledger.javaapi.data.Template;
import com.digitalasset.jsonapi.JsonApi;
import com.digitalasset.jsonapi.gson.GsonDeserializer;
import com.digitalasset.jsonapi.gson.GsonSerializer;
import com.digitalasset.jsonapi.http.Api;
import com.digitalasset.jsonapi.http.HttpClient;
import com.digitalasset.jsonapi.http.HttpResponse;
import com.digitalasset.jsonapi.http.Jwt;
import com.digitalasset.jsonapi.json.JsonDeserializer;
import com.digitalasset.testing.junit4.Sandbox;
import com.digitalasset.testing.ledger.DefaultLedgerAdapter;
import com.google.protobuf.InvalidProtocolBufferException;
import da.timeservice.timeservice.CurrentTime;
import da.timeservice.timeservice.CurrentTime.ContractId;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.Collections;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExternalResource;
import org.junit.rules.RuleChain;
import org.junit.rules.TestRule;

public class ApacheHttpClientIT {

  private static final Path RELATIVE_DAR_PATH = Paths.get("target/market-data-service.dar");
  private static final String OPERATOR = "Operator";

  private static final Sandbox sandbox =
      Sandbox.builder().dar(RELATIVE_DAR_PATH).useWallclockTime().build();

  @ClassRule public static ExternalResource startSandbox = sandbox.getClassRule();
  private final Api api = new Api("localhost", 7575);
  private final JsonDeserializer<HttpResponse> deserializer =
      new GsonDeserializer().getHttpResponseDeserializer();

  @Rule
  public TestRule processes =
      RuleChain.outerRule(sandbox.getRule()).around(new JsonApi(sandbox::getSandboxPort));

  private DefaultLedgerAdapter ledger;
  private String jwt;

  @Before
  public void setUp() {
    ledger = sandbox.getLedgerAdapter();
    String ledgerId = sandbox.getClient().getLedgerId();
    jwt = Jwt.createToken(ledgerId, "market-data-service", Collections.singletonList(OPERATOR));
  }

  @Test
  public void createContract() {
    CurrentTime currentTime = new CurrentTime("Operator", Instant.now(), Collections.emptyList());
    CreateCommand command = new CreateCommand(CurrentTime.TEMPLATE_ID, currentTime);

    HttpClient client = new ApacheHttpClient(deserializer, new GsonSerializer(), jwt);
    HttpResponse response = client.post(api.createContract(), command);

    assertThat(response.getStatus(), is(200));
  }

  @Test
  public void exerciseChoice() throws InvalidProtocolBufferException {
    CurrentTime currentTime = new CurrentTime("Operator", Instant.now(), Collections.emptyList());
    Party party = new Party(OPERATOR);
    ledger.createContract(party, CurrentTime.TEMPLATE_ID, currentTime.toValue());
    CurrentTime.ContractId contractId =
        ledger.getCreatedContractId(party, CurrentTime.TEMPLATE_ID, ContractId::new);
    ExerciseCommand command = contractId.exerciseCurrentTime_AddObserver("MarketDataVendor");

    HttpClient client = new ApacheHttpClient(deserializer, new GsonSerializer(), jwt);
    HttpResponse response = client.post(api.exercise(), command);

    assertThat(response.getStatus(), is(200));
  }

  private static class CreateCommand {

    private final Identifier templateId;
    private final Template payload;

    public CreateCommand(Identifier identifier, Template contract) {
      this.templateId = identifier;
      this.payload = contract;
    }

    public Identifier getTemplateId() {
      return templateId;
    }

    public Template getPayload() {
      return payload;
    }
  }
}
