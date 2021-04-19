/*
 * Copyright (c) 2019, Digital Asset (Switzerland) GmbH and/or its affiliates. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package com.daml.extensions.jsonapi.events;

import com.daml.extensions.jsonapi.ActiveContractSet;

public class ArchivedEvent implements Event {

  private final String contractId;

  public ArchivedEvent(String contractId) {
    this.contractId = contractId;
  }

  public String getContractId() {
    return contractId;
  }

  @Override
  public ActiveContractSet update(ActiveContractSet activeContractSet) {
    return activeContractSet.remove(getContractId());
  }
}
