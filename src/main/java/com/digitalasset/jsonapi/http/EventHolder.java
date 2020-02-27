/*
 * Copyright (c) 2019, Digital Asset (Switzerland) GmbH and/or its affiliates. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package com.digitalasset.jsonapi.http;

import com.digitalasset.jsonapi.events.Event;

public interface EventHolder {
  Event event();
}
