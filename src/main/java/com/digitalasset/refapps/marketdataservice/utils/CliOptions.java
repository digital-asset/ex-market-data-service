/*
 * Copyright (c) 2019, Digital Asset (Switzerland) GmbH and/or its affiliates. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package com.digitalasset.refapps.marketdataservice.utils;

import static com.digitalasset.refapps.marketdataservice.utils.AppParties.ALL_PARTIES;

import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;
import org.kohsuke.args4j.spi.StringArrayOptionHandler;

// CLI parser requires non-fields for options.
@SuppressWarnings({"PMD.DataClass", "FieldCanBeLocal", "CanBeFinal"})
public class CliOptions {

  @Option(name = "-ledgerId", usage = "Ledger ID", metaVar = "LEDGER_ID", required = true)
  private String ledgerId = null;

  @Option(name = "-jsonHost", usage = "JSON API host", metaVar = "JSON_API_HOST")
  private String jsonApiHost = "localhost";

  @Option(name = "-jsonPort", usage = "JSON API port", metaVar = "JSON_API_PORT")
  private int jsonApiPort = 7575;

  @Option(
      name = "-u",
      usage = "Parties to run the application for.",
      handler = StringArrayOptionHandler.class,
      metaVar = "PARTIES")
  private String[] parties = ALL_PARTIES;

  public String[] getParties() {
    return parties;
  }

  public String getLedgerId() {
    return ledgerId;
  }

  public String getJsonApiHost() {
    return jsonApiHost;
  }

  public int getJsonApiPort() {
    return jsonApiPort;
  }

  public static CliOptions parseArgs(String[] args) throws CmdLineException {
    CliOptions options = new CliOptions();
    CmdLineParser parser = new CmdLineParser(options);
    try {
      parser.parseArgument(args);
    } catch (CmdLineException e) {
      System.err.println("Invalid command line options");
      parser.printUsage(System.err);
      throw e;
    }
    return options;
  }
}
