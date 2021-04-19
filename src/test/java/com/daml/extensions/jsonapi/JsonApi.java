/*
 * Copyright (c) 2019, Digital Asset (Switzerland) GmbH and/or its affiliates. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package com.daml.extensions.jsonapi;

import static org.junit.Assert.fail;

import java.io.File;
import java.lang.ProcessBuilder.Redirect;
import java.net.URI;
import java.util.function.IntSupplier;
import org.junit.rules.ExternalResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JsonApi extends ExternalResource {

  private final Logger log = LoggerFactory.getLogger(getClass());
  private final IntSupplier ledgerPort;
  private Process jsonApi;

  public JsonApi(IntSupplier ledgerPort) {
    this.ledgerPort = ledgerPort;
  }

  @Override
  protected void before() throws Throwable {
    super.before();
    ProcessBuilder processBuilder =
        new ProcessBuilder(
                "daml",
                "json-api",
                "--ledger-host",
                "localhost",
                "--ledger-port",
                Integer.toString(ledgerPort.getAsInt()),
                "--http-port",
                "7575",
                "--max-inbound-message-size",
                "4194304",
                "--package-reload-interval",
                "5s",
                "--application-id",
                "HTTP-JSON-API-Gateway")
            .redirectOutput(Redirect.appendTo(new File("json-api.log")))
            .redirectError(Redirect.appendTo(new File("json-api.err.log")));
    log.debug("Executing: {}", String.join(" ", processBuilder.command()));
    jsonApi = processBuilder.start();
    waitForJsonApi();
  }

  private static void waitForJsonApi() {
    try {
      Utils.waitForJsonApi(URI.create("http://localhost:7575"));
    } catch (Exception e) {
      fail("Failed to start JSON API");
    }
  }

  @Override
  protected void after() {
    super.after();
    log.info("Stopping JSON API...");
    try {
      jsonApi.destroy();
      jsonApi.waitFor();
    } catch (InterruptedException e) {
      fail(e.getMessage());
    }
    log.info("Stopped JSON API.");
  }
}
