/*
 * Copyright (c) 2019, Digital Asset (Switzerland) GmbH and/or its affiliates. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package com.digitalasset.refapps.marketdataservice.timeservice;

import jsonapi.JsonLedgerClient;

public class TimeUpdaterBotBuilder {

  private JsonLedgerClient jsonLedgerClient;
  private String party;

  public TimeUpdaterBotBuilder setJsonLedgerClient(JsonLedgerClient jsonLedgerClient) {
    this.jsonLedgerClient = jsonLedgerClient;
    return this;
  }

  public TimeUpdaterBotBuilder setParty(String party) {
    this.party = party;
    return this;
  }

  public TimeUpdaterBot build() {
    LedgerApiHandle ledgerApiHandle = new JsonLedgerApiHandle(jsonLedgerClient, party);
    return new TimeUpdaterBot(ledgerApiHandle);
  }
}
