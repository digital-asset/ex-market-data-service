/*
 * Copyright (c) 2019, Digital Asset (Switzerland) GmbH and/or its affiliates. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */

package com.digitalasset.refapps.marketdataservice;

import com.digitalasset.jsonapi.JsonLedgerClient;
import com.digitalasset.jsonapi.LedgerClient;
import com.digitalasset.jsonapi.apache.ApacheHttpClient;
import com.digitalasset.jsonapi.gson.GsonDeserializer;
import com.digitalasset.jsonapi.gson.GsonSerializer;
import com.digitalasset.jsonapi.http.Api;
import com.digitalasset.jsonapi.http.HttpClient;
import com.digitalasset.jsonapi.http.HttpResponse;
import com.digitalasset.jsonapi.http.Jwt;
import com.digitalasset.jsonapi.http.WebSocketClient;
import com.digitalasset.jsonapi.http.WebSocketResponse;
import com.digitalasset.jsonapi.json.JsonDeserializer;
import com.digitalasset.jsonapi.json.JsonSerializer;
import com.digitalasset.jsonapi.tyrus.TyrusWebSocketClient;
import com.digitalasset.refapps.marketdataservice.utils.AppParties;
import com.digitalasset.refapps.marketdataservice.utils.CliOptions;
import java.time.Duration;
import java.util.Arrays;

public class AppConfig {

  private final String jsonApiHost;
  private final int jsonApiPort;
  private final String ledgerId;
  private final String applicationId;
  private final AppParties appParties;
  private final Duration systemPeriodTime;

  private AppConfig(
      String jsonApiHost,
      int jsonApiPort,
      String ledgerId,
      String applicationId,
      AppParties appParties,
      Duration systemPeriodTime) {
    this.jsonApiHost = jsonApiHost;
    this.jsonApiPort = jsonApiPort;
    this.ledgerId = ledgerId;
    this.applicationId = applicationId;
    this.appParties = appParties;
    this.systemPeriodTime = systemPeriodTime;
  }

  public LedgerClient getClientFor(String... parties) {
    return new JsonLedgerClient(
        getHttpClientFor(parties), getWebSocketClientFor(parties), getApi());
  }

  private HttpClient getHttpClientFor(String... parties) {
    return new ApacheHttpClient(
        getHttpResponseDeserializer(), getJsonSerializer(), getTokenFor(parties));
  }

  private WebSocketClient getWebSocketClientFor(String... parties) {
    return new TyrusWebSocketClient(
        getWebSocketResponseDeserializer(), getJsonSerializer(), getTokenFor(parties));
  }

  private Api getApi() {
    return new Api(getJsonApiHost(), getJsonApiPort());
  }

  private JsonDeserializer<HttpResponse> getHttpResponseDeserializer() {
    return new GsonDeserializer().getHttpResponseDeserializer();
  }

  private JsonDeserializer<WebSocketResponse> getWebSocketResponseDeserializer() {
    return new GsonDeserializer().getWebSocketResponseDeserializer();
  }

  private JsonSerializer getJsonSerializer() {
    return new GsonSerializer();
  }

  private String getTokenFor(String... parties) {
    return Jwt.createToken(getLedgerId(), getApplicationId(), Arrays.asList(parties));
  }

  private String getJsonApiHost() {
    return jsonApiHost;
  }

  private int getJsonApiPort() {
    return jsonApiPort;
  }

  private String getLedgerId() {
    return ledgerId;
  }

  private String getApplicationId() {
    return applicationId;
  }

  public AppParties getAppParties() {
    return appParties;
  }

  public Duration getSystemPeriodTime() {
    return systemPeriodTime;
  }

  public static AppConfigBuilder builder() {
    return new AppConfigBuilder();
  }

  public static class AppConfigBuilder {

    private String jsonApiHost;
    private int jsonApiPort;
    private String ledgerId;
    private String applicationId;
    private AppParties appParties;
    private Duration systemPeriodTime;

    public AppConfigBuilder useCliOptions(CliOptions cliOptions) {
      this.jsonApiHost = cliOptions.getJsonApiHost();
      this.jsonApiPort = cliOptions.getJsonApiPort();
      this.appParties = new AppParties(cliOptions.getParties());
      this.ledgerId = cliOptions.getLedgerId();
      return this;
    }

    public AppConfigBuilder setApplicationId(String applicationId) {
      this.applicationId = applicationId;
      return this;
    }

    public AppConfigBuilder setSystemPeriodTime(Duration systemPeriodTime) {
      this.systemPeriodTime = systemPeriodTime;
      return this;
    }

    public AppConfig create() {
      return new AppConfig(
          jsonApiHost, jsonApiPort, ledgerId, applicationId, appParties, systemPeriodTime);
    }
  }
}
