/*
 * Copyright (c) 2019, Digital Asset (Switzerland) GmbH and/or its affiliates. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package com.daml.product.refapps.marketdataservice.utils;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class AppParties {
  private static final String MARKET_DATA_PROVIDER_1 = "MarketDataProvider1";
  private static final String MARKET_DATA_PROVIDER_2 = "MarketDataProvider2";
  private static final String OPERATOR = "Operator";

  public static final String[] ALL_PARTIES =
      new String[] {MARKET_DATA_PROVIDER_1, MARKET_DATA_PROVIDER_2, OPERATOR};
  private final Set<String> parties;

  public AppParties(String[] parties) {
    this.parties = new HashSet<>(Arrays.asList(parties));
  }

  public boolean hasMarketDataProvider1() {
    return parties.contains(MARKET_DATA_PROVIDER_1);
  }

  public String getMarketDataProvider1() {
    return MARKET_DATA_PROVIDER_1;
  }

  public boolean hasMarketDataProvider2() {
    return parties.contains(MARKET_DATA_PROVIDER_2);
  }

  public String getMarketDataProvider2() {
    return MARKET_DATA_PROVIDER_2;
  }

  public boolean hasOperator() {
    return parties.contains(OPERATOR);
  }

  public String getOperator() {
    return OPERATOR;
  }
}
