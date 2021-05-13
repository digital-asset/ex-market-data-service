#!/usr/bin/env python3
#
# Copyright (c) 2019, Digital Asset (Switzerland) GmbH and/or its affiliates. All rights reserved.
# SPDX-License-Identifier: Apache-2.0
#

import logging
import sys
import time

from damlassistant import run_script, wait_for_port


dar = 'target/market-data-service.dar'
script_name = 'DA.RefApps.MarketDataService.MarketSetupScript:setupMarketForSandbox'

if len(sys.argv) < 2:
    print(f"Usage: populate.py SANDBOX_PORT")
    exit(1)
sandbox_port = sys.argv[1]

logging.basicConfig(level=logging.DEBUG)

wait_for_port(port=sandbox_port, timeout=30)
script = run_script(dar, script_name, sandbox_port)
if script.returncode != 0:
    raise Exception("Script has returned nonzero")
