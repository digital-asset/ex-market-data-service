/*
 * Copyright (c) 2019, Digital Asset (Switzerland) GmbH and/or its affiliates. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package jsonapi.json;

import com.daml.ledger.javaapi.data.*;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.time.Instant;
import jsonapi.gson.*;

public class SampleJsonSerializer implements JsonSerializer {

  private final Gson json =
      new GsonBuilder()
          .registerTypeAdapter(Identifier.class, new IdentifierSerializer())
          .registerTypeAdapter(Instant.class, new InstantSerializer())
          .registerTypeAdapter(Record.class, new RecordSerializer())
          .registerTypeAdapter(Party.class, new PartySerializer())
          .registerTypeAdapter(Text.class, new TextSerializer())
          .registerTypeAdapter(Date.class, new DateSerializer())
          .registerTypeAdapter(Timestamp.class, new TimestampSerializer())
          .registerTypeAdapter(Variant.class, new VariantSerializer())
          .registerTypeAdapter(ExerciseCommand.class, new ExerciseCommandSerializer())
          .create();

  @Override
  public String apply(Object o) {
    return json.toJson(o);
  }
}
