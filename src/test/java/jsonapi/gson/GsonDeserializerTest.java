/*
 * Copyright (c) 2019, Digital Asset (Switzerland) GmbH and/or its affiliates. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package jsonapi.gson;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import com.google.gson.Gson;
import da.refapps.marketdataservice.datasource.DataSource;
import da.refapps.marketdataservice.marketdatatypes.InstrumentId;
import da.refapps.marketdataservice.marketdatatypes.ObservationReference;
import java.time.LocalDate;
import java.util.Collections;
import org.junit.Test;

public class GsonDeserializerTest {

  @Test
  public void deserializeDataStream() {
    String json =
        "{\n"
            + "  \"observers\": [],\n"
            + "  \"reference\": {\n"
            + "    \"market\": \"European Bond Market\",\n"
            + "    \"instrumentId\": {\n"
            + "      \"unpack\": \"ISIN 123 1244\"\n"
            + "    },\n"
            + "    \"maturityDate\": \"2021-03-20\"\n"
            + "  },\n"
            + "  \"path\": \"default-1000.csv\",\n"
            + "  \"operator\": \"Operator\",\n"
            + "  \"owner\": \"MarketDataProvider2\"\n"
            + "}";

    Gson deserializer = GsonRegisteredAllDeserializers.gson();
    DataSource result = deserializer.fromJson(json, DataSource.class);

    assertThat(
        result,
        is(
            new DataSource(
                "MarketDataProvider2",
                "Operator",
                Collections.emptyList(),
                new ObservationReference(
                    "European Bond Market",
                    new InstrumentId("ISIN 123 1244"),
                    LocalDate.of(2021, 3, 20)),
                "default-1000.csv")));
  }
}
