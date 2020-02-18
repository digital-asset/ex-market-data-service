/*
 * Copyright (c) 2019, Digital Asset (Switzerland) GmbH and/or its affiliates. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package jsonapi;

import com.daml.ledger.javaapi.data.Identifier;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;
import jsonapi.events.Event;

public class ActiveContractSet {

  private final Map<String, ActiveContract> activeContracts;

  private ActiveContractSet() {
    this(new HashMap<>());
  }

  ActiveContractSet(Map<String, ActiveContract> activeContracts) {
    this.activeContracts = activeContracts;
  }

  public static ActiveContractSet empty() {
    return new ActiveContractSet();
  }

  // TODO: Rename this method.
  public Stream<ActiveContract> getActiveContracts() {
    return Collections.unmodifiableCollection(activeContracts.values()).stream();
  }

  public <T> Stream<Contract<T>> getActiveContracts(Identifier identifier, Class<T> type) {
    return activeContracts.values().stream()
        .filter(x -> x.getIdentifier().equals(identifier))
        .map(x -> createContract(x, type));
  }

  private <T> Contract<T> createContract(ActiveContract x, Class<T> type) {
    return new Contract<>(x.getContractId(), type.cast(x.getTemplate()));
  }

  public ActiveContractSet update(Collection<? extends Event> events) {
    return events.stream().reduce(this, (acs, event) -> event.update(acs), (left, right) -> left);
  }

  public ActiveContractSet add(ActiveContract activeContract) {
    if (!this.activeContracts.containsKey(activeContract.getContractId())) {
      return updateSet(activeContract);
    } else if (!this.activeContracts.get(activeContract.getContractId()).equals(activeContract)) {
      return updateSet(activeContract);
    } else {
      return this;
    }
  }

  private ActiveContractSet updateSet(ActiveContract activeContract) {
    Map<String, ActiveContract> newActiveContracts = new HashMap<>(this.activeContracts);
    newActiveContracts.put(activeContract.getContractId(), activeContract);
    return new ActiveContractSet(newActiveContracts);
  }

  public ActiveContractSet remove(String contractId) {
    if (this.activeContracts.containsKey(contractId)) {
      Map<String, ActiveContract> newActiveContracts = new HashMap<>(this.activeContracts);
      newActiveContracts.remove(contractId);
      return new ActiveContractSet(newActiveContracts);
    } else {
      return this;
    }
  }

  // TODO: Does it make sense to have an isEmpty?
  public boolean isEmpty() {
    return this.activeContracts.isEmpty();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ActiveContractSet that = (ActiveContractSet) o;
    return activeContracts.equals(that.activeContracts);
  }

  @Override
  public int hashCode() {
    return Objects.hash(activeContracts);
  }

  @Override
  public String toString() {
    return "ActiveContractSet" + activeContracts.values();
  }
}
