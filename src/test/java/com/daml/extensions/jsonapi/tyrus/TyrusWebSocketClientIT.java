/*
 * Copyright (c) 2019, Digital Asset (Switzerland) GmbH and/or its affiliates. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package com.daml.extensions.jsonapi.tyrus;

import static org.hamcrest.CoreMatchers.everyItem;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import com.daml.extensions.testing.junit4.Sandbox;
import com.daml.extensions.testing.ledger.DefaultLedgerAdapter;
import com.daml.ledger.javaapi.data.Party;
import com.daml.extensions.jsonapi.ContractQuery;
import com.daml.extensions.jsonapi.JsonApi;
import com.daml.extensions.jsonapi.events.CreatedEvent;
import com.daml.extensions.jsonapi.events.Event;
import com.daml.extensions.jsonapi.gson.GsonDeserializer;
import com.daml.extensions.jsonapi.gson.GsonSerializer;
import com.daml.extensions.jsonapi.http.Api;
import com.daml.extensions.jsonapi.http.Jwt;
import com.daml.extensions.jsonapi.http.WebSocketClient;
import com.daml.extensions.jsonapi.http.WebSocketResponse;
import com.daml.extensions.jsonapi.json.JsonDeserializer;
import com.google.protobuf.InvalidProtocolBufferException;
import da.refapps.marketdataservice.roles.OperatorRole;
import da.timeservice.timeservice.CurrentTime;
import io.reactivex.Flowable;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExternalResource;
import org.junit.rules.RuleChain;
import org.junit.rules.TestRule;

public class TyrusWebSocketClientIT {

  private static final Path RELATIVE_DAR_PATH = Paths.get("target/market-data-service.dar");
  private static final String OPERATOR = "Operator";

  private static final Sandbox sandbox =
      Sandbox.builder().dar(RELATIVE_DAR_PATH).useWallclockTime().build();

  @ClassRule public static ExternalResource startSandbox = sandbox.getClassRule();
  private final Api api = new Api("localhost", 7575);

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
  public void queryingMultipleTemplateTypes() throws InvalidProtocolBufferException {
    CurrentTime currentTime = new CurrentTime(OPERATOR, Instant.now(), Collections.emptyList());
    Party party = new Party(OPERATOR);
    ledger.createContract(party, CurrentTime.TEMPLATE_ID, currentTime.toValue());
    OperatorRole operatorRole = new OperatorRole(OPERATOR);
    ledger.createContract(party, OperatorRole.TEMPLATE_ID, operatorRole.toValue());

    ContractQuery query =
        new ContractQuery(Arrays.asList(OperatorRole.TEMPLATE_ID, CurrentTime.TEMPLATE_ID));

    JsonDeserializer<WebSocketResponse> deserializer =
        new GsonDeserializer().getWebSocketResponseDeserializer();
    WebSocketClient client = new TyrusWebSocketClient(deserializer, new GsonSerializer(), jwt);
    Flowable<WebSocketResponse> response = client.post(api.searchContractsForever(), query);

    List<ArrayList<Event>> values =
        response
            .map(webSocketResponse -> new ArrayList<>(webSocketResponse.getEvents().get()))
            .test()
            .awaitCount(2)
            .values();

    assertThat(values.get(0), everyItem(instanceOf(CreatedEvent.class)));
    assertThat(values.get(0).size(), is(1));
    CreatedEvent createdEvent = (CreatedEvent) values.get(0).get(0);
    CurrentTime.ContractId currentTimeCid =
        ledger.getCreatedContractId(party, CurrentTime.TEMPLATE_ID, CurrentTime.ContractId::new);
    CreatedEvent expectedCreatedEvent =
        new CreatedEvent(CurrentTime.TEMPLATE_ID, currentTimeCid.contractId, currentTime);
    assertThat(createdEvent, is(expectedCreatedEvent));

    assertThat(values.get(1), everyItem(instanceOf(CreatedEvent.class)));
    assertThat(values.get(1).size(), is(1));
    createdEvent = (CreatedEvent) values.get(1).get(0);
    OperatorRole.ContractId operatorRoleCid =
        ledger.getCreatedContractId(party, OperatorRole.TEMPLATE_ID, OperatorRole.ContractId::new);
    expectedCreatedEvent =
        new CreatedEvent(OperatorRole.TEMPLATE_ID, operatorRoleCid.contractId, operatorRole);
    assertThat(createdEvent, is(expectedCreatedEvent));
  }
}
