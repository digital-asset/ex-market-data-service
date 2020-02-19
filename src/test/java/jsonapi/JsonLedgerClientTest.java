/*
 * Copyright (c) 2019, Digital Asset (Switzerland) GmbH and/or its affiliates. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package jsonapi;

import java.net.URI;
import java.util.Collections;
import jsonapi.gson.GsonSerializer;
import jsonapi.http.Api;
import jsonapi.http.HttpClient;
import jsonapi.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class JsonLedgerClientTest {

  private static class DummyHttpClientGivingError implements HttpClient {
    @Override
    public HttpResponse get(URI resource) {
      throw new RuntimeException("not implemented");
    }

    @Override
    public HttpResponse post(URI resource, Object body) {
      return new HttpResponse(
          HttpStatus.SC_BAD_REQUEST,
          null,
          Collections.singletonList(someBadRequestErrorMessage),
          null);
    }
  }

  private static final String someBadRequestErrorMessage = "some bad request error message";
  private static final GsonSerializer jsonSerializer = new GsonSerializer();
  private static HttpClient httpClient = new DummyHttpClientGivingError();
  private static Api api = new Api(null, 0);

  @Rule public ExpectedException exceptionRule = ExpectedException.none();

  @Test
  public void exerciseChoiceThrowsForHttpError() {
    JsonLedgerClient ledger = new JsonLedgerClient(httpClient, null, jsonSerializer, api);
    exceptionRule.expectMessage(someBadRequestErrorMessage);
    ledger.exerciseChoice(null);
  }
}
