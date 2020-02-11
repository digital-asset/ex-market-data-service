/*
 * Copyright (c) 2019, Digital Asset (Switzerland) GmbH and/or its affiliates. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package jsonapi.http;

import jsonapi.events.CreatedEvent;

import java.util.Collection;

@SuppressWarnings("PMD.DataClass")
public class HttpResponse {

  private final int status;
  private final Collection<CreatedEvent> result;
  private final Object errors;
  private final Object warnings;

  public HttpResponse(int status, Collection<CreatedEvent> result, Object errors, Object warnings) {
    this.status = status;
    this.result = result;
    this.errors = errors;
    this.warnings = warnings;
  }

  public int getStatus() {
    return status;
  }

  public Collection<CreatedEvent> getResult() {
    return result;
  }

  public Object getErrors() {
    return errors;
  }

  public Object getWarnings() {
    return warnings;
  }
}
