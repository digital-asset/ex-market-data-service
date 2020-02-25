/*
 * Copyright (c) 2019, Digital Asset (Switzerland) GmbH and/or its affiliates. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package com.digitalasset.refapps.marketdataservice.assertions;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Optional;

public class Assert {

  @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
  public static <T> void assertOptionalValue(T expectedValue, Optional<T> actual) {
    assertTrue(actual.isPresent());
    assertEquals(expectedValue, actual.get());
  }

  @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
  public static <T> void assertEmpty(Optional<T> result) {
    assertFalse(result.isPresent());
  }
}
