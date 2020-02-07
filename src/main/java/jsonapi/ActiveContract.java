/*
 * Copyright (c) 2019, Digital Asset (Switzerland) GmbH and/or its affiliates. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package jsonapi;

import com.daml.ledger.javaapi.data.Identifier;
import com.daml.ledger.javaapi.data.Template;

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
}
