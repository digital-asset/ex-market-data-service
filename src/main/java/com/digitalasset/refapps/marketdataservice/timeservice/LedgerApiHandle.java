package com.digitalasset.refapps.marketdataservice.timeservice;

import com.daml.ledger.javaapi.data.Command;
import com.daml.ledger.javaapi.data.Record;
import jsonapi.ContractQuery;

import java.util.List;

public interface LedgerApiHandle {
    class Contract {
        private String contractId;
        private Record arguments;

        public Contract(String contractId, Record arguments) {
            this.arguments = arguments;
            this.contractId = contractId;
        }

        public String getContractId() {
            return contractId;
        }

        public Record getArguments() {
            return arguments;
        }
    }

    void submitCommand(Command command);

    List<Contract> getCreatedEvents(ContractQuery filter);

    String getOperatingParty();
}
