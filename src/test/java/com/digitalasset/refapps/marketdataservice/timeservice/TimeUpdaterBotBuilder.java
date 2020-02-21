/*
 * Copyright (c) 2019, Digital Asset (Switzerland) GmbH and/or its affiliates. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package com.digitalasset.refapps.marketdataservice.timeservice;

import com.digitalasset.refapps.marketdataservice.JsonLedgerApiHandle;
import com.digitalasset.refapps.marketdataservice.LedgerApiHandle;
import jsonapi.LedgerClient;

public class TimeUpdaterBotBuilder {

  private LedgerClient ledgerClient;
  private String party;

  public TimeUpdaterBotBuilder setLedgerClient(LedgerClient ledgerClient) {
    this.ledgerClient = ledgerClient;
    return this;
  }

  public TimeUpdaterBotBuilder setParty(String party) {
    this.party = party;
    return this;
  }

  public TimeUpdaterBot build() {
    LedgerApiHandle ledgerApiHandle = new JsonLedgerApiHandle(ledgerClient, party);
    return new TimeUpdaterBot(ledgerApiHandle);
  }
}
