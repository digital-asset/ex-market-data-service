/*
 * Copyright (c) 2019, Digital Asset (Switzerland) GmbH and/or its affiliates. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package com.daml.extensions.jsonapi.events;

import com.daml.ledger.javaapi.data.Identifier;
import com.daml.ledger.javaapi.data.Template;
import com.daml.extensions.jsonapi.ActiveContract;
import com.daml.extensions.jsonapi.ActiveContractSet;
import java.util.Objects;

public class CreatedEvent implements Event {

  private final Identifier templateId;
  private final String contractId;
  private final Template payload;

  public CreatedEvent(Identifier templateId, String contractId, Template payload) {
    this.templateId = templateId;
    this.contractId = contractId;
    this.payload = payload;
  }

  @Override
  public ActiveContractSet update(ActiveContractSet activeContractSet) {
    ActiveContract activeContract = toActiveContract();
    return activeContractSet.add(activeContract);
  }

  private ActiveContract toActiveContract() {
    return new ActiveContract(templateId, contractId, payload);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    CreatedEvent that = (CreatedEvent) o;
    return Objects.equals(templateId, that.templateId)
        && Objects.equals(contractId, that.contractId)
        && Objects.equals(payload, that.payload);
  }

  @Override
  public int hashCode() {
    return Objects.hash(templateId, contractId, payload);
  }
}
