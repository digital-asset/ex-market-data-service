/*
 * Copyright (c) 2019, Digital Asset (Switzerland) GmbH and/or its affiliates. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
import { DamlLfValue } from '@da/ui-core';

export const version = {
    schema: 'navigator-config',
    major: 2,
    minor: 0
};


// --- Creating views --------------------------------------------------------------------

function publisherProvidedView(publisher) {
    return createTab("DataStreams: Provided streams", "DataStream@",
        [
            createCol("reference", "Market", 80, r => (r.reference || r.observation.label).market),
            createCol("reference", "Instrument", 80, r => (r.reference || r.observation.label).instrumentId),
            createCol("reference", "Maturity date", 80, r => (r.reference || r.observation.label).maturityDate),
            createCol("consumers", "Consumers", 80, r => showConsumers(r.consumers)),
            createCol("started", "Started", 80, r => (r.observation === undefined) ? "No" : "Yes"),
        ],
        {
            field: "argument.publisher.party",
            value: publisher
        }
    );
}

function publisherDataView(publisher) {
    return createTab("DataStreams: Sent data", ":Publication@",
        [
            createIdCol(),
            createCol("published", "Published", 80, r => r.published),
            createCol("consumer"),
            createCol("reference", "Market", 80, r => r.observation.label.market),
            createCol("reference", "Instrument", 80, r => r.observation.label.instrumentId),
            createCol("reference", "Maturity date", 80, r => r.observation.label.maturityDate),
            createCol("time", "Time", 80, r => r.observation.time),
            createCol("observation", "Observation", 80, r => showObservationValue(r.observation))],
        {
            field: "argument.publisher.party",
            value: publisher
        }
    );
}

function consumerView(consumer) {
    return createTab("DataStreams: Received data", ":Publication@",
        [
            createIdCol(),
            createCol("published", "Published", 80, r => r.published),
            createCol("publisher"),
            createCol("reference", "Market", 80, r => r.observation.label.market),
            createCol("reference", "Instrument", 80, r => r.observation.label.instrumentId),
            createCol("reference", "Maturity date", 80, r => r.observation.label.maturityDate),
            createCol("time", "Time", 80, r => r.observation.time),
            createCol("observation", "Observation", 80, r => showObservationValue(r.observation))],
        {
            field: "argument.consumer.party",
            value: consumer
        }
    );
}

function complaintsNonPerformanceView(consumer) {
    return createTab("Complaints: NonPerformance", "NonPerformance",
        [
            createCol("claimed", "Claimed", 80, r => r.claimed),
            createCol("starting", "Starting", 80, r => r.licenseData.starting),
        ],
        {
            field: "argument.licenseData.consumer.party",
            value: consumer
        });
}

function complaintsStaleDataView(consumer) {
    return createTab("Complaints: StaleData", "StalePublication",
        [
            createCol("claimed", "Claimed", 80, r => r.claimed),
            createCol("published", "Published", 80, r => r.publication.published),
            createCol("stale", "Stale allowed", 80, r => prettyMs(r.licenseData.stale.microseconds)),
        ],
        {
            field: "argument.licenseData.consumer.party",
            value: consumer
        });
}

const timeManagerView = createTab("Time Management", "TimeManager",
    [
        createCol("operator", "Operator", 80, r => r.operator)
    ]
);

const timeConfigurationView = createTab("Time Configuration", "TimeConfiguration",
    [
        createCol("operator", "Operator", 80, r => r.operator),
        createCol("modelPeriodTime", "ModelPeriodTime", 80, r => prettyMs(r.modelPeriodTime.microseconds)),
        createCol("isRunning", "Running", 80, r => {
            return r.isRunning ? "Running" : "Stopped";
        })
    ]
);

const currentTimeView = createTab("Current Time", "CurrentTime",
    [
        createCol("time", "Time", 80, r => r.currentTime)
    ]
);

const dataSourceView = createTab("Data Sources", "DataSource",
    [
        createCol("owner", "Owner", 80, r => r.owner),
        createCol("operator", "Operator", 80, r => r.operator),
        createCol("reference", "Market", 80, r => r.reference.market),
        createCol("reference", "Instrument", 80, r => r.reference.instrumentId),
        createCol("reference", "Maturity date", 80, r => r.reference.maturityDate)
    ]
);

const dataProviderView = createTab("Data Provider Role", "MarketDataProviderRole",
    [
        createCol("operator", "Operator", 80, r => r.operator),
        createCol("marketDataProvider", "Market Data Provider", 80, r => r.marketDataProvider)
    ]
);

function relationships(party) {
    return createTab("Relationships", "PublisherConsumerRelationship",
        [
            createCol("myrole", "My Role", 20, r => {
                if (r.publisher.party === party) {
                    return "Publisher";
                } else if (r.consumer.party === party) {
                    return "Consumer";
                } else {
                    return "Unknown role";
                }
            }),
            createCol("counterparty", "Counterparty", 80, r => {
                if (r.publisher.party === party) {
                    return `${r.consumer.party} (Consumer)`;
                } else if (r.consumer.party === party) {
                    return `${r.publisher.party} (Publisher)`;
                } else {
                    return "Unknown counterparty";
                }
            }),
            createCol("licenses", "Licenses", 80, r => {
                if (r.publisher.party === party) {
                    return "-";
                } else if (r.consumer.party === party) {
                    return "Click to request stream / renew license...";
                } else {
                    return "-";
                }
            }),
        ]
    );
}

function streamRequestsView(party) {
    return createTab("Stream Requests", "DataStreamRequest",
        [
            createCol("type", "In/Out", 80, r => {
                if (r.publisher.party === party) {
                    return "Received";
                } else if (r.consumer.party === party) {
                    return "Sent";
                } else {
                    return "-";
                }
            }),
            createCol("counterparty", "Counterparty", 100, r => {
                if (r.publisher.party === party) {
                    return r.consumer.party + " (Consumer)";
                } else if (r.consumer.party === party) {
                    return r.publisher.party + " (Publisher)";
                } else {
                    return "Unknown counterparty";
                }
            }),
            createCol("reference", "Market", 80, r => r.reference.market),
            createCol("reference", "Instrument", 80, r => r.reference.instrumentId),
            createCol("reference", "Maturity date", 80, r => r.reference.maturityDate),
            createCol("expiry", "Expiry", 100, r => prettyDate(r.ending)),
        ]
    );
}

function licenseProposalView(party) {
    return createTab("License Proposals", "DataLicenseProposal",
        [
            createCol("type", "In/Out", 80, r => {
                if (r.publisher.party === party) {
                    return "Sent";
                } else if (r.consumer.party === party) {
                    return "Received";
                } else {
                    return "-";
                }
            }),
            createCol("counterparty", "Counterparty", 100, r => {
                if (r.publisher.party === party) {
                    return r.consumer.party + " (Consumer)";
                } else if (r.consumer.party === party) {
                    return r.publisher.party + " (Publisher)";
                } else {
                    return "Unknown counterparty";
                }
            }),
            createCol("reference", "Market", 80, r => r.reference.market),
            createCol("reference", "Instrument", 80, r => r.reference.instrumentId),
            createCol("reference", "Maturity date", 80, r => r.reference.maturityDate),
            createCol("price", "Price", 80, r => r.price),
            createCol("expiry", "Expiry", 100, r => prettyDate(r.ending)),
        ]
    );
}

function licenseView(party) {
    return createTab("Licenses", "License@",
        [
            createCol("type", "Description", 200, r => {
                const publisherParty = r.licenseData.publisher.party;
                const consumerParty = r.licenseData.consumer.party
                if (publisherParty === party) {
                    return "Approved for consumer '" + consumerParty + "'";
                } else if (consumerParty === party) {
                    return "Received from publisher '" + publisherParty + "'";
                } else {
                    return "-";
                }
            }),
            createCol("reference", "Market", 80, r => r.licenseData.reference.market),
            createCol("reference", "Instrument", 80, r => r.licenseData.reference.instrumentId),
            createCol("reference", "Maturity date", 60, r => r.licenseData.reference.maturityDate),
            createCol("price", "Price", 80, r => r.licenseData.price),
            createCol("startDate", "Start date", 100, r => prettyDate(r.licenseData.starting)),
            createCol("expiry", "Expiry", 100, r => prettyDate(r.licenseData.ending)),
            createCol("live", "Status", 80, r => {
                if (r.began != null) {
                    return "Live"
                } else {
                    return "-";
                }
            })
        ]
    );
}

// --- Assigning vievs to parties --------------------------------------------------------------------

export const customViews = (userId, party, role) => {
    function partyIs(partyName) {
        return party === partyName || userId === partyName;
    }
    if (partyIs('Operator')) {
        return {
            timeManagerView,
            timeConfigurationView,
            currentTimeView
        };
    }
    const publisherDataViewInstance = publisherDataView(party)
    const publisherProvidedViewInstance = publisherProvidedView(party)
    const relationshipsViewInstance = relationships(party);
    const streamRequestsViewInstance = streamRequestsView(party);
    const licenseProposalViewInstance = licenseProposalView(party);
    const licenseViewInstance = licenseView(party);
    const complaintsNonPerformanceViewInstance = complaintsNonPerformanceView(party);
    const complaintsStaleDataViewInstance = complaintsStaleDataView(party);
    if (partyIs('MarketDataProvider1')) {
        return {
            relationshipsViewInstance,
            dataProviderView,
            publisherProvidedViewInstance,
            publisherDataViewInstance,
            streamRequestsViewInstance,
            licenseProposalViewInstance,
            licenseViewInstance,
            dataSourceView,
            currentTimeView
        };
    }

    if (partyIs('MarketDataProvider2')) {
        return {
            relationshipsViewInstance,
            dataProviderView,
            publisherProvidedViewInstance,
            publisherDataViewInstance,
            streamRequestsViewInstance,
            licenseProposalViewInstance,
            licenseViewInstance,
            dataSourceView,
            currentTimeView
        };
    }
    const consumerViewInstance = consumerView(party)
    if (partyIs('AnalyticsVendor')) {
        return {
            relationshipsViewInstance,
            consumerViewInstance,
            publisherProvidedViewInstance,
            publisherDataViewInstance,
            streamRequestsViewInstance,
            licenseProposalViewInstance,
            licenseViewInstance,
            currentTimeView
        };
    }

    if (partyIs('MarketDataVendor')) {
        return {
            relationshipsViewInstance,
            consumerViewInstance,
            publisherProvidedViewInstance,
            publisherDataViewInstance,
            streamRequestsViewInstance,
            licenseProposalViewInstance,
            licenseViewInstance,
            currentTimeView
        };
    }

    if (partyIs('EndUser')) {
        return {
            relationshipsViewInstance,
            consumerViewInstance,
            streamRequestsViewInstance,
            licenseProposalViewInstance,
            licenseViewInstance,
            complaintsNonPerformanceViewInstance,
            complaintsStaleDataViewInstance,
            currentTimeView
        };
    } else {
        return {
        };
    }
};


// --- Helpers --------------------------------------------------------------------

/**
 title, width and proj are optional

 if proj is null and key is "id" then it will default to the contract id
 if proj is null and key is not "id" then it will default to stringified single or array value of rowData.key
*/
function createCol(key, title = toTitle(key), width = 80, proj) {
    return {
        key: key,
        title: title,
        createCell: ({ rowData }) => {
            return {
                type: "text",
                value: valueFunction(rowData, key, proj)
            }
        },
        sortable: true,
        width: width,
        weight: 0,
        alignment: "left"
    };
}

