/*
 * Copyright (c) 2019, Digital Asset (Switzerland) GmbH and/or its affiliates. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package com.digitalasset.jsonapi.genson;

import com.daml.ledger.javaapi.data.Identifier;
import com.owlike.genson.Context;
import com.owlike.genson.Deserializer;
import com.owlike.genson.stream.ObjectReader;

class IdentifierDeserializer implements Deserializer<Identifier> {

  @Override
  public Identifier deserialize(ObjectReader objectReader, Context context) {
    String value = objectReader.valueAsString();
    String[] parts = value.split(":");
    return new Identifier(parts[0], parts[1], parts[2]);
  }
}
