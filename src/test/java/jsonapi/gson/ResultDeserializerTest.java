/*
 * Copyright (c) 2019, Digital Asset (Switzerland) GmbH and/or its affiliates. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package jsonapi.gson;

import com.daml.ledger.javaapi.data.Identifier;
import com.google.gson.Gson;
import com.google.gson.JsonDeserializer;
import da.refapps.marketdataservice.roles.OperatorRole;
import java.lang.reflect.Type;
import jsonapi.events.CreatedEvent;
import jsonapi.http.HttpResponse;
import jsonapi.http.HttpResponse.Result;
import org.junit.Test;

public class ResultDeserializerTest extends DeserializerBaseTest<HttpResponse.Result> {

  @Test
  public void deserializeCreateResult() {
    String serializedResult =
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
            + "   \"templateId\":\"230a15b6240603917c18612a7dcb83a7040ab1cf8d498bb4b523b5de03659f58:DA.TimeService.TimeService:CurrentTime\"\n"
            + "}";
    Gson deserializer = createDeserializer();
    deserializer.fromJson(serializedResult, HttpResponse.Result.class);
  }

  @Test
  public void deserializeSearchResult() {
    String tid = new GsonSerializer().apply(OperatorRole.TEMPLATE_ID);
    String serializedResult =
        "[ \n"
            + "   { \n"
            + "      \"observers\":[ \n"
            + "\n"
            + "      ],\n"
            + "      \"agreementText\":\"\",\n"
            + "      \"payload\":{ \n"
            + "         \"operator\":\"Operator\",\n"
            + "         \"currentTime\":\"2020-02-04T22:57:29Z\",\n"
            + "         \"observers\":[ \n"
            + "\n"
            + "         ]\n"
            + "      },\n"
            + "      \"signatories\":[ \n"
            + "         \"Operator\"\n"
            + "      ],\n"
            + "      \"key\":\"Operator\",\n"
            + "      \"contractId\":\"#11:0\",\n"
            + "      \"templateId\":"
            + tid
            + "\n"
            + "   }\n"
            + "]";
    registerDeserializer(HttpResponse.SearchResult.class, new SearchResultDeserializer());
    registerDeserializer(CreatedEvent.class, new CreatedEventDeserializer());
    registerDeserializer(Identifier.class, new IdentifierDeserializer());
    Gson deserializer = createDeserializer();
    deserializer.fromJson(serializedResult, HttpResponse.Result.class);
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