function createIdCol() {
    return createCol("id", "Contract ID", 60);
}

function createTab(name, templateId, columns, additionalFilter) {
    let filter;
    if (additionalFilter == null) {
        filter =
            [
                {
                    field: "template.id",
                    value: templateId
                }
            ]
    } else {
        filter =
            [
                {
                    field: "template.id",
                    value: templateId
                },
                additionalFilter
            ]
    }
    return {
        type: "table-view",
        title: name,
        source: {
            type: "contracts",
            filter: filter,
            search: "",
            sort: [
                {
                    field: "id",
                    direction: "ASCENDING"
                }
            ]
        },
        columns: columns
    };
}

/**
 * A convenient function that unpacks a wrapped value. If the value is not
 * wrapped this is a no-op.
 * @example
 * unpack({unpack: 12.34}); // returns 12.34
 * unpack(12.34); // returns 12.34
 */
function unpack(value) {
    return (value && value.unpack)
        ? value.unpack
        : value;
}

/**
 * Will return 0 for empty string or strings with whitespace. This is due
 * to the documented behaviour of Number.
 */
function formatIfNum(val) {
    const n = Number(val);
    if (Number.isNaN(n)) return val;
    else return n.toLocaleString();
}

function valueFunction(rowData, key, proj) {
    return (
        proj == null
            ?
            (
                Array.isArray(DamlLfValue.toJSON(rowData.argument)[key])
                    ?
                    DamlLfValue.toJSON(rowData.argument)[key].join(", ")
                    :
                    (
                        key === "id"
                            ?
                            rowData.id
                            :
                            (
                                'party' in DamlLfValue.toJSON(rowData.argument)[key]
                                    ?
                                    DamlLfValue.toJSON(rowData.argument)[key].party
                                    :
                                    formatIfNum(DamlLfValue.toJSON(rowData.argument)[key])
                            )
                    )
            )
            :
            formatIfNum(unpack(proj(DamlLfValue.toJSON(rowData.argument)))));
}

