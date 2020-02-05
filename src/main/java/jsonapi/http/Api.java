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
    return baseHttp.resolve("/command/create");
  }

  public URI exercise() {
    return baseHttp.resolve("/command/exercise");
  }

  public URI fetchContract() {
    return baseHttp.resolve("/contracts/lookup");
  }

  public URI searchContract() {
    return baseHttp.resolve("/contracts/search");
  }

  public URI parties() {
    return baseHttp.resolve("/parties");
  }

  public URI searchContractsForever() {
    return baseWs.resolve("/contracts/searchForever");
  }
}
