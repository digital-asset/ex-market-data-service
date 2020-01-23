/*
 * Copyright (c) 2019, Digital Asset (Switzerland) GmbH and/or its affiliates. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package com.digitalasset.refapps.marketdataservice.publishing;

import com.daml.ledger.javaapi.data.Identifier;
import com.daml.ledger.javaapi.data.Template;
import com.daml.ledger.rxjava.components.LedgerViewFlowable;
import da.refapps.marketdataservice.marketdatatypes.ObservationReference;
import da.refapps.marketdataservice.marketdatatypes.ObservationValue;
import java.time.Instant;
import java.util.Optional;
import java.util.Set;

public interface PublishingDataProvider {
  Set<Identifier> getUsedTemplates();

  Optional<ObservationValue> getObservation(
      LedgerViewFlowable.LedgerView<Template> ledgerView,
      ObservationReference reference,
      Instant time);
}
