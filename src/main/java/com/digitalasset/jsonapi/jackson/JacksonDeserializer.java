/*
 * Copyright (c) 2019, Digital Asset (Switzerland) GmbH and/or its affiliates. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */

package com.digitalasset.jsonapi.jackson;

import com.digitalasset.jsonapi.http.HttpResponse;
import com.digitalasset.jsonapi.json.JsonDeserializer;

public class JacksonDeserializer {

  public JsonDeserializer<HttpResponse> getHttpResponseDeserializer() {
    throw new UnsupportedOperationException("Not implemented.");
  }
}
