/*
 * Copyright (c) 2019, Digital Asset (Switzerland) GmbH and/or its affiliates. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package jsonapi;

import da.refapps.marketdataservice.roles.OperatorRole;
import io.reactivex.Flowable;
import java.util.Arrays;
import java.util.Collections;
import jsonapi.events.ArchivedEvent;
import jsonapi.events.CreatedEvent;
import jsonapi.http.WebSocketResponse;
import org.junit.Test;

public class ActiveContractSetTest {

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
        response.scan(new ActiveContractSet(), (acs, ws) -> acs.update(ws.getEvents()));

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
}
