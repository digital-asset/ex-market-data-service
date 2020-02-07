/*
 * Copyright (c) 2019, Digital Asset (Switzerland) GmbH and/or its affiliates. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package jsonapi.events;

import java.util.Set;
import jsonapi.ActiveContract;

public interface Event {

  void addOrRemove(Set<ActiveContract> activeContracts);
}