// inserts spaces into the usually camel-case key
// e.g. "assetISINCode" -> "Asset ISIN Code"
function toTitle(key) {
    const spaced = key.replace(/([^A-Z])([A-Z])/g, '$1 $2').replace(/([A-Z])([A-Z][^A-Z])/g, '$1 $2');
    return spaced[0].toUpperCase() + spaced.substr(1)
}

function showConsumers(xs) {
    return xs.map(showParty).join(", ");
}

function showParty(member) {
    return member.party;
}

function showObservationValue(observation) {
    if (observation.value.CleanPrice) {
        return `Clean price: ${observation.value.CleanPrice.clean}`;
    } else if (observation.value.EnrichedCleanDirtyPrice) {
        return `Dirty price: ${observation.value.EnrichedCleanDirtyPrice.dirty}`;
    } else {
        return "N/A";
    }
}

// could use pretty-ms or humanize-duration js package
function prettyMs(microSeconds) {
    const seconds = Math.floor(microSeconds / 1000000);
    const minutes = Math.floor(seconds / 60);
    const hours = Math.floor(minutes / 60);
    return hours + " hours " + (minutes % 60) + " minutes";
}

function prettyDate(isoDateStr) {
    const d = new Date(isoDateStr);
    return `${d.getMonth() + 1}/${d.getDate()} ${d.getFullYear()} ${d.getHours()}:${d.getMinutes()}`;
}
