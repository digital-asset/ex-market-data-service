/*
 * Copyright (c) 2019, Digital Asset (Switzerland) GmbH and/or its affiliates. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package com.daml.extensions.jsonapi.gson;

import static org.junit.Assert.assertEquals;

import com.daml.ledger.javaapi.data.Text;
import com.google.gson.Gson;
import com.google.gson.JsonSerializer;
import org.junit.Test;

public class TextSerializerTest extends SerializerBaseTest<Text> {

  @Test
  public void serializeText() {
    Text text = new Text("apple");

    Gson serializer = createSerializer();

    assertEquals("\"apple\"", serializer.toJson(text));
  }

  @Override
  protected Class<Text> getSerializedClass() {
    return Text.class;
  }

  @Override
  protected JsonSerializer<Text> getClassSerializer() {
    return new TextSerializer();
  }
}
