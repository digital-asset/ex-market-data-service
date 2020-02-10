package jsonapi.gson;

import com.daml.ledger.javaapi.data.CreateCommand;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import java.lang.reflect.Type;

public class CreateCommandSerializer implements JsonSerializer<CreateCommand> {
    @Override
    public JsonElement serialize(CreateCommand createCommand, Type type, JsonSerializationContext jsonSerializationContext) {
        JsonObject json = new JsonObject();
        json.add("templateId", jsonSerializationContext.serialize(createCommand.getTemplateId()));
        json.add("payload", jsonSerializationContext.serialize(createCommand.getCreateArguments()));
        return json;
    }
}
