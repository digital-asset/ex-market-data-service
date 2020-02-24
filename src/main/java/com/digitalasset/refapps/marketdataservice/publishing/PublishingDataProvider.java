/*
 * Copyright (c) 2019, Digital Asset (Switzerland) GmbH and/or its affiliates. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package com.digitalasset.refapps.marketdataservice.publishing;

import com.daml.ledger.javaapi.data.Identifier;
import da.refapps.marketdataservice.marketdatatypes.ObservationReference;
import da.refapps.marketdataservice.marketdatatypes.ObservationValue;
import java.time.Instant;
import java.util.Optional;
import java.util.Set;
import jsonapi.ActiveContractSet;

public interface PublishingDataProvider {
  Set<Identifier> getUsedTemplates();

  Optional<ObservationValue> getObservation(
      ActiveContractSet activeContractSet, ObservationReference reference, Instant time);
}
