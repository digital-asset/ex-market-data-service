/*
 * Copyright (c) 2019, Digital Asset (Switzerland) GmbH and/or its affiliates. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package com.daml.extensions.jsonapi.http;

import com.daml.extensions.jsonapi.events.CreatedEvent;

public class CreatedEventHolder implements EventHolder {
  private final CreatedEvent created;

  public CreatedEventHolder(CreatedEvent created) {
    this.created = created;
  }

  @Override
  public CreatedEvent event() {
    return created;
  }
}
