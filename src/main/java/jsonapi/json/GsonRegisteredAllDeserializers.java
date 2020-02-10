/*
 * Copyright (c) 2019, Digital Asset (Switzerland) GmbH and/or its affiliates. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package jsonapi.json;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.time.Instant;

import da.refapps.marketdataservice.marketdatatypes.ObservationValue;
import jsonapi.gson.InstantDeserializer;
import jsonapi.gson.ObservationValueDeserializer;

public class GsonRegisteredAllDeserializers {

  public static Gson gson() {
    return new GsonBuilder().registerTypeAdapter(Instant.class, new InstantDeserializer()).
            registerTypeAdapter(ObservationValue.class, new ObservationValueDeserializer()).create();
  }
}
