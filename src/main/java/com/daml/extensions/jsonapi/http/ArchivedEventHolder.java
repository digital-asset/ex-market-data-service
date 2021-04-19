/*
 * Copyright (c) 2019, Digital Asset (Switzerland) GmbH and/or its affiliates. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package com.daml.extensions.jsonapi.http;

import com.daml.extensions.jsonapi.events.ArchivedEvent;

public class ArchivedEventHolder implements EventHolder {
  private final ArchivedEvent archived;

  public ArchivedEventHolder(ArchivedEvent archived) {
    this.archived = archived;
  }

  @Override
  public ArchivedEvent event() {
    return archived;
  }
}
