/*
 * Copyright (c) 2019, Digital Asset (Switzerland) GmbH and/or its affiliates. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package com.digitalasset.jsonapi.http;

import java.net.URI;

public interface HttpClient {

  HttpResponse get(URI resource);

  HttpResponse post(URI resource, Object body);
}
