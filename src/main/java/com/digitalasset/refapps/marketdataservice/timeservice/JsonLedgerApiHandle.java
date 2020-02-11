package com.digitalasset.refapps.marketdataservice.timeservice;

import com.daml.ledger.javaapi.data.Command;
import jsonapi.ContractQuery;
import jsonapi.JsonLedgerClient;
import jsonapi.apache.ApacheHttpClient;
import jsonapi.http.Api;
import jsonapi.http.HttpResponse;
import jsonapi.http.Jwt;
import jsonapi.http.WebSocketResponse;
import jsonapi.json.JsonDeserializer;
import jsonapi.json.JsonSerializer;
import jsonapi.tyrus.TyrusWebSocketClient;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class JsonLedgerApiHandle implements LedgerApiHandle {
    private final JsonLedgerClient ledgerClient;
    private String party;

    public JsonLedgerApiHandle(String party, String ledgerId, String applicationId, JsonDeserializer<HttpResponse> httpResponseDeserializer, JsonSerializer jsonSerializer, JsonDeserializer<WebSocketResponse> webSocketResponseDeserializer) {
        this.party = party;
        String jwt = Jwt.createToken(ledgerId, applicationId, Collections.singletonList(party));
        ApacheHttpClient httpClient =
                new ApacheHttpClient(httpResponseDeserializer, jsonSerializer, jwt);
        TyrusWebSocketClient webSocketClient =
                new TyrusWebSocketClient(webSocketResponseDeserializer, jsonSerializer, jwt);
        Api api = new Api("localhost", 7575);

        ledgerClient =
                new JsonLedgerClient(httpClient, webSocketClient, jsonSerializer, api);
    }

    @Override
    public void submitCommand(Command command) {
        command.asCreateCommand().ifPresent(ledgerClient::create);
        command.asExerciseCommand().ifPresent(ledgerClient::exerciseChoice);
    }

    @Override
    public List<Contract> getCreatedEvents(ContractQuery filter) {
        return StreamSupport
                .stream(ledgerClient.getActiveContracts().getActiveContracts().spliterator(), false)
                .map(activeContract -> new Contract(activeContract.getContractId(), activeContract.getTemplate().create().getCreateArguments()))
                .collect(Collectors.toList());
    }

    @Override
    public String getOperatingParty() {
        return party;
    }
}
