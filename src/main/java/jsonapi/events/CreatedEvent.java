/*
 * Copyright (c) 2019, Digital Asset (Switzerland) GmbH and/or its affiliates. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package jsonapi.events;

import com.daml.ledger.javaapi.data.Identifier;
import com.daml.ledger.javaapi.data.Template;
import java.util.Objects;
import jsonapi.ActiveContract;
import jsonapi.ActiveContractSet;

public class CreatedEvent implements Event {

  private final Identifier templateId;
  private final String contractId;
  private final Template payload;

  public CreatedEvent(Identifier templateId, String contractId, Template payload) {
    this.templateId = templateId;
    this.contractId = contractId;
    this.payload = payload;
  }

  public Identifier getTemplateId() {
    return templateId;
  }

  public String getContractId() {
    return contractId;
  }

  public Template getPayload() {
    return payload;
  }

  @Override
  public ActiveContractSet update(ActiveContractSet activeContractSet) {
    ActiveContract activeContract = toActiveContract();
    return activeContractSet.add(activeContract);
  }

  private ActiveContract toActiveContract() {
    return new ActiveContract(getTemplateId(), getContractId(), getPayload());
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
    return Objects.equals(getTemplateId(), that.getTemplateId())
        && Objects.equals(getContractId(), that.getContractId())
        && Objects.equals(getPayload(), that.getPayload());
  }

  @Override
  public int hashCode() {
    return Objects.hash(getTemplateId(), getContractId(), getPayload());
  }
}
