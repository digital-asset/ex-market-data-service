/*
 * Copyright (c) 2019, Digital Asset (Switzerland) GmbH and/or its affiliates. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package jsonapi;

import com.daml.ledger.javaapi.data.Identifier;
import java.util.Collection;

public class ContractQuery {

  private final Collection<Identifier> templateIds;

  public ContractQuery(Collection<Identifier> identifiers) {
    templateIds = identifiers;
  }

  public Collection<Identifier> getTemplateIds() {
    return templateIds;
  }
}
