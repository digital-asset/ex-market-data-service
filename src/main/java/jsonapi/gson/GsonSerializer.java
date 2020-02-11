/*
 * Copyright (c) 2019, Digital Asset (Switzerland) GmbH and/or its affiliates. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package jsonapi.gson;

import com.daml.ledger.javaapi.data.Date;
import com.daml.ledger.javaapi.data.ExerciseCommand;
import com.daml.ledger.javaapi.data.Identifier;
import com.daml.ledger.javaapi.data.Numeric;
import com.daml.ledger.javaapi.data.Party;
import com.daml.ledger.javaapi.data.Record;
import com.daml.ledger.javaapi.data.Text;
import com.daml.ledger.javaapi.data.Timestamp;
import com.daml.ledger.javaapi.data.Variant;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.time.Instant;
import jsonapi.json.JsonSerializer;

public class GsonSerializer implements JsonSerializer {

  private final Gson gson =
      new GsonBuilder()
          .registerTypeAdapter(Identifier.class, new IdentifierSerializer())
          .registerTypeAdapter(Instant.class, new InstantSerializer())
          .registerTypeAdapter(Record.class, new RecordSerializer())
          .registerTypeAdapter(Party.class, new PartySerializer())
          .registerTypeAdapter(Text.class, new TextSerializer())
          .registerTypeAdapter(Date.class, new DateSerializer())
          .registerTypeAdapter(Timestamp.class, new TimestampSerializer())
          .registerTypeAdapter(Variant.class, new VariantSerializer())
          .registerTypeAdapter(Numeric.class, new NumericSerializer())
          .registerTypeAdapter(ExerciseCommand.class, new ExerciseCommandSerializer())
          .create();

  @Override
  public String apply(Object o) {
    return gson.toJson(o);
  }
}
