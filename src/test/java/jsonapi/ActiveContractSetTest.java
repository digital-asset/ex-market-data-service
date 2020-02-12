/*
 * Copyright (c) 2019, Digital Asset (Switzerland) GmbH and/or its affiliates. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package jsonapi;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertSame;

import com.daml.ledger.javaapi.data.CreateCommand;
import com.daml.ledger.javaapi.data.Identifier;
import com.daml.ledger.javaapi.data.Template;
import da.refapps.marketdataservice.roles.OperatorRole;
import io.reactivex.Flowable;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import jsonapi.events.ArchivedEvent;
import jsonapi.events.CreatedEvent;
import jsonapi.http.WebSocketResponse;
import org.junit.Test;

public class ActiveContractSetTest {

  @Test
  public void emptyContractSetHasNoContracts() {
    ActiveContractSet acs = ActiveContractSet.empty();

    Iterator<ActiveContract> contracts = acs.getActiveContracts().iterator();

    assertFalse(contracts.hasNext());
  }

  @Test
  public void addingContractDoesNotChangeOriginalSet() {
    ActiveContractSet acs = ActiveContractSet.empty();

    acs.add(new ActiveContract(null, "#123", new DummyTemplate()));

    Iterator<ActiveContract> contracts = acs.getActiveContracts().iterator();
    assertFalse(contracts.hasNext());
  }

  @Test
  public void addingContractToEmptySetReturnsNewSetWithContract() {
    ActiveContractSet acs = ActiveContractSet.empty();

    ActiveContract activeContract = new ActiveContract(null, "#123", new DummyTemplate());
    ActiveContractSet newAcs = acs.add(activeContract);

    Iterator<ActiveContract> contracts = newAcs.getActiveContracts().iterator();
    assertEquals(activeContract, contracts.next());
    assertFalse(contracts.hasNext());
  }

  @Test
  public void addingTheSameContractTwiceDoesNotChangeTheSet() {
    ActiveContract activeContract = new ActiveContract(null, "#123", new DummyTemplate());
    ActiveContractSet acs = ActiveContractSet.empty().add(activeContract);

    ActiveContractSet sameAcs = acs.add(activeContract);

    assertSame(acs, sameAcs);
  }

  @Test
  public void addingContractWithExistingIdReturnsNewUpdatedSet() {
    Identifier identifier = new Identifier("p", "m", "e");
    DummyTemplate template1 = new DummyTemplate();
    ActiveContract activeContract = new ActiveContract(identifier, "#123", template1);
    ActiveContractSet acs = ActiveContractSet.empty().add(activeContract);

    DummyTemplate template2 = new DummyTemplate();
    ActiveContract updatedContract = new ActiveContract(identifier, "#123", template2);
    ActiveContractSet newAcs = acs.add(updatedContract);

    Iterator<ActiveContract> contracts = newAcs.getActiveContracts().iterator();
    assertNotEquals(activeContract, updatedContract);
    assertEquals(updatedContract, contracts.next());
    assertFalse(contracts.hasNext());
  }

  @Test
  public void removeFromEmptySetReturnsTheSameSet() {
    ActiveContract activeContract = new ActiveContract(null, "#123", new DummyTemplate());
    ActiveContractSet acs = ActiveContractSet.empty();

    ActiveContractSet sameAcs = acs.remove(activeContract.getContractId());

    assertSame(acs, sameAcs);
  }

  @Test
  public void removeContractNotInSetReturnsTheSameSet() {
    ActiveContract activeContract = new ActiveContract(null, "#123", new DummyTemplate());
    ActiveContractSet acs = ActiveContractSet.empty().add(activeContract);

    ActiveContractSet sameAcs = acs.remove("non existing id");

    assertSame(acs, sameAcs);
  }

  @Test
  public void removingExistingContractReturnsNewSetWithoutGivenContract() {
    ActiveContract activeContract = new ActiveContract(null, "#123", new DummyTemplate());
    ActiveContractSet acs = ActiveContractSet.empty().add(activeContract);

    ActiveContractSet newAcs = acs.remove(activeContract.getContractId());
    Iterator<ActiveContract> contracts = newAcs.getActiveContracts().iterator();

    assertFalse(contracts.hasNext());
  }

  @Test
  public void scan() {
    Flowable<WebSocketResponse> response =
        Flowable.just(
            new WebSocketResponse(
                Collections.singletonList(
                    new CreatedEvent(
                        OperatorRole.TEMPLATE_ID, "#1:0", new OperatorRole("Operator1")))),
            new WebSocketResponse(
                Arrays.asList(
                    new CreatedEvent(
                        OperatorRole.TEMPLATE_ID, "#2:0", new OperatorRole("Operator2")),
                    new ArchivedEvent("#1:0"))));

    Flowable<ActiveContractSet> activeContractSet =
        response.scan(ActiveContractSet.empty(), (acs, ws) -> acs.update(ws.getEvents()));

    activeContractSet
        .test()
        .assertValues(
            new ActiveContractSet(Collections.emptyMap()),
            new ActiveContractSet(
                Collections.singletonMap(
                    "#1:0",
                    new ActiveContract(
                        OperatorRole.TEMPLATE_ID, "#1:0", new OperatorRole("Operator1")))),
            new ActiveContractSet(
                Collections.singletonMap(
                    "#2:0",
                    new ActiveContract(
                        OperatorRole.TEMPLATE_ID, "#2:0", new OperatorRole("Operator2")))));
  }

  private static class DummyTemplate extends Template {

    @Override
    public CreateCommand create() {
      return null;
    }
  }
}
