/*
 * Copyright (c) 2019, Digital Asset (Switzerland) GmbH and/or its affiliates. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package com.digitalasset.jsonapi.genson;

import com.daml.ledger.javaapi.data.Identifier;
import com.daml.ledger.javaapi.data.Template;
import com.digitalasset.jsonapi.ClassName;
import com.digitalasset.jsonapi.events.CreatedEvent;
import com.owlike.genson.Context;
import com.owlike.genson.Deserializer;
import com.owlike.genson.stream.ObjectReader;

// TODO: This is far from being done. Could not find a way to do this.
class CreatedEventDeserializer implements Deserializer<CreatedEvent> {

  @Override
  public CreatedEvent deserialize(ObjectReader objectReader, Context context) {
    // Assuming for simplicity that properties exist and parsing them happens in the order below.
    // This is against the documentation:
    // http://genson.io/Documentation/Javadoc/com/owlike/genson/stream/ObjectReader.html

    ObjectReader o = objectReader.beginObject();

    o.skipValue(); // Skip 'payload' for now.

    o.next();
    String contractId = o.valueAsString();

    o.next();
    String templateId = String.format("\"%s\"", o.valueAsString());
    Identifier identifier = context.genson.deserialize(templateId, Identifier.class);

    o.endObject();

    // Have identifier, can deal with 'payload'.
    o = objectReader.beginObject();
    o.next();
    String templateType = ClassName.from(identifier);
    try {
      Class<?> toClass = Class.forName(templateType);
      Template template = (Template) context.genson.deserialize(o.valueAsString(), toClass);
      return new CreatedEvent(identifier, contractId, template);
    } catch (ClassNotFoundException e) {
      throw new IllegalStateException(e.getMessage(), e);
    }
  }
}
