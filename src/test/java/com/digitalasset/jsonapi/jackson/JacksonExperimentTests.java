/*
 * Copyright (c) 2019, Digital Asset (Switzerland) GmbH and/or its affiliates. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */

package com.digitalasset.jsonapi.jackson;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import com.fasterxml.jackson.annotation.JsonCreator.Mode;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;
import com.google.gson.Gson;
import java.util.Objects;
import org.junit.Test;

public class JacksonExperimentTests {

  @Test
  public void jackson_with_property_support() throws JsonProcessingException {
    Person person = new Person("John Doe", 42);
    ObjectMapper mapper = new ObjectMapper();
    mapper.registerModule(new ParameterNamesModule(Mode.PROPERTIES));
    String json = mapper.writeValueAsString(person);
    assertThat(mapper.readValue(json, Person.class), is(person));
  }

  @Test
  public void gson_works_out_of_the_box() {
    Person person = new Person("John Doe", 42);
    Gson mapper = new Gson();
    String json = mapper.toJson(person);
    assertThat(mapper.fromJson(json, Person.class), is(person));
  }

  private static class Person {
    public final String name;
    public final int age;

    public Person(String name, int age) {
      this.name = name;
      this.age = age;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;
      Person person = (Person) o;
      return age == person.age && Objects.equals(name, person.name);
    }

    @Override
    public int hashCode() {
      return Objects.hash(name, age);
    }
  }
}
