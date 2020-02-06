/*
 * Copyright (c) 2019, Digital Asset (Switzerland) GmbH and/or its affiliates. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package jsonapi.http;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.net.URI;
import org.junit.Test;

public class ApiTest {

  private final Api api = new Api("sample.com", 5678);

  @Test
  public void createContract() {
    assertThat(api.createContract(), is(URI.create("http://sample.com:5678/command/create")));
  }

  @Test
  public void exercise() {
    assertThat(api.exercise(), is(URI.create("http://sample.com:5678/command/exercise")));
  }

  @Test
  public void fetchContract() {
    assertThat(api.fetchContract(), is(URI.create("http://sample.com:5678/contracts/lookup")));
  }

  @Test
  public void searchContract() {
    assertThat(api.searchContract(), is(URI.create("http://sample.com:5678/contracts/search")));
  }

  @Test
  public void parties() {
    assertThat(api.parties(), is(URI.create("http://sample.com:5678/parties")));
  }

  @Test
  public void searchContractsForever() {
    assertThat(
        api.searchContractsForever(),
        is(URI.create("ws://sample.com:5678/contracts/searchForever")));
  }
}
