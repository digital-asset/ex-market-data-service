/*
 * Copyright (c) 2019, Digital Asset (Switzerland) GmbH and/or its affiliates. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package com.digitalasset.jsonapi.jackson;

import static com.digitalasset.jsonapi.jackson.Util.readNodeAs;

import com.daml.ledger.javaapi.data.Identifier;
import com.daml.ledger.javaapi.data.Template;
import com.digitalasset.jsonapi.ClassName;
import com.digitalasset.jsonapi.events.CreatedEvent;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import java.io.IOException;

class CreatedEventDeserializer extends StdDeserializer<CreatedEvent> {

  protected CreatedEventDeserializer(Class<?> vc) {
    super(vc);
  }

  @Override
  public CreatedEvent deserialize(
      JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException {
    JsonNode o = jsonParser.readValueAsTree();
    Identifier identifier = readNodeAs(jsonParser, o.path("templateId"), Identifier.class);
    String contractId = o.path("contractId").textValue();
    String templateType = ClassName.from(identifier);
    try {
      Template template = readNodeAs(jsonParser, o.path("payload"), Class.forName(templateType));
      return new CreatedEvent(identifier, contractId, template);
    } catch (ClassNotFoundException e) {
      throw new IllegalStateException(e.getMessage(), e);
    }
  }
}
