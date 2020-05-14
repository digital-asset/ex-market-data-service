#!/usr/bin/env bash
#
# Copyright (c) 2019, Digital Asset (Switzerland) GmbH and/or its affiliates. All rights reserved.
# SPDX-License-Identifier: Apache-2.0
#

# Do not launch this script directly, intended to be called from other launcher scripts.

set -e

cleanup() {
    pids=$(jobs -p)
    echo Killing $pids
    [ -n "$pids" ] && kill $pids
}

trap "cleanup" INT QUIT TERM

if [ $# -lt 2 ]; then
    echo "${0} SANDBOX_HOST SANDBOX_PORT [DAR_FILE]"
    exit 1
fi

SANDBOX_HOST="${1}"
SANDBOX_PORT="${2}"
DAR_FILE="${3:-/home/daml/market-data-service.dar}"

daml trigger \
    --wall-clock-time \
    --dar "${DAR_FILE}" \
    --trigger-name DA.RefApps.MarketDataService.Triggers.Enrichment:republishObservationTrigger \
    --ledger-host ${SANDBOX_HOST} \
    --ledger-port ${SANDBOX_PORT} \
    --ledger-party MarketDataVendor &

daml trigger \
    --wall-clock-time \
    --dar "${DAR_FILE}" \
    --trigger-name DA.RefApps.MarketDataService.Triggers.Enrichment:enrichCleanPriceWithAccrualTrigger \
    --ledger-host ${SANDBOX_HOST} \
    --ledger-port ${SANDBOX_PORT} \
    --ledger-party AnalyticsVendor &
