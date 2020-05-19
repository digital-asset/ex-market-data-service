/*
 * Copyright (c) 2019, Digital Asset (Switzerland) GmbH and/or its affiliates. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */

package com.digitalasset.jsonapi.jackson.mixins;

import com.fasterxml.jackson.annotation.JsonCreator;

public abstract class PartyWrapper {
  @JsonCreator()
  public PartyWrapper(String party) {}
}
