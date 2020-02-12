/*
 * Copyright (c) 2019, Digital Asset (Switzerland) GmbH and/or its affiliates. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package jsonapi.http;

import jsonapi.events.ArchivedEvent;

public class ArchivedEventHolder implements EventHolder {
  private final ArchivedEvent archived;

  public ArchivedEventHolder(ArchivedEvent archived) {
    this.archived = archived;
  }

  public ArchivedEvent getArchived() {
    return archived;
  }
}
