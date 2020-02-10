/*
 * Copyright (c) 2019, Digital Asset (Switzerland) GmbH and/or its affiliates. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package jsonapi.gson;

import com.daml.ledger.javaapi.data.ExerciseCommand;
import com.daml.ledger.javaapi.data.Identifier;
import com.daml.ledger.javaapi.data.Record;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.time.Instant;
import jsonapi.json.JsonSerializer;

// TODO: Rename
public class SampleJsonSerializer implements JsonSerializer {

  private final Gson gson =
      new GsonBuilder()
          .registerTypeAdapter(Identifier.class, new IdentifierSerializer())
          .registerTypeAdapter(Instant.class, new InstantSerializer())
          .registerTypeAdapter(Record.class, new RecordSerializer())
          .registerTypeAdapter(ExerciseCommand.class, new ExerciseCommandSerializer())
          .create();

  @Override
  public String apply(Object o) {
    return gson.toJson(o);
  }
}
