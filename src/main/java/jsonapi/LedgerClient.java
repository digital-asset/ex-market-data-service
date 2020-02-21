/*
 * Copyright (c) 2019, Digital Asset (Switzerland) GmbH and/or its affiliates. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package jsonapi;

import com.daml.ledger.javaapi.data.CreateCommand;
import com.daml.ledger.javaapi.data.ExerciseCommand;
import io.reactivex.Flowable;

public interface LedgerClient {

  String create(CreateCommand command);

  String exerciseChoice(ExerciseCommand command);

  ActiveContractSet getActiveContracts();

  ActiveContractSet queryContracts(ContractQuery query);

  Flowable<ActiveContractSet> getActiveContracts(ContractQuery query);
}
