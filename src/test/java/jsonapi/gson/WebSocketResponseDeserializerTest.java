package jsonapi.gson;

import com.google.common.collect.Iterables;
import da.refapps.marketdataservice.roles.OperatorRole;
import jsonapi.events.CreatedEvent;
import jsonapi.events.Event;
import jsonapi.http.WebSocketResponse;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.*;

public class WebSocketResponseDeserializerTest {

    @Test
    public void deserializeWebSocketResponse() {
        String tid = new GsonSerializer().apply(OperatorRole.TEMPLATE_ID);
        String serializedWebSocketResponse = "[ \n" +
                "   { \n" +
                "      \"created\":{ \n" +
                "         \"observers\":[ \n" +
                "\n" +
                "         ],\n" +
                "         \"agreementText\":\"\",\n" +
                "         \"payload\":{ \n" +
                "            \"operator\":\"Operator\",\n" +
                "            \"currentTime\":\"2020-02-12T16:43:44.571076Z\",\n" +
                "            \"observers\":[ \n" +
                "\n" +
                "            ]\n" +
                "         },\n" +
                "         \"signatories\":[ \n" +
                "            \"Operator\"\n" +
                "         ],\n" +
                "         \"key\":\"Operator\",\n" +
                "         \"contractId\":\"#11:0\",\n" +
                "         \"templateId\":\"b4eb9b86bb78db2acde90edf0a03d96e5d65cc7a7cc422f23b6d98a286e07c09:DA.TimeService.TimeService:CurrentTime\"\n" +
                "      }\n" +
                "   }\n" +
                "]";
        WebSocketResponse deserializedWebSocketResponse = GsonRegisteredAllDeserializers.gson().fromJson(serializedWebSocketResponse, WebSocketResponse.class);
        assertEquals(deserializedWebSocketResponse.getEvents().size(), 1);
        Event event = Iterables.getOnlyElement(deserializedWebSocketResponse.getEvents());
        assertThat(event, instanceOf(CreatedEvent.class));
    }

}
