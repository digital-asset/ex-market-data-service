/*
 * Copyright (c) 2019, Digital Asset (Switzerland) GmbH and/or its affiliates. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */

package com.digitalasset.jsonapi.jackson;

import com.daml.ledger.javaapi.data.Date;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import java.io.IOException;
import java.time.LocalDate;

class DateDeserializer extends StdDeserializer<Date> {
  protected DateDeserializer(Class<?> vc) {
    super(vc);
  }

  @Override
  public Date deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException {
    LocalDate date = LocalDate.parse(jsonParser.getValueAsString());
    return new Date((int) date.toEpochDay());
  }
}
