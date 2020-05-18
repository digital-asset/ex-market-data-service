/*
 * Copyright (c) 2019, Digital Asset (Switzerland) GmbH and/or its affiliates. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */

package com.digitalasset.jsonapi.jackson;

import static com.digitalasset.jsonapi.jackson.Util.readNodeAs;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import da.refapps.marketdataservice.marketdatatypes.InstrumentId;
import java.io.IOException;

class InstrumentIdDeserializer extends StdDeserializer<InstrumentId> {
  protected InstrumentIdDeserializer(Class<?> vc) {
    super(vc);
  }

  @Override
  public InstrumentId deserialize(
      JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException {
    JsonNode o = jsonParser.readValueAsTree();
    String id = readNodeAs(jsonParser, o.path("unpack"), String.class);
    return new InstrumentId(id);
  }
}
