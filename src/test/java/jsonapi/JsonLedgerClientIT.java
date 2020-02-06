/*
 * Copyright (c) 2019, Digital Asset (Switzerland) GmbH and/or its affiliates. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package jsonapi;

import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.Assert.assertThat;

import com.daml.ledger.javaapi.data.ExerciseCommand;
import com.daml.ledger.javaapi.data.Party;
import com.digitalasset.testing.junit4.Sandbox;
import com.digitalasset.testing.ledger.DefaultLedgerAdapter;
import com.digitalasset.testing.utils.ContractWithId;
import da.timeservice.timeservice.CurrentTime;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.Collections;
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
      Sandbox.builder()
          .dar(RELATIVE_DAR_PATH)
          .parties(OPERATOR.getValue())
          .useWallclockTime()
          .build();

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
    //    try (Scanner scanner = new Scanner(jsonApi.getInputStream())) {
    //      while (scanner.hasNextLine() && !scanner.nextLine().contains("Connected to Ledger")) {
    //        Thread.sleep(100);
    //      }
    //    }
    Thread.sleep(5000);
  }

  @After
  public void tearDown() throws Exception {
    jsonApi.destroy();
    jsonApi.waitFor();
  }

  @Test
  public void getActiveContracts() throws IOException {
    CurrentTime currentTime =
        new CurrentTime(
            OPERATOR.getValue(), Instant.parse("2020-02-04T22:57:29Z"), Collections.emptyList());
    ledger.createContract(OPERATOR, CurrentTime.TEMPLATE_ID, currentTime.toValue());

    JsonLedgerClient ledger = new JsonLedgerClient(sandbox.getClient().getLedgerId(), null);
    String result = ledger.getActiveContracts();

    assertThat(result, containsString("200"));
    //    assertThat(
    //        result,
    //        containsString(
    //
    // "{\"result\":[{\"observers\":[],\"agreementText\":\"\",\"payload\":{\"operator\":\"Operator\",\"currentTime\":\"2020-02-04T22:57:29Z\",\"observers\":[]},\"signatories\":[\"Operator\"],\"key\":\"Operator\",\"contractId\":\"#0:0\",\"templateId\":\"6f14cd82bbdbf637ae067f60af1d8da0b941de2e44f4b97b12e9fe7b5f13147a:DA.TimeService.TimeService:CurrentTime\"}],\"status\":200}"));
  }

  @Test
  public void exerciseChoice() throws IOException {
    CurrentTime currentTime =
        new CurrentTime(
            OPERATOR.getValue(), Instant.parse("2020-02-04T22:57:29Z"), Collections.emptyList());
    ledger.createContract(OPERATOR, CurrentTime.TEMPLATE_ID, currentTime.toValue());
    ContractWithId<CurrentTime.ContractId> currentTimeWithId =
        ledger.getMatchedContract(OPERATOR, CurrentTime.TEMPLATE_ID, CurrentTime.ContractId::new);

    JsonLedgerClient jsonLedgerClient =
        new JsonLedgerClient(sandbox.getClient().getLedgerId(), this::objectToJsonMapper);
    String result =
        jsonLedgerClient.exerciseChoice(
            currentTimeWithId.contractId.exerciseCurrentTime_AddObserver(OPERATOR.getValue()));

    assertThat(result, containsString("200"));
  }

  private String objectToJsonMapper(Object o) {
    if (o instanceof ExerciseCommand) {
      String newObserver =
          ((ExerciseCommand) o)
              .getChoiceArgument()
              .asRecord()
              .get()
              .getFieldsMap()
              .get("newObserver")
              .asParty()
              .get()
              .getValue();
      String templateId =
          String.format(
              "%s:%s",
              ((ExerciseCommand) o).getTemplateId().getModuleName(),
              ((ExerciseCommand) o).getTemplateId().getEntityName());
      return String.format(
          "{ \"templateId\": \"%s\", \"contractId\": \"%s\", \"choice\": \"%s\", \"argument\": { \"newObserver\": \"%s\" } }",
          templateId,
          ((ExerciseCommand) o).getContractId(),
          ((ExerciseCommand) o).getChoice(),
          newObserver);
    }
    throw new RuntimeException("Unhandled case: " + o.toString());
  }
}
