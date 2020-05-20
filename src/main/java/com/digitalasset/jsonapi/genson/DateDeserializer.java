/*
 * Copyright (c) 2019, Digital Asset (Switzerland) GmbH and/or its affiliates. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */

package com.digitalasset.jsonapi.genson;

import com.daml.ledger.javaapi.data.Date;
import com.owlike.genson.Context;
import com.owlike.genson.Deserializer;
import com.owlike.genson.stream.ObjectReader;
import java.time.LocalDate;

public class DateDeserializer implements Deserializer<Date> {
  @Override
  public Date deserialize(ObjectReader objectReader, Context context) {
    LocalDate date = LocalDate.parse(objectReader.valueAsString());
    return new Date((int) date.toEpochDay());
  }
}
