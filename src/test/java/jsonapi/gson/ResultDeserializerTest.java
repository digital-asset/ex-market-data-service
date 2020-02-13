/*
 * Copyright (c) 2019, Digital Asset (Switzerland) GmbH and/or its affiliates. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package jsonapi.gson;

import static org.junit.Assert.*;

import jsonapi.http.HttpResponse;
import org.junit.Ignore;
import org.junit.Test;

public class ResultDeserializerTest {

  @Ignore
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
    GsonRegisteredAllDeserializers.gson().fromJson(serializedResult, HttpResponse.Result.class);
  }

  @Ignore
  @Test
  public void deserializeSearchResult() {
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
            + "      \"templateId\":\"230a15b6240603917c18612a7dcb83a7040ab1cf8d498bb4b523b5de03659f58:DA.TimeService.TimeService:CurrentTime\"\n"
            + "   }\n"
            + "]";
    GsonRegisteredAllDeserializers.gson().fromJson(serializedResult, HttpResponse.Result.class);
  }
}
