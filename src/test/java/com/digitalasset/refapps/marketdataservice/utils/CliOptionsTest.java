/*
 * Copyright (c) 2019, Digital Asset (Switzerland) GmbH and/or its affiliates. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package com.digitalasset.refapps.marketdataservice.utils;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;
import org.kohsuke.args4j.CmdLineException;

public class CliOptionsTest {

  @Test(expected = CmdLineException.class)
  public void notSpecifyingLedgerIdThrowsException() throws CmdLineException {
    String[] args = {};
    CliOptions.parseArgs(args);
  }

  @Test
  public void cliOptionsCanBeParsedWhenLedgerIdSpecified() throws CmdLineException {
    String[] args = {"-ledgerId", "sample-ledger"};
    CliOptions cliOptions = CliOptions.parseArgs(args);
    assertThat(cliOptions.getLedgerId(), is("sample-ledger"));
  }

  @Test
  public void jsonApiOptionsCanBeSpecified() throws CmdLineException {
    String[] args = {"-ledgerId", "", "-jsonHost", "sample-api", "-jsonPort", "4849"};
    CliOptions cliOptions = CliOptions.parseArgs(args);
    assertThat(cliOptions.getJsonApiHost(), is("sample-api"));
    assertThat(cliOptions.getJsonApiPort(), is(4849));
  }
}
