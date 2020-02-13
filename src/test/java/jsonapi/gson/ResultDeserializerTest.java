/*
 * Copyright (c) 2019, Digital Asset (Switzerland) GmbH and/or its affiliates. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package jsonapi.gson;

import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

import com.daml.ledger.javaapi.data.Identifier;
import com.google.gson.Gson;
import com.google.gson.JsonDeserializer;
import da.refapps.marketdataservice.roles.OperatorRole;
import da.timeservice.timeservice.CurrentTime;
import java.time.Instant;
import java.util.Collection;
import java.util.Collections;
import jsonapi.events.CreatedEvent;
import jsonapi.http.ArchivedEventHolder;
import jsonapi.http.CreatedEventHolder;
import jsonapi.http.EventHolder;
import jsonapi.http.HttpResponse;
import jsonapi.http.HttpResponse.CreateResult;
import jsonapi.http.HttpResponse.Result;
import org.junit.Test;

public class ResultDeserializerTest extends DeserializerBaseTest<HttpResponse.Result> {

  @Test
  public void deserializeCreateResult() {
    String templateId = identifierToJson(CurrentTime.TEMPLATE_ID);
    String json =
        "{ \n"
            + "   \"observers\":[ \n"
            + "\n"
            + "   ],\n"
            + "   \"agreementText\":\"\",\n"
            + "   \"payload\":{ \n"
            + "      \"operator\":\"Operator\",\n"
            + "      \"currentTime\":\"2020-02-13T10:53:19.898140Z\",\n"
            + "      \"observers\":[ \n"
            + "\n"
            + "      ]\n"
            + "   },\n"
            + "   \"signatories\":[ \n"
            + "      \"Operator\"\n"
            + "   ],\n"
            + "   \"key\":\"Operator\",\n"
            + "   \"contractId\":\"#11:0\",\n"
            + "   \"templateId\":"
            + templateId
            + "\n"
            + "}";
    registerDeserializer(Identifier.class, new IdentifierDeserializer());
    registerDeserializer(Instant.class, new InstantDeserializer());
    registerDeserializer(CreatedEvent.class, new CreatedEventDeserializer());
    registerDeserializer(CreateResult.class, new CreateResultDeserializer());
    Gson deserializer = createDeserializer();
    Result result = deserializer.fromJson(json, Result.class);

    assertThat(result, instanceOf(CreateResult.class));
    CreatedEvent actual = ((CreateResult) result).getCreatedEvent();
    CreatedEvent expected =
        new CreatedEvent(
            CurrentTime.TEMPLATE_ID,
            "#11:0",
            new CurrentTime(
                "Operator", Instant.parse("2020-02-13T10:53:19.898140Z"), Collections.emptyList()));
    assertThat(actual, is(expected));
  }

  @Test
  public void deserializeSearchResult() {
    String templateId = identifierToJson(OperatorRole.TEMPLATE_ID);
    String serializedResult =
        "[ \n"
            + "   { \n"
            + "      \"observers\":[ \n"
            + "\n"
            + "      ],\n"
            + "      \"agreementText\":\"\",\n"
            + "      \"payload\":{ \n"
            + "         \"operator\":\"Operator\"\n"
            + "      },\n"
            + "      \"signatories\":[ \n"
            + "         \"Operator\"\n"
            + "      ],\n"
            + "      \"key\":\"Operator\",\n"
            + "      \"contractId\":\"#11:0\",\n"
            + "      \"templateId\":"
            + templateId
            + "\n"
            + "   }\n"
            + "]";
    registerDeserializer(HttpResponse.SearchResult.class, new SearchResultDeserializer());
    registerDeserializer(CreatedEvent.class, new CreatedEventDeserializer());
    registerDeserializer(Identifier.class, new IdentifierDeserializer());
    Gson deserializer = createDeserializer();
    Result result = deserializer.fromJson(serializedResult, Result.class);
    assertThat(result, instanceOf(HttpResponse.SearchResult.class));
    assertEquals(1, ((HttpResponse.SearchResult) result).getCreatedEvents().size());
  }

  @Test
  public void deserializeExerciseResult() {
    String tid = new GsonSerializer().apply(OperatorRole.TEMPLATE_ID);
    String serializedExerciseResult =
        "{ \n"
            + "  \"exerciseResult\":\"#14:1\",\n"
            + "  \"events\":[ \n"
            + "     { \n"
            + "        \"archived\":{ \n"
            + "           \"contractId\":\"#12:0\",\n"
            + "           \"templateId\":"
            + tid
            + "\n"
            + "        }\n"
            + "     },\n"
            + "     { \n"
            + "        \"created\":{ \n"
            + "           \"payload\":{ \n"
            + "              \"operator\":\"Operator\"\n"
            + "           },\n"
            + "           \"contractId\":\"#14:1\",\n"
            + "           \"templateId\":"
            + tid
            + "\n"
            + "        }\n"
            + "     }\n"
            + "  ]\n"
            + "}\n";
    registerDeserializer(HttpResponse.SearchResult.class, new SearchResultDeserializer());
    registerDeserializer(CreatedEvent.class, new CreatedEventDeserializer());
    registerDeserializer(EventHolder.class, new EventHolderDeserializer());
    registerDeserializer(Identifier.class, new IdentifierDeserializer());
    Gson deserializer = createDeserializer();
    HttpResponse.ExerciseResult deserializedExerciseResult =
        deserializer.fromJson(serializedExerciseResult, HttpResponse.ExerciseResult.class);

    Collection<EventHolder> events = deserializedExerciseResult.getEvents();
    assertEquals(2, events.size());
    assertThat(events, hasItem(instanceOf(CreatedEventHolder.class)));
    assertThat(events, hasItem(instanceOf(ArchivedEventHolder.class)));
  }

  private String identifierToJson(Identifier templateId) {
    return new GsonSerializer().apply(templateId);
  }

  @Override
  protected Class<Result> getDeserializedClass() {
    return Result.class;
  }

  @Override
  protected JsonDeserializer<Result> getClassDeserializer() {
    return new ResultDeserializer();
  }
}
