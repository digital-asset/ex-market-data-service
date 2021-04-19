/*
 * Copyright (c) 2019, Digital Asset (Switzerland) GmbH and/or its affiliates. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package com.daml.product.refapps.marketdataservice.publishing;

import da.refapps.marketdataservice.marketdatatypes.ObservationValue;
import da.refapps.marketdataservice.marketdatatypes.observationvalue.CleanPrice;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Scanner;

public class CsvParser {

  private static final String COMMA = ",";

  static Collection<ObservationTimeWithValue> parseData(String content) {
    final Collection<ObservationTimeWithValue> result = new ArrayList<>();
    String unescaped = content.replaceAll("\\\\n", "\n");
    try (Scanner reader = new Scanner(unescaped)) {
      while (reader.hasNextLine()) {
        String line = reader.nextLine();
        String[] timeAndValue = line.split(COMMA);
        if (timeAndValue.length < 2) {
          throw new IllegalArgumentException(
              String.format(
                  "Malformed CSV with line '%s' - it has %d fields instead of 2.",
                  line, timeAndValue.length));
        }

        // Format: 2007-12-03T10:15:30.00Z
        Instant time = Instant.parse(timeAndValue[0].trim());
        // Format: a number (integer of decimal) in string format
        ObservationValue value = new CleanPrice(new BigDecimal(timeAndValue[1].trim()));
        result.add(new ObservationTimeWithValue(time, value));
      }
    }
    return result;
  }
}
