/*
 * Copyright (c) 2019, Digital Asset (Switzerland) GmbH and/or its affiliates. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package jsonapi.gson;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

import com.daml.ledger.javaapi.data.Identifier;
import com.google.common.collect.Iterables;
import com.google.gson.Gson;
import com.google.gson.JsonDeserializer;
import da.refapps.marketdataservice.roles.OperatorRole;
import da.timeservice.timeservice.CurrentTime;
import java.lang.reflect.Type;
import java.time.Instant;
import java.util.Collection;
import java.util.Collections;
import jsonapi.events.CreatedEvent;
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

  private String identifierToJson(Identifier templateId) {
    return new GsonSerializer().apply(templateId);
  }

  @Override
  protected Type getDeserializedClass() {
    return Result.class;
  }

  @Override
  protected JsonDeserializer<Result> getClassDeserializer() {
    return new ResultDeserializer();
  }
}
