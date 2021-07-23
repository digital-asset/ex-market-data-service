/*
 * Copyright (c) 2019, Digital Asset (Switzerland) GmbH and/or its affiliates. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */

package com.daml.extensions.jsonapi.gson;

import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import com.daml.ledger.javaapi.data.CreateCommand;
import com.daml.ledger.javaapi.data.DamlList;
import com.daml.ledger.javaapi.data.DamlRecord;
import com.daml.ledger.javaapi.data.Identifier;
import com.daml.ledger.javaapi.data.Party;
import com.daml.ledger.javaapi.data.Timestamp;
import com.google.gson.Gson;
import com.google.gson.JsonSerializer;
import da.timeservice.timeservice.CurrentTime;
import java.time.Instant;
import java.util.Collections;
import org.junit.Test;

public class CreateCommandSerializerTest extends SerializerBaseTest<CreateCommand> {

  @Test
  public void serializeCreateCommand() {
    Instant time = Instant.parse("2020-02-08T12:30:00Z");
    CurrentTime currentTime =
        new CurrentTime("JohnDoe", time, Collections.singletonList("JaneDoe"));
    CreateCommand createCommand = currentTime.create();
    registerSerializer(Party.class, new PartySerializer());
    registerSerializer(Timestamp.class, new TimestampSerializer());
    registerSerializer(Identifier.class, new IdentifierSerializer());
    registerSerializer(DamlRecord.class, new RecordSerializer());
    registerSerializer(DamlList.class, new DamlListSerializer());

    Gson serializer = createSerializer();

    String json = serializer.toJson(createCommand);
    String templateIdPattern =
        ".*\"templateId\":\"[a-zA-Z0-9]{64}:DA\\.TimeService\\.TimeService:CurrentTime\".*";
    assertTrue(json.matches(templateIdPattern));
    assertThat(
        json,
        containsString(
            "\"payload\":{\"operator\":\"JohnDoe\",\"currentTime\":\"2020-02-08T12:30:00Z\",\"observers\":[\"JaneDoe\"]}}"));
  }

  @Override
  protected Class<CreateCommand> getSerializedClass() {
    return CreateCommand.class;
  }

  @Override
  protected JsonSerializer<CreateCommand> getClassSerializer() {
    return new CreateCommandSerializer();
  }
}
