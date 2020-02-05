/*
 * Copyright (c) 2019, Digital Asset (Switzerland) GmbH and/or its affiliates. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package jsonapi;

@SuppressWarnings("PMD")
public class ExerciseChoiceData {
  private final String templateId;
  private final String contractId;
  private final String choice;

  private final Object argument;

  public ExerciseChoiceData(String templateId, String contractId, String choice, Object argument) {
    this.templateId = templateId;
    this.contractId = contractId;
    this.choice = choice;
    this.argument = argument;
  }

  public String getTemplateId() {
    return templateId;
  }

  public String getContractId() {
    return contractId;
  }

  public String getChoice() {
    return choice;
  }

  public Object getArgument() {
    return argument;
  }

  @Override
  public String toString() {
    return "ExerciseChoiceData{"
        + "templateId='"
        + templateId
        + '\''
        + ", contractId='"
        + contractId
        + '\''
        + ", choice='"
        + choice
        + '\''
        + ", argument="
        + argument
        + '}';
  }
}
