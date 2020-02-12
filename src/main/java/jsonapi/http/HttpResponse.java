/*
 * Copyright (c) 2019, Digital Asset (Switzerland) GmbH and/or its affiliates. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package jsonapi.http;

import java.util.Collection;
import jsonapi.events.Event;

@SuppressWarnings("PMD.DataClass")
public class HttpResponse {

  // TODO make Result an interface with 2 implementations: one for Exercise, one for Search
  public static class ExerciseResult {
    private final String exerciseResult;
    private final Collection<Event> contracts;

    public ExerciseResult(String exerciseResult, Collection<Event> contracts) {
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
  private final ExerciseResult result;
  private final Object errors;
  private final Object warnings;

  public HttpResponse(int status, ExerciseResult result, Object errors, Object warnings) {
    this.status = status;
    this.result = result;
    this.errors = errors;
    this.warnings = warnings;
  }

  public int getStatus() {
    return status;
  }

  public ExerciseResult getResult() {
    return result;
  }

  public Object getErrors() {
    return errors;
  }

  public Object getWarnings() {
    return warnings;
  }
}
