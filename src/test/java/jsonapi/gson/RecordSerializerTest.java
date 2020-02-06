package jsonapi.gson;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import com.daml.ledger.javaapi.data.ExerciseCommand;
import com.daml.ledger.javaapi.data.Record;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import da.timeservice.timeservice.CurrentTime.ContractId;
import org.junit.Test;

public class RecordSerializerTest {

  @Test
  public void toJson() {
    ContractId contractId = new ContractId("#0:0");
    ExerciseCommand command = contractId.exerciseCurrentTime_AddObserver("John Doe");

    Gson json =
        new GsonBuilder().registerTypeAdapter(Record.class, new RecordSerializer()).create();
    String result = json.toJson(command.getChoiceArgument());

    assertThat(result, is("{\"newObserver\":\"John Doe\"}"));
  }
}
