/*
 * Copyright (c) 2019, Digital Asset (Switzerland) GmbH and/or its affiliates. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package jsonapi.http;

import java.util.Collection;
import jsonapi.events.Event;

@SuppressWarnings("PMD.DataClass")
public class HttpResponse {

  public static class Result {
    private final String exerciseResult;
    private final Collection<Event> contracts;

    public Result(String exerciseResult, Collection<Event> contracts) {
      this.exerciseResult = exerciseResult;
      this.contracts = contracts;
    }

    public String getExerciseResult() {
      return exerciseResult;
    }

    public Collection<Event> getContracts() {
      return contracts;
    }
  }

  private final int status;
  private final Result result;
  private final Object errors;
  private final Object warnings;

  public HttpResponse(int status, Result result, Object errors, Object warnings) {
    this.status = status;
    this.result = result;
    this.errors = errors;
    this.warnings = warnings;
  }

  public int getStatus() {
    return status;
  }

  public Result getResult() {
    return result;
  }

  public Object getErrors() {
    return errors;
  }

  public Object getWarnings() {
    return warnings;
  }
}
