/*
 * Copyright (c) 2019, Digital Asset (Switzerland) GmbH and/or its affiliates. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package com.digitalasset.jsonapi;

@SuppressWarnings("PMD")
public class Contract<T> {

  private final String contractId;
  private final T contract;

  public Contract(String contractId, T contract) {
    this.contractId = contractId;
    this.contract = contract;
  }

  public String getContractId() {
    return contractId;
  }

  public T getContract() {
    return contract;
  }
}
