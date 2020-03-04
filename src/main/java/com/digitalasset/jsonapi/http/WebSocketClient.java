/*
 * Copyright (c) 2019, Digital Asset (Switzerland) GmbH and/or its affiliates. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package com.digitalasset.jsonapi.http;

import io.reactivex.Flowable;
import java.net.URI;

public interface WebSocketClient {

  Flowable<WebSocketResponse> post(URI resource, Object body);
}
