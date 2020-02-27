/*
 * Copyright (c) 2019, Digital Asset (Switzerland) GmbH and/or its affiliates. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package com.digitalasset.jsonapi;

import com.daml.ledger.javaapi.data.Identifier;
import java.util.Collection;
import java.util.Objects;

public class ContractQuery {

  private final Collection<Identifier> templateIds;

  public ContractQuery(Collection<Identifier> identifiers) {
    templateIds = identifiers;
  }

  public Collection<Identifier> getTemplateIds() {
    return templateIds;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ContractQuery that = (ContractQuery) o;
    return collectionsEquals(getTemplateIds(), that.getTemplateIds());
  }

  private <T> boolean collectionsEquals(Collection<T> left, Collection<T> right) {
    if (left == right) {
      return true;
    } else if (left == null || right == null) {
      return false;
    } else {
      return left.containsAll(right) && right.containsAll(left);
    }
  }

  @Override
  public int hashCode() {
    return Objects.hash(getTemplateIds());
  }
}
