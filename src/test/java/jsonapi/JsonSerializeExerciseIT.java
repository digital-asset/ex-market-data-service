/*
 * Copyright (c) 2019, Digital Asset (Switzerland) GmbH and/or its affiliates. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package jsonapi;

import com.daml.ledger.javaapi.data.ExerciseCommand;
import com.daml.ledger.javaapi.data.Identifier;
import com.daml.ledger.javaapi.data.Party;
import com.daml.ledger.javaapi.data.Record;
import com.digitalasset.refapps.marketdataservice.Main;
import com.digitalasset.testing.junit4.Sandbox;
import com.digitalasset.testing.ledger.DefaultLedgerAdapter;
import com.digitalasset.testing.utils.ContractWithId;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import da.timeservice.timeservice.CurrentTime;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.Collections;

import da.timeservice.timeservice.TimeManager;
import jsonapi.apache.ApacheHttpClientIT;
import jsonapi.gson.ExerciseCommandSerializer;
import jsonapi.gson.RecordSerializer;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExternalResource;
import org.junit.rules.RuleChain;
import org.junit.rules.TestRule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JsonSerializeExerciseIT {
  private final Logger logger = LoggerFactory.getLogger(getClass());

  private static final Path RELATIVE_DAR_PATH = Paths.get("target/market-data-service.dar");
  private static final Party OPERATOR = new Party("Operator");

  private static final Sandbox sandbox =
      Sandbox.builder()
          .dar(RELATIVE_DAR_PATH)
          .module("Test.DA.TimeService.TimeServiceTest")
          .scenario("setupTimeTestWithAlice")
          .parties(OPERATOR.getValue())
          .build();

  @ClassRule public static ExternalResource startSandbox = sandbox.getClassRule();

  @Rule
  public TestRule processes =
      RuleChain.outerRule(sandbox.getRule()).around(new JsonApi(sandbox::getSandboxPort));

  private DefaultLedgerAdapter ledger;

  @Before
  public void setUp() {
    ledger = sandbox.getLedgerAdapter();
  }

  @Test
  public void AdvanceCurrentTimeIsCompletedByTheLedger() throws IOException {
    logger.info("start");
    ContractWithId<TimeManager.ContractId> timeManagerWithId =
            ledger.getMatchedContract(OPERATOR, TimeManager.TEMPLATE_ID, TimeManager.ContractId::new);
    logger.info("got manager");
    JsonLedgerClient jsonLedgerClient = new JsonLedgerClient(sandbox.getClient().getLedgerId(), this::toJson);

    jsonLedgerClient.exerciseChoice(
                    timeManagerWithId.contractId.exerciseAdvanceCurrentTime());
    logger.info("exercised");

    ContractWithId<CurrentTime.ContractId> currentTimeWithId =
            ledger.getMatchedContract(OPERATOR, CurrentTime.TEMPLATE_ID, CurrentTime.ContractId::new);
    logger.info("got time");
    CurrentTime currentTime = CurrentTime.fromValue(currentTimeWithId.record);
    logger.info(currentTime.toString());

    String result = jsonLedgerClient.getActiveContracts();
    logger.info(jsonFormat(result));
  }

  private String toJson(Object o) {
    final Gson gson =
            new GsonBuilder()
                    .registerTypeAdapter(ExerciseCommand.class, new ExerciseCommandSerializer())
                    .create();
    return gson.toJson(o);
  }

  private String jsonFormat(String json) {
    JsonParser parser = new JsonParser();
    Gson gson = new GsonBuilder().setPrettyPrinting().create();

    JsonElement el = parser.parse(json);
    return gson.toJson(el); // done
  }
}
