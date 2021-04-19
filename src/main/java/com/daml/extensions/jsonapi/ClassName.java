/*
 * Copyright (c) 2019, Digital Asset (Switzerland) GmbH and/or its affiliates. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package com.daml.extensions.jsonapi;

import com.daml.ledger.javaapi.data.Identifier;

public class ClassName {

  public static String from(Identifier identifier) {
    String moduleName = identifier.getModuleName().toLowerCase();
    String entityName = identifier.getEntityName();
    return String.format("%s.%s", moduleName, entityName);
  }
}
