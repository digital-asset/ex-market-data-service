#!/usr/bin/env bash
#
# Copyright (c) 2019, Digital Asset (Switzerland) GmbH and/or its affiliates. All rights reserved.
# SPDX-License-Identifier: Apache-2.0
#

find
source $(dirname "$0")/lib/startTriggers_common.sh

daml trigger \
    --wall-clock-time \
    --dar "${DAR_FILE}" \
    --trigger-name DA.RefApps.MarketDataService.Triggers.AutoRegisterLicense:automaticLicenseRegistrarTrigger \
    --ledger-host ${SANDBOX_HOST} \
    --ledger-port ${SANDBOX_PORT} \
    --ledger-party MarketDataProvider1 &

daml trigger \
    --wall-clock-time \
    --dar "${DAR_FILE}" \
    --trigger-name DA.RefApps.MarketDataService.Triggers.AutoRegisterLicense:automaticLicenseRegistrarTrigger \
    --ledger-host ${SANDBOX_HOST} \
    --ledger-port ${SANDBOX_PORT} \
    --ledger-party MarketDataProvider2 &

daml trigger \
    --wall-clock-time \
    --dar "${DAR_FILE}" \
    --trigger-name DA.RefApps.MarketDataService.Triggers.AutoRegisterLicense:automaticLicenseRegistrarTrigger \
    --ledger-host ${SANDBOX_HOST} \
    --ledger-port ${SANDBOX_PORT} \
    --ledger-party MarketDataVendor &

daml trigger \
    --wall-clock-time \
    --dar "${DAR_FILE}" \
    --trigger-name DA.RefApps.MarketDataService.Triggers.AutoRegisterLicense:automaticLicenseRegistrarTrigger \
    --ledger-host ${SANDBOX_HOST} \
    --ledger-port ${SANDBOX_PORT} \
    --ledger-party AnalyticsVendor &

sleep 2
pids=$(jobs -p)
echo Waiting for $pids
wait $pids
