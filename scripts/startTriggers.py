#!/usr/bin/env python3
#
# Copyright (c) 2020, Digital Asset (Switzerland) GmbH and/or its affiliates. All rights reserved.
# SPDX-License-Identifier: Apache-2.0
#

import logging
import sys
import time

from damlassistant import get_package_id, start_trigger_service_in_background, kill_background_process, \
    add_trigger_to_service, wait_for_port, catch_signals, DEFAULT_TRIGGER_SERVICE_PORT, DEFAULT_SANDBOX_PORT


dar = 'target/market-data-service.dar'

triggers_with_parties = [
    ("MarketDataVendor", "DA.RefApps.MarketDataService.Triggers.Enrichment:republishObservationTrigger"),
    ("AnalyticsVendor", "DA.RefApps.MarketDataService.Triggers.Enrichment:enrichCleanPriceWithAccrualTrigger"),
    ("MarketDataProvider1", "DA.RefApps.MarketDataService.Triggers.AutoRegisterLicense:automaticLicenseRegistrarTrigger"),
    ("MarketDataProvider2", "DA.RefApps.MarketDataService.Triggers.AutoRegisterLicense:automaticLicenseRegistrarTrigger"),
    ("MarketDataVendor", "DA.RefApps.MarketDataService.Triggers.AutoRegisterLicense:automaticLicenseRegistrarTrigger"),
    ("AnalyticsVendor", "DA.RefApps.MarketDataService.Triggers.AutoRegisterLicense:automaticLicenseRegistrarTrigger"),
]

if len(sys.argv) < 2:
    print(f"Usage: startTriggers.py SANDBOX_PORT")
    exit(1)
sandbox_port = sys.argv[1]

logging.basicConfig(level=logging.DEBUG)

wait_for_port(port=sandbox_port, timeout=30)

service = start_trigger_service_in_background(dar = dar, sandbox_port = sandbox_port)
try:
    catch_signals()
    package_id = get_package_id(dar)
    wait_for_port(port=DEFAULT_TRIGGER_SERVICE_PORT, timeout=30)
    for party, triggerName in triggers_with_parties:
        add_trigger_to_service(party=party, package_id=package_id, trigger=triggerName)
    time.sleep(3)
    print('\nPress Ctrl+C to stop...')
    service.wait()
    logging.error(f"Trigger service died unexpectedly:\n{service.stderr}")
finally:
    kill_background_process(service)
