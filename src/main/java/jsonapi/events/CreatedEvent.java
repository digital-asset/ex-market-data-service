/*
 * Copyright (c) 2019, Digital Asset (Switzerland) GmbH and/or its affiliates. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package jsonapi.events;

import com.daml.ledger.javaapi.data.Identifier;
import com.daml.ledger.javaapi.data.Template;
import java.util.Set;
import jsonapi.ActiveContract;

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
  public void addOrRemove(Set<ActiveContract> activeContracts) {
    activeContracts.add(new ActiveContract(templateId, contractId, payload));
  }
}
