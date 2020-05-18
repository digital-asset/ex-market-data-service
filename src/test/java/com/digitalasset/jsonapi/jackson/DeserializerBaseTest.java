/*
 * Copyright (c) 2019, Digital Asset (Switzerland) GmbH and/or its affiliates. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package com.digitalasset.jsonapi.jackson;

import com.fasterxml.jackson.databind.ObjectMapper;

public abstract class DeserializerBaseTest<T> {

  protected abstract Class<T> getDeserializeClass();

  protected abstract ObjectMapper getClassDeserializer();
}
