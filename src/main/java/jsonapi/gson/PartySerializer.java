package jsonapi.gson;

import com.daml.ledger.javaapi.data.Party;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import java.lang.reflect.Type;

public class PartySerializer implements JsonSerializer<Party> {

  @Override
  public JsonElement serialize(Party party, Type type, JsonSerializationContext context) {
    return new JsonPrimitive(party.getValue());
  }
}
