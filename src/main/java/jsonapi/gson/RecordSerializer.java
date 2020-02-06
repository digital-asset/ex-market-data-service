package jsonapi.gson;

import com.daml.ledger.javaapi.data.Record;
import com.daml.ledger.javaapi.data.Record.Field;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import java.lang.reflect.Type;

public class RecordSerializer implements JsonSerializer<Record> {

  @Override
  public JsonElement serialize(
      Record record, Type type, JsonSerializationContext jsonSerializationContext) {
    JsonObject json = new JsonObject();
    // TODO: Implement properly.
    for (Field field : record.getFields()) {
      json.addProperty(field.getLabel().get(), field.getValue().asParty().get().getValue());
    }
    return json;
  }
}
