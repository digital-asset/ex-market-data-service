/*
 * Copyright (c) 2019, Digital Asset (Switzerland) GmbH and/or its affiliates. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package com.digitalasset.jsonapi;

import com.digitalasset.refapps.utils.Eventually;
import com.digitalasset.refapps.utils.Eventually.TimeoutExceeded;
import com.digitalasset.refapps.utils.EventuallyBuilder;
import java.io.IOException;
import java.net.URI;
import java.time.Duration;
import org.apache.http.HttpResponse;
import org.apache.http.client.fluent.Request;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Utils {

  private static final Logger log = LoggerFactory.getLogger("JsonAPI Util");
  private static final Duration TIMEOUT = Duration.ofSeconds(180);
  private static final Eventually EVENTUALLY =
      new EventuallyBuilder().setTimeout(TIMEOUT).setInterval(Duration.ofSeconds(1)).create();

  public static void waitForJsonApi(URI uri) throws Exception {
    log.info("Waiting for JSON API at: {}", uri);
    try {
      EVENTUALLY.execute(() -> connectTo(uri));
    } catch (TimeoutExceeded e) {
      throw notAvailableWithinTimeout();
    }
    log.info("JSON API available.");
  }

  private static boolean connectTo(URI uri) {
    log.info("Waiting for JSON API...");
    try {
      HttpResponse response = Request.Options(uri).execute().returnResponse();
      return serverHasResponded(response);
    } catch (IOException e) {
      log.debug("Could not reach JSON API: {}", e.getMessage());
      return false;
    }
  }

  private static Exception notAvailableWithinTimeout() {
    return new Exception("JSON API not available within " + TIMEOUT.toMillis() + "ms timout");
  }

  private static boolean serverHasResponded(HttpResponse response) {
    log.info(Integer.toString(response.getStatusLine().getStatusCode()));
    return response.getStatusLine().getStatusCode() < 500;
  }
}
