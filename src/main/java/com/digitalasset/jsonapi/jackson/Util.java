/*
 * Copyright (c) 2019, Digital Asset (Switzerland) GmbH and/or its affiliates. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */

package com.digitalasset.jsonapi.jackson;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.JsonNode;
import java.io.IOException;

public class Util {
  @SuppressWarnings("unchecked")
  static <T> T readNodeAs(JsonParser jsonParser, JsonNode node, Class<?> type) throws IOException {
    return (T) node.traverse(jsonParser.getCodec()).readValueAs(type);
  }
}
