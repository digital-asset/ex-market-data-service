/*
 * Copyright (c) 2019, Digital Asset (Switzerland) GmbH and/or its affiliates. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package jsonapi;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import com.daml.ledger.javaapi.data.Party;
import com.digitalasset.testing.junit4.Sandbox;
import com.digitalasset.testing.ledger.DefaultLedgerAdapter;
import com.google.protobuf.InvalidProtocolBufferException;
import da.timeservice.timeservice.CurrentTime;
import java.io.IOException;
import java.net.http.HttpRequest;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.Collections;
import java.util.Scanner;
import java.util.concurrent.ExecutionException;
import org.junit.After;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExternalResource;

public class JsonLedgerClientIT {

  private static final Path RELATIVE_DAR_PATH = Paths.get("target/market-data-service.dar");
  private static final Party OPERATOR = new Party("Operator");

  private static final Sandbox sandbox =
      Sandbox.builder().dar(RELATIVE_DAR_PATH).parties(OPERATOR.getValue()).build();

  @ClassRule public static ExternalResource startSandbox = sandbox.getClassRule();
  @Rule public ExternalResource restartSandbox = sandbox.getRule();
  private DefaultLedgerAdapter ledger;
  private Process jsonApi;

  @Before
  public void setUp() throws IOException, InterruptedException {
    ledger = sandbox.getLedgerAdapter();
    jsonApi =
        new ProcessBuilder(
                "daml",
                "json-api",
                "--ledger-host",
                "localhost",
                "--ledger-port",
                Integer.toString(sandbox.getSandboxPort()),
                "--http-port",
                "7575",
                "--max-inbound-message-size",
                "4194304",
                "--package-reload-interval",
                "5s",
                "--application-id",
                "HTTP-JSON-API-Gateway")
            .start();
    waitForJsonApi();
  }

  private void waitForJsonApi() throws InterruptedException {
    try (Scanner scanner = new Scanner(jsonApi.getInputStream())) {
      while (scanner.hasNextLine() && !scanner.nextLine().contains("Connected to Ledger")) {
        Thread.sleep(100);
      }
    }
  }

  @After
  public void tearDown() throws Exception {
    jsonApi.destroy();
    jsonApi.waitFor();
  }

  @Test
  public void getActiveContracts()
      throws InterruptedException, ExecutionException, InvalidProtocolBufferException {
    var currentTime =
        new CurrentTime(
            OPERATOR.getValue(), Instant.parse("2020-02-04T22:57:29Z"), Collections.emptyList());
    ledger.createContract(OPERATOR, CurrentTime.TEMPLATE_ID, currentTime.toValue());

    var ledger = new JsonLedgerClient(null);
    var result = ledger.getActiveContracts().get();

    assertThat(result.statusCode(), is(200));
    assertThat(
        result.body(),
        is(
            "{\"result\":[{\"observers\":[],\"agreementText\":\"\",\"payload\":{\"operator\":\"Operator\",\"currentTime\":\"2020-02-04T22:57:29Z\",\"observers\":[]},\"signatories\":[\"Operator\"],\"key\":\"Operator\",\"contractId\":\"#0:0\",\"templateId\":\"6f14cd82bbdbf637ae067f60af1d8da0b941de2e44f4b97b12e9fe7b5f13147a:DA.TimeService.TimeService:CurrentTime\"}],\"status\":200}"));
  }

  @Test
  public void getContractByKey()
      throws InterruptedException, ExecutionException, InvalidProtocolBufferException {
    var currentTime =
        new CurrentTime(
            OPERATOR.getValue(), Instant.parse("2020-02-04T22:57:29Z"), Collections.emptyList());
    ledger.createContract(OPERATOR, CurrentTime.TEMPLATE_ID, currentTime.toValue());

    var ledger = new JsonLedgerClient(null);
    String post = "{\"templateId\": \"DA.TimeService.TimeService:CurrentTime\", \"key\": \"Operator\" }";

    /* To avoid:
java.util.concurrent.ExecutionException: java.net.http.HttpTimeoutException: request timed out

	at java.base/java.util.concurrent.CompletableFuture.reportGet(CompletableFuture.java:395)
	at java.base/java.util.concurrent.CompletableFuture.get(CompletableFuture.java:1999)
	at jsonapi.JsonLedgerClientIT.getContractByKey(JsonLedgerClientIT.java:112)
Caused by: java.net.http.HttpTimeoutException: request timed out
	at java.net.http/jdk.internal.net.http.ResponseTimerEvent.handle(ResponseTimerEvent.java:63)
	at java.net.http/jdk.internal.net.http.HttpClientImpl.purgeTimeoutsAndReturnNextDeadline(HttpClientImpl.java:1248)
	at java.net.http/jdk.internal.net.http.HttpClientImpl$SelectorManager.run(HttpClientImpl.java:877)

     */
    Thread.sleep(1_000);

    System.out.println(post);
    var result = ledger.getContractByKey(HttpRequest.BodyPublishers.ofString(post)).get();

    assertThat(result.statusCode(), is(200));
    assertThat(
        result.body(),
        is(
                "{\"status\":200,\"result\":{\"observers\":[],\"agreementText\":\"\",\"payload\":{\"operator\":\"Operator\",\"currentTime\":\"2020-02-04T22:57:29Z\",\"observers\":[]},\"signatories\":[\"Operator\"],\"key\":\"Operator\",\"contractId\":\"#0:0\",\"templateId\":\"6f14cd82bbdbf637ae067f60af1d8da0b941de2e44f4b97b12e9fe7b5f13147a:DA.TimeService.TimeService:CurrentTime\"}}"
        ));
  }
}
