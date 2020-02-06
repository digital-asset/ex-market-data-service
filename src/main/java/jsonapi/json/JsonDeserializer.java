/*
 * Copyright (c) 2019, Digital Asset (Switzerland) GmbH and/or its affiliates. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package jsonapi.json;

import java.io.InputStream;

public interface JsonDeserializer<T> {

  T apply(InputStream s);
}
