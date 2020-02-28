/*
 * Copyright (c) 2019, Digital Asset (Switzerland) GmbH and/or its affiliates. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package com.digitalasset.jsonapi.events;

import com.digitalasset.jsonapi.ActiveContractSet;

public interface Event {

  ActiveContractSet update(ActiveContractSet activeContractSet);
}
