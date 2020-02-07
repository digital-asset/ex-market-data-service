package jsonapi.gson;

import com.daml.ledger.javaapi.data.Identifier;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import java.lang.reflect.Type;

public class IdentifierSerializer implements JsonSerializer<Identifier> {

  @Override
  public JsonElement serialize(
      Identifier identifier, Type type, JsonSerializationContext jsonSerializationContext) {
    return new JsonPrimitive(identifier.getModuleName() + ":" + identifier.getEntityName());
  }
}
