/*
 * Copyright (c) 2019, Digital Asset (Switzerland) GmbH and/or its affiliates. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package jsonapi.json;

import da.timeservice.timeservice.CurrentTime;

import java.io.InputStream;
import java.io.InputStreamReader;

public class SampleCurrentTimeDeserializer extends SampleJsonDeserializerBase {

  @Override
  public CurrentTime apply(InputStream s) {
    return json.fromJson(new InputStreamReader(s), CurrentTime.class);
  }
}
