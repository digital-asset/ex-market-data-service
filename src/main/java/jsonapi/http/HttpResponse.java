/*
 * Copyright (c) 2019, Digital Asset (Switzerland) GmbH and/or its affiliates. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package jsonapi.http;

import java.util.Collection;
import jsonapi.events.CreatedEvent;

@SuppressWarnings("PMD.DataClass")
public class HttpResponse {

  public interface Result {}

  public static class CreateResult implements Result {
    private final CreatedEvent createdEvent;

    public CreateResult(CreatedEvent createdEvent) {
      this.createdEvent = createdEvent;
    }

    public CreatedEvent getCreatedEvent() {
      return createdEvent;
    }
  }

  public static class SearchResult implements Result {
    private final Collection<CreatedEvent> createdEvents;

    public SearchResult(Collection<CreatedEvent> createdEvents) {
      this.createdEvents = createdEvents;
    }

    public Collection<CreatedEvent> getCreatedEvents() {
      return createdEvents;
    }
  }

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
