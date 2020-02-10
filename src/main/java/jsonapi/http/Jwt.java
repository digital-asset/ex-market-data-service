/*
 * Copyright (c) 2019, Digital Asset (Switzerland) GmbH and/or its affiliates. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package jsonapi.http;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import java.security.Key;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Jwt {

  public static String createToken(String ledgerId, String applicationId, List<String> parties) {
    Key key = Keys.secretKeyFor(SignatureAlgorithm.HS256);
    Map<String, Object> claim = new HashMap<>();
    claim.put("ledgerId", ledgerId);
    claim.put("applicationId", applicationId);
    claim.put("actAs", parties);
    Map<String, Object> claims = Collections.singletonMap("https://daml.com/ledger-api", claim);
    return Jwts.builder().setClaims(claims).signWith(key).compact();
  }
}
