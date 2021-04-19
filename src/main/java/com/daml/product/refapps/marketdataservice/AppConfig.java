/*
 * Copyright (c) 2019, Digital Asset (Switzerland) GmbH and/or its affiliates. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */

package com.daml.product.refapps.marketdataservice;

import com.daml.extensions.jsonapi.JsonLedgerClient;
import com.daml.extensions.jsonapi.LedgerClient;
import com.daml.extensions.jsonapi.apache.ApacheHttpClient;
import com.daml.extensions.jsonapi.gson.GsonDeserializer;
import com.daml.extensions.jsonapi.gson.GsonSerializer;
import com.daml.extensions.jsonapi.http.Api;
import com.daml.extensions.jsonapi.http.HttpClient;
import com.daml.extensions.jsonapi.http.HttpResponse;
import com.daml.extensions.jsonapi.http.Jwt;
import com.daml.extensions.jsonapi.http.WebSocketClient;
import com.daml.extensions.jsonapi.http.WebSocketResponse;
import com.daml.extensions.jsonapi.json.JsonDeserializer;
import com.daml.extensions.jsonapi.tyrus.TyrusWebSocketClient;
import com.daml.product.refapps.marketdataservice.utils.AppParties;
import com.daml.product.refapps.marketdataservice.utils.CliOptions;
import java.net.URI;
import java.time.Duration;
import java.util.Arrays;

public class AppConfig {

  private final String jsonApiHost;
  private final int jsonApiPort;
  private final String ledgerId;
  private final String applicationId;
  private final AppParties appParties;
  private final Duration systemPeriodTime;
  private final GsonSerializer jsonSerializer = new GsonSerializer();
  private final JsonDeserializer<WebSocketResponse> webSocketResponseDeserializer;
  private final JsonDeserializer<HttpResponse> httpResponseDeserializer;

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
    GsonDeserializer deserializer = new GsonDeserializer();
    this.webSocketResponseDeserializer = deserializer.getWebSocketResponseDeserializer();
    this.httpResponseDeserializer = deserializer.getHttpResponseDeserializer();
  }

  public LedgerClient getClientFor(String... parties) {
    return new JsonLedgerClient(
        getHttpClientFor(parties), getWebSocketClientFor(parties), getApi());
  }

  private HttpClient getHttpClientFor(String... parties) {
    return new ApacheHttpClient(httpResponseDeserializer, jsonSerializer, getTokenFor(parties));
  }

  private WebSocketClient getWebSocketClientFor(String... parties) {
    return new TyrusWebSocketClient(
        webSocketResponseDeserializer, jsonSerializer, getTokenFor(parties));
  }

  private Api getApi() {
    return new Api(jsonApiHost, jsonApiPort);
  }

  private String getTokenFor(String... parties) {
    return Jwt.createToken(ledgerId, applicationId, Arrays.asList(parties));
  }

  public AppParties getAppParties() {
    return appParties;
  }

  public Duration getSystemPeriodTime() {
    return systemPeriodTime;
  }

  public URI getJsonApiUrl() {
    return URI.create(String.format("%s://%s:%d", "http", jsonApiHost, jsonApiPort));
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

    public AppConfigBuilder setLedgerId(String ledgerId) {
      this.ledgerId = ledgerId;
      return this;
    }

    public AppConfigBuilder setJsonApiHost(String jsonApiHost) {
      this.jsonApiHost = jsonApiHost;
      return this;
    }

    public AppConfigBuilder setJsonApiPort(int jsonApiPort) {
      this.jsonApiPort = jsonApiPort;
      return this;
    }

    public AppConfigBuilder setAppParties(String... appParties) {
      this.appParties = new AppParties(appParties);
      return this;
    }

    public AppConfig create() {
      require(nonNullOrBlank(jsonApiHost), "JSON API host cannot be null or blank.");
      require(nonNullOrBlank(ledgerId), "Ledger ID cannot be null or blank.");
      require(nonNullOrBlank(applicationId), "Application ID cannot be null or blank.");
      return new AppConfig(
          jsonApiHost, jsonApiPort, ledgerId, applicationId, appParties, systemPeriodTime);
    }

    private void require(boolean condition, String message) {
      if (!condition) {
        throw new IllegalArgumentException(message);
      }
    }

    private boolean nonNullOrBlank(String s) {
      return s != null && !s.trim().isEmpty();
    }
  }
}
