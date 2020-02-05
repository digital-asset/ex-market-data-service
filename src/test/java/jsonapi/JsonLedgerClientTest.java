/*
 * Copyright (c) 2019, Digital Asset (Switzerland) GmbH and/or its affiliates. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package jsonapi;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertThat;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import javax.websocket.DeploymentException;
import org.junit.Ignore;
import org.junit.Test;

public class JsonLedgerClientTest {

  @Ignore
  @Test
  public void getActiveContracts() throws IOException {
    JsonLedgerClient ledger = new JsonLedgerClient(null);
    String result = ledger.getActiveContracts();
    assertThat(result, containsString("200"));
    assertThat(result, not(containsString("\"result\":[]")));
  }

  @Ignore
  @Test
  public void webSocket() throws InterruptedException, IOException, DeploymentException {
    JsonLedgerClient ledger = new JsonLedgerClient(null);
    CountDownLatch latch = new CountDownLatch(1);
    ledger.getActiveContractsViaWebSockets(latch);
    latch.await();
  }
}
