/*
 * Copyright (c) 2019, Digital Asset (Switzerland) GmbH and/or its affiliates. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package jsonapi.http;

import java.net.URI;

public class Api {

  private final URI baseHttp;
  private final URI baseWs;

  public Api(String host, int port) {
    baseHttp = URI.create(String.format("%s://%s:%d", "http", host, port));
    baseWs = URI.create(String.format("%s://%s:%d", "ws", host, port));
  }

  public URI createContract() {
    return baseHttp.resolve("/v1/create");
  }

  public URI exercise() {
    return baseHttp.resolve("/v1/exercise");
  }

  public URI fetchContract() {
    return baseHttp.resolve("/v1/fetch");
  }

  public URI searchContract() {
    return baseHttp.resolve("/v1/query");
  }

  public URI parties() {
    return baseHttp.resolve("/v1/parties");
  }

  public URI searchContractsForever() {
    return baseWs.resolve("/v1/stream/query");
  }

  public URI fetchContractsForever() {
    return baseWs.resolve("/v1/stream/fetch");
  }
}
