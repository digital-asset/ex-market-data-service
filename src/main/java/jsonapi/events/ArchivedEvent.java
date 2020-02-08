/*
 * Copyright (c) 2019, Digital Asset (Switzerland) GmbH and/or its affiliates. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package jsonapi.events;

import jsonapi.ActiveContractSet;

public class ArchivedEvent implements Event {

  private final String archived;

  public ArchivedEvent(String archived) {
    this.archived = archived;
  }

  public String getContractId() {
    return archived;
  }

  @Override
  public ActiveContractSet update(ActiveContractSet activeContractSet) {
    return activeContractSet.remove(getContractId());
  }
}
