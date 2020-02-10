/*
 * Copyright (c) 2019, Digital Asset (Switzerland) GmbH and/or its affiliates. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package jsonapi.tyrus;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import com.daml.ledger.javaapi.data.Identifier;
import com.daml.ledger.javaapi.data.Party;
import com.daml.ledger.javaapi.data.Template;
import com.digitalasset.testing.junit4.Sandbox;
import com.digitalasset.testing.ledger.DefaultLedgerAdapter;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.protobuf.InvalidProtocolBufferException;
import da.timeservice.timeservice.CurrentTime;
import io.reactivex.Flowable;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import jsonapi.ContractQuery;
import jsonapi.JsonApi;
import jsonapi.events.CreatedEvent;
import jsonapi.events.Event;
import jsonapi.gson.CreatedEventDeserializer;
import jsonapi.gson.EventDeserializer;
import jsonapi.gson.IdentifierSerializer;
import jsonapi.gson.InstantSerializer;
import jsonapi.gson.WebSocketResponseDeserializer;
import jsonapi.http.Api;
import jsonapi.http.Jwt;
import jsonapi.http.WebSocketClient;
import jsonapi.http.WebSocketResponse;
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
  private final Gson json =
      new GsonBuilder()
          .registerTypeAdapter(Identifier.class, new IdentifierSerializer())
          .registerTypeAdapter(WebSocketResponse.class, new WebSocketResponseDeserializer())
          .registerTypeAdapter(Event.class, new CreatedEventDeserializer())
          .registerTypeAdapter(Instant.class, new InstantSerializer())
          .create();
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
  public void getActiveContracts() throws InvalidProtocolBufferException {
    CurrentTime currentTime = new CurrentTime(OPERATOR, Instant.now(), Collections.emptyList());
    Party party = new Party(OPERATOR);
    ledger.createContract(party, CurrentTime.TEMPLATE_ID, currentTime.toValue());

    ContractQuery query = new ContractQuery(Collections.singletonList(CurrentTime.TEMPLATE_ID));

    WebSocketClient client = new TyrusWebSocketClient(this::fromJson, this::toJson, jwt);
    Flowable<WebSocketResponse> response = client.post(api.searchContractsForever(), query);

    WebSocketResponse webSocketResponse = response.blockingFirst();
    List<Event> events = new ArrayList<>(webSocketResponse.getEvents());
    assertThat(events.size(), is(1));
    CreatedEvent createdEvent = (CreatedEvent) events.get(0);
    assertThat(createdEvent.getTemplateId(), is(CurrentTime.TEMPLATE_ID));
    assertThat(createdEvent.getPayload(), is(currentTime));
  }

  private WebSocketResponse fromJson(InputStream inputStream) {
    return json.fromJson(new InputStreamReader(inputStream), WebSocketResponse.class);
  }

  private String toJson(Object o) {
    return json.toJson(o);
  }
}
