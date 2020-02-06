/*
 * Copyright (c) 2019, Digital Asset (Switzerland) GmbH and/or its affiliates. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package jsonapi;

import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.function.IntSupplier;
import org.apache.http.HttpResponse;
import org.apache.http.client.fluent.Request;
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
    jsonApi =
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
            .redirectOutput(new File("json-api.log"))
            .redirectError(new File("json-api.err.log"))
            .start();
    waitForJsonApi();
  }

  private void waitForJsonApi() throws InterruptedException {
    Instant started = Instant.now();
    boolean isRunning = false;
    while (!isRunning && !hasPassedSince(started, Duration.ofSeconds(30))) {
      log.info("Waiting for JSON API...");
      try {
        HttpResponse response = Request.Options("http://localhost:7575").execute().returnResponse();
        if (response.getStatusLine().getStatusCode() < 500) {
          isRunning = true;
        }
      } catch (IOException ignored) {
      }
      Thread.sleep(1000);
    }
    if (!isRunning) {
      fail("Failed to start JSON API");
    }
    log.info("JSON API available.");
  }

  private boolean hasPassedSince(Instant started, Duration timeout) {
    Duration elapsed = Duration.between(started, Instant.now());
    return elapsed.compareTo(timeout) > 0;
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
