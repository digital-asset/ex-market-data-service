/*
 * Copyright (c) 2019, Digital Asset (Switzerland) GmbH and/or its affiliates. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package com.digitalasset.refapps.marketdataservice.publishing;

import static org.junit.Assert.assertEquals;

import java.util.Collection;
import org.junit.Test;

public class CsvParserTest {

  @Test
  public void parserHandlesActualNewLines() {
    String csvWithNewLines = "2019-11-12T11:30:30.00Z,12.34\n2019-11-12T13:30:30.00Z,23.45\n";

    Collection<ObservationTimeWithValue> result = CsvParser.parseData(csvWithNewLines);

    assertEquals(2, result.size());
  }

  @Test
  public void parserHandlesEscapedNewLines() {
    String csvWithEscapes = "2019-11-12T11:30:30.00Z,12.34\\n2019-11-12T13:30:30.00Z,23.45\\n";

    Collection<ObservationTimeWithValue> result = CsvParser.parseData(csvWithEscapes);

    assertEquals(2, result.size());
  }
}
