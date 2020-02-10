/*
 * Copyright (c) 2019, Digital Asset (Switzerland) GmbH and/or its affiliates. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package jsonapi.gson;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import da.refapps.marketdataservice.marketdatatypes.ObservationValue;
import da.refapps.marketdataservice.marketdatatypes.observationvalue.CleanPrice;
import java.lang.reflect.Type;

public class ObservationValueDeserializer implements JsonDeserializer<ObservationValue> {

  @Override
  public ObservationValue deserialize(
      JsonElement json, Type typeOfT, JsonDeserializationContext context)
      throws JsonParseException {
    // We do not properly deserialize the class hierarchy, we only want a MWE.
    // A possible way to do this properly: https://stackoverflow.com/a/22081826
    return context.deserialize(json, CleanPrice.class);
  }
}
