/*
 * Copyright (c) 2019, Digital Asset (Switzerland) GmbH and/or its affiliates. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package jsonapi.http;

import java.util.Collection;

@SuppressWarnings("PMD.DataClass")
public class HttpResponse {

  public interface Result {}

  // TODO make Result an interface with several implementations: ExerciseResult, SearchResult etc
  public static class ExerciseResult implements Result {
    private final String exerciseResult;
    private final Collection<EventHolder> events;

    public ExerciseResult(String exerciseResult, Collection<EventHolder> events) {
      this.exerciseResult = exerciseResult;
      this.events = events;
    }

    public String getExerciseResult() {
      return exerciseResult;
    }

    public Collection<EventHolder> getEvents() {
      return events;
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
