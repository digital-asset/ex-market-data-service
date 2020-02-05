/*
 * Copyright (c) 2019, Digital Asset (Switzerland) GmbH and/or its affiliates. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package jsonapi;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;

import java.io.UnsupportedEncodingException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import org.junit.Ignore;
import org.junit.Test;

public class JsonLedgerClientTest {

  @Ignore
  @Test
  public void getActiveContracts()
      throws InterruptedException, ExecutionException, UnsupportedEncodingException {
    var ledger = new JsonLedgerClient(null, null);
    var result = ledger.getActiveContracts().get();
    assertThat(result.statusCode(), is(200));
    assertThat(result.body(), not(containsString("\"result\":[]")));
  }

  @Ignore
  @Test
  public void webSocket() throws InterruptedException, UnsupportedEncodingException {
    var ledger = new JsonLedgerClient(null, null);
    var latch = new CountDownLatch(1);
    ledger.getActiveContractsViaWebSockets(latch);
    latch.await();
  }
}
