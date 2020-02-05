/*
 * Copyright (c) 2019, Digital Asset (Switzerland) GmbH and/or its affiliates. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package jsonapi;

import com.daml.ledger.javaapi.data.ExerciseCommand;
import com.daml.ledger.javaapi.data.Party;
import com.digitalasset.testing.junit4.Sandbox;
import com.digitalasset.testing.ledger.DefaultLedgerAdapter;
import com.digitalasset.testing.utils.ContractWithId;
import com.google.protobuf.InvalidProtocolBufferException;
import da.timeservice.timeservice.CurrentTime;
import org.junit.*;
import org.junit.rules.ExternalResource;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.Collections;
import java.util.concurrent.ExecutionException;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class JsonLedgerClientIT {

  private static final Path RELATIVE_DAR_PATH = Paths.get("target/market-data-service.dar");
  private static final Party OPERATOR = new Party("Operator");

  private static final Sandbox sandbox =
      Sandbox.builder().dar(RELATIVE_DAR_PATH).parties(OPERATOR.getValue()).useWallclockTime().build();

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
  public void getActiveContracts()
          throws InterruptedException, ExecutionException, InvalidProtocolBufferException, UnsupportedEncodingException {
    var currentTime =
        new CurrentTime(
            OPERATOR.getValue(), Instant.parse("2020-02-04T22:57:29Z"), Collections.emptyList());
    ledger.createContract(OPERATOR, CurrentTime.TEMPLATE_ID, currentTime.toValue());

    var jsonLedgerClient = new JsonLedgerClient(null, null);
    var result = jsonLedgerClient.getActiveContracts().get();

    assertThat(result.statusCode(), is(200));
    assertThat(
        result.body(),
        is(
            "{\"result\":[{\"observers\":[],\"agreementText\":\"\",\"payload\":{\"operator\":\"Operator\",\"currentTime\":\"2020-02-04T22:57:29Z\",\"observers\":[]},\"signatories\":[\"Operator\"],\"key\":\"Operator\",\"contractId\":\"#0:0\",\"templateId\":\"6f14cd82bbdbf637ae067f60af1d8da0b941de2e44f4b97b12e9fe7b5f13147a:DA.TimeService.TimeService:CurrentTime\"}],\"status\":200}"));
  }

  @Test
  public void exerciseChoice()
          throws InterruptedException, ExecutionException, InvalidProtocolBufferException, UnsupportedEncodingException {
    var currentTime =
        new CurrentTime(
            OPERATOR.getValue(), Instant.parse("2020-02-04T22:57:29Z"), Collections.emptyList());
    ledger.createContract(OPERATOR, CurrentTime.TEMPLATE_ID, currentTime.toValue());
    ContractWithId<CurrentTime.ContractId> currentTimeWithId =
        ledger.getMatchedContract(OPERATOR, CurrentTime.TEMPLATE_ID, CurrentTime.ContractId::new);

    var jsonLedgerClient = new JsonLedgerClient(sandbox.getClient().getLedgerId(), this::objectToJsonMapper);
    var result = jsonLedgerClient.exerciseChoice(currentTimeWithId.contractId.exerciseCurrentTime_AddObserver(OPERATOR.getValue())).get();

    assertThat(result.statusCode(), is(200));
  }

  private String objectToJsonMapper(Object o) {
    if (o instanceof ExerciseCommand) {
      String newObserver = ((ExerciseCommand) o).getChoiceArgument().asRecord().get().getFieldsMap().get("newObserver").asParty().get().getValue();
      String templateId = String.format("%s:%s", ((ExerciseCommand) o).getTemplateId().getModuleName(),((ExerciseCommand) o).getTemplateId().getEntityName());
      return String.format("{ \"templateId\": \"%s\", \"contractId\": \"%s\", \"choice\": \"%s\", \"argument\": { \"newObserver\": \"%s\" } }",
              templateId, ((ExerciseCommand) o).getContractId(), ((ExerciseCommand) o).getChoice(), newObserver);
    }
    throw new RuntimeException("Unhandled case: " + o.toString());
  }
}
