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
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.reflect.TypeToken;
import com.google.protobuf.InvalidProtocolBufferException;
import da.timeservice.timeservice.CurrentTime;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import io.reactivex.Flowable;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.Key;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import jsonapi.ContractQuery;
import jsonapi.JsonApi;
import jsonapi.events.ArchivedEvent;
import jsonapi.events.CreatedEvent;
import jsonapi.events.Event;
import jsonapi.gson.IdentifierSerializer;
import jsonapi.gson.InstantSerializer;
import jsonapi.http.Api;
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
          .registerTypeAdapter(Event.class, new EventDeserializer())
          .registerTypeAdapter(Template.class, new TemplateDeserializer())
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
    jwt = createJwt(ledgerId, Collections.singletonList(OPERATOR));
  }

  @Test
  public void getActiveContracts() throws InvalidProtocolBufferException {
    CurrentTime currentTime = new CurrentTime("Operator", Instant.now(), Collections.emptyList());
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

  private String createJwt(String ledgerId, List<String> parties) {
    Key key = Keys.secretKeyFor(SignatureAlgorithm.HS256);
    Map<String, Object> claim = new HashMap<>();
    claim.put("ledgerId", ledgerId);
    claim.put("applicationId", "market-data-service");
    claim.put("actAs", parties);
    Map<String, Object> claims = Collections.singletonMap("https://daml.com/ledger-api", claim);
    return Jwts.builder().setClaims(claims).signWith(key).compact();
  }

  private WebSocketResponse fromJson(InputStream inputStream) {
    return json.fromJson(new InputStreamReader(inputStream), WebSocketResponse.class);
  }

  private String toJson(Object o) {
    return json.toJson(o);
  }

  private static class WebSocketResponseDeserializer
      implements JsonDeserializer<WebSocketResponse> {

    @Override
    public WebSocketResponse deserialize(
        JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext)
        throws JsonParseException {
      Type eventsCollection = new TypeToken<Collection<Event>>() {}.getType();
      Collection<Event> events =
          jsonDeserializationContext.deserialize(jsonElement, eventsCollection);
      return new WebSocketResponse(events);
    }
  }

  private static class EventDeserializer implements JsonDeserializer<Event> {

    @Override
    public Event deserialize(
        JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext)
        throws JsonParseException {
      JsonObject event = jsonElement.getAsJsonObject();
      if (event.has("archive")) {
        return jsonDeserializationContext.deserialize(event.get("archived"), ArchivedEvent.class);
      } else if (event.has("created")) {
        return jsonDeserializationContext.deserialize(event.get("created"), CreatedEvent.class);
      } else {
        throw new IllegalStateException("Unsupported event type.");
      }
    }
  }

  private static class TemplateDeserializer implements JsonDeserializer<Template> {

    @Override
    public Template deserialize(
        JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext)
        throws JsonParseException {
      JsonObject o = jsonElement.getAsJsonObject();
      String operator = o.getAsJsonPrimitive("operator").getAsString();
      Instant currentTime =
          jsonDeserializationContext.deserialize(o.get("currentTime"), Instant.class);
      List<String> observers =
          jsonDeserializationContext.deserialize(o.get("observers"), List.class);
      return new CurrentTime(operator, currentTime, observers);
    }
  }
}
