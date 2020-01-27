#!/usr/bin/env bash
#
# Copyright (c) 2019, Digital Asset (Switzerland) GmbH and/or its affiliates. All rights reserved.
# SPDX-License-Identifier: Apache-2.0
#

export _JAVA_OPTIONS="-Xmx250m"

source lib/startTriggers_common.sh

sleep 2
pids=$(jobs -p)
echo Waiting for $pids
wait $pids
