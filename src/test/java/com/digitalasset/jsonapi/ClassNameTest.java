/*
 * Copyright (c) 2019, Digital Asset (Switzerland) GmbH and/or its affiliates. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package com.digitalasset.jsonapi;

import static org.junit.Assert.assertEquals;

import com.daml.ledger.javaapi.data.Identifier;
import org.junit.Test;

public class ClassNameTest {

  @Test
  public void concatenatesModuleAndEntityNames() {
    Identifier identifier = new Identifier(null, "foo", "bar");
    String className = ClassName.from(identifier);
    assertEquals("foo.bar", className);
  }

  @Test
  public void convertsModuleNameToLowerCase() {
    Identifier identifier = new Identifier(null, "Bar", "bar");
    String className = ClassName.from(identifier);
    assertEquals("bar.bar", className);
  }

  @Test
  public void ignoresPackageId() {
    Identifier identifier = new Identifier("packageId", "foo", "bar");
    String className = ClassName.from(identifier);
    assertEquals("foo.bar", className);
  }

  @Test
  public void keepsEntityNameAsIs() {
    Identifier identifier = new Identifier("packageId", "foo", "BAR");
    String className = ClassName.from(identifier);
    assertEquals("foo.BAR", className);
  }
}
