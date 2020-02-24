/*
 * Copyright (c) 2019, Digital Asset (Switzerland) GmbH and/or its affiliates. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package com.digitalasset.refapps.marketdataservice.assertions;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.daml.ledger.javaapi.data.Command;
import com.daml.ledger.javaapi.data.ExerciseCommand;
import java.util.List;
import java.util.Optional;

public class Assert {

  public static void assertHasSingleExercise(
      List<Command> actualCommands, String cid, String choiceName) {
    assertEquals(1, actualCommands.size());
    actualCommands.forEach(
        cmd -> {
          Optional<ExerciseCommand> exerciseCommand = cmd.asExerciseCommand();
          assertTrue(exerciseCommand.isPresent());
          assertEquals(cid, exerciseCommand.get().getContractId());
          assertEquals(choiceName, exerciseCommand.get().getChoice());
        });
  }

  @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
  public static <T> void assertOptionalValue(T expectedValue, Optional<T> actual) {
    assertTrue(actual.isPresent());
    assertEquals(expectedValue, actual.get());
  }

  @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
  public static <T> void assertEmpty(Optional<T> result) {
    assertFalse(result.isPresent());
  }
}
