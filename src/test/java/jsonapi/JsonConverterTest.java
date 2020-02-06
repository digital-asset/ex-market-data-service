/*
 * Copyright (c) 2019, Digital Asset (Switzerland) GmbH and/or its affiliates. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package jsonapi;

import com.google.gson.*;
import da.timeservice.timeservice.CurrentTime;
import org.junit.Test;
import java.lang.reflect.Type;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;
import static org.junit.Assert.assertEquals;


// TODO migrate to interface we agreed on
class CurrentTimeJsonConverter {

  private Gson gson;

  public CurrentTimeJsonConverter() {
    GsonBuilder gsonBuilder = new GsonBuilder();
    gsonBuilder.registerTypeAdapter(Instant.class, new CurrentTimeJsonConverter.InstantJsonConverter());
    gson = gsonBuilder.create();
  }

  CurrentTime fromResponseWithSingletonList(String responseWithMany) {
    Map<?, ArrayList<Map<?, ?>>> m1 = gson.fromJson(responseWithMany, Map.class);
    Object x1 = m1.get("result").get(0).get("payload");
    return gson.fromJson(gson.toJson(x1), CurrentTime.class);
  }

  String to(CurrentTime currentTime) {
    return gson.toJson(currentTime);
  }

  private static class InstantJsonConverter implements JsonSerializer<Instant>, JsonDeserializer<Instant> {
    public JsonElement serialize(
            Instant instant, Type typeOfSrc, JsonSerializationContext context) {
      return new JsonPrimitive(instant.toString());
    }

    public Instant deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
            throws JsonParseException {
      return Instant.parse(json.getAsJsonPrimitive().getAsString());
    }
  }

}

public class JsonConverterTest {

  @Test
  public void jsonParseFromSingletonList() {
    String responseWithSingletonList =
            "{\"result\":[{\"observers\":[],\"agreementText\":\"\",\"payload\":{\"operator\":\"Operator\",\"currentTime\":\"2020-02-04T22:57:29Z\",\"observers\":[]},\"signatories\":[\"Operator\"],\"key\":\"Operator\",\"contractId\":\"#0:0\",\"templateId\":\"6f14cd82bbdbf637ae067f60af1d8da0b941de2e44f4b97b12e9fe7b5f13147a:DA.TimeService.TimeService:CurrentTime\"}],\"status\":200}";
    CurrentTimeJsonConverter jsonConverter = new CurrentTimeJsonConverter();
    CurrentTime currentTime = jsonConverter.fromResponseWithSingletonList(responseWithSingletonList);
    assertEquals(currentTime, new CurrentTime("Operator", Instant.parse("2020-02-04T22:57:29Z"), Collections.EMPTY_LIST));
    assertEquals(jsonConverter.to(currentTime), "{\"operator\":\"Operator\",\"currentTime\":\"2020-02-04T22:57:29Z\",\"observers\":[]}");
  }
}
