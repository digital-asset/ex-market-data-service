/*
 * Copyright (c) 2019, Digital Asset (Switzerland) GmbH and/or its affiliates. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package com.daml.extensions.jsonapi.gson;

import static org.junit.Assert.*;

import com.google.gson.Gson;
import com.google.gson.JsonDeserializer;
import da.refapps.marketdataservice.marketdatatypes.ObservationValue;
import da.refapps.marketdataservice.marketdatatypes.observationvalue.CleanPrice;
import java.math.BigDecimal;
import org.junit.Test;

public class ObservationValueDeserializerTest extends DeserializerBaseTest<ObservationValue> {

  @Test
  public void deserialize() {
    String json =
        "{ \n"
            + "   \"tag\":\"CleanPrice\",\n"
            + "   \"value\":{ \n"
            + "      \"clean\":\"1\"\n"
            + "   }\n"
            + "}";
    Gson deserializer = createDeserializer();
    assertEquals(
        new CleanPrice(BigDecimal.ONE), deserializer.fromJson(json, getDeserializedClass()));
  }

  @Override
  protected Class<ObservationValue> getDeserializedClass() {
    return ObservationValue.class;
  }

  @Override
  protected JsonDeserializer<ObservationValue> getClassDeserializer() {
    return new ObservationValueDeserializer();
  }
}
