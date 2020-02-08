/*
 * Copyright (c) 2019, Digital Asset (Switzerland) GmbH and/or its affiliates. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package jsonapi;

import com.daml.ledger.javaapi.data.Identifier;
import com.daml.ledger.javaapi.data.Template;
import java.util.Objects;

@SuppressWarnings("PMD")
public class ActiveContract {

  private final Identifier identifier;
  private final String contractId;
  private final Template template;

  public ActiveContract(Identifier identifier, String contractId, Template template) {
    this.identifier = identifier;
    this.contractId = contractId;
    this.template = template;
  }

  public Identifier getIdentifier() {
    return identifier;
  }

  public String getContractId() {
    return contractId;
  }

  public Template getTemplate() {
    return template;
  }

  @Override
  public String toString() {
    return "ActiveContract{"
        + "identifier="
        + identifier
        + ", contractId='"
        + contractId
        + '\''
        + ", template="
        + template
        + '}';
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ActiveContract that = (ActiveContract) o;
    return getIdentifier().equals(that.getIdentifier())
        && getContractId().equals(that.getContractId())
        && getTemplate().equals(that.getTemplate());
  }

  @Override
  public int hashCode() {
    return Objects.hash(getIdentifier(), getContractId(), getTemplate());
  }
}
