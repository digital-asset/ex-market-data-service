/*
 * Copyright (c) 2019, Digital Asset (Switzerland) GmbH and/or its affiliates. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package jsonapi.events;

import java.util.Set;
import jsonapi.ActiveContract;

public class ArchivedEvent implements Event {

  private final String archived;

  public ArchivedEvent(String archived) {
    this.archived = archived;
  }

  @Override
  public void addOrRemove(Set<ActiveContract> activeContracts) {
    activeContracts.remove(new ActiveContract(null, archived, null));
  }
}
