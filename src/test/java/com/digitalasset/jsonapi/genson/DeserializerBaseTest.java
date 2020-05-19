/*
 * Copyright (c) 2019, Digital Asset (Switzerland) GmbH and/or its affiliates. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package com.digitalasset.jsonapi.genson;

import com.owlike.genson.Genson;

public abstract class DeserializerBaseTest<T> {

  protected abstract Class<T> getDeserializeClass();

  protected abstract Genson getClassDeserializer();
}
