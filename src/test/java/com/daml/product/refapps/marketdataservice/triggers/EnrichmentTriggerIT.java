/*
 * Copyright (c) 2019, Digital Asset (Switzerland) GmbH and/or its affiliates. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package com.daml.product.refapps.marketdataservice.triggers;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.daml.extensions.testing.junit4.Sandbox;
import com.daml.extensions.testing.ledger.DefaultLedgerAdapter;
import com.daml.extensions.testing.utils.ContractWithId;
import com.daml.ledger.javaapi.data.ExerciseCommand;
import com.daml.ledger.javaapi.data.Party;
import com.daml.product.refapps.utils.Trigger;
import com.google.protobuf.InvalidProtocolBufferException;
import da.refapps.marketdataservice.datalicense.DataLicense;
import da.refapps.marketdataservice.datastream.DataStream;
import da.refapps.marketdataservice.datastream.DataStream.ContractId;
import da.refapps.marketdataservice.datastream.EmptyDataStream;
import da.refapps.marketdataservice.marketdatatypes.Consumer;
import da.refapps.marketdataservice.marketdatatypes.InstrumentId;
import da.refapps.marketdataservice.marketdatatypes.Observation;
import da.refapps.marketdataservice.marketdatatypes.ObservationReference;
import da.refapps.marketdataservice.marketdatatypes.ObservationValue;
import da.refapps.marketdataservice.marketdatatypes.Publisher;
import da.refapps.marketdataservice.marketdatatypes.SubscriptionFee;
import da.refapps.marketdataservice.marketdatatypes.observationvalue.CleanPrice;
import da.refapps.marketdataservice.marketdatatypes.observationvalue.DirtyPrice;
import da.refapps.marketdataservice.marketdatatypes.observationvalue.EnrichedCleanDirtyPrice;
import da.refapps.marketdataservice.publication.Publication;
import da.refapps.marketdataservice.roles.ConsumerInvitation;
import da.refapps.marketdataservice.roles.DataLicenseProposal;
import da.refapps.marketdataservice.roles.DataStreamRequest;
import da.refapps.marketdataservice.roles.OperatorRole;
import da.refapps.marketdataservice.roles.PublisherConsumerRelationship;
import da.refapps.marketdataservice.roles.PublisherInvitation;
import da.refapps.marketdataservice.roles.PublisherRole;
import da.refapps.marketdataservice.roles.PublisherRoleInvitation;
import da.time.types.RelTime;
import da.timeservice.timeservice.CurrentTime;
import da.types.Tuple2;
import java.math.BigDecimal;
import java.nio.file.Paths;
import java.time.Instant;
import java.time.LocalDate;
import java.time.Period;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.After;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExternalResource;

public class EnrichmentTriggerIT {

  private static final Party OPERATOR = new Party("Operator");
  private static final Party MARKET = new Party("Market");
  private static final Party MARKET_DATA_VENDOR = new Party("MarketDataVendor");
  private static final Party ANALYTICS_VENDOR = new Party("AnalyticsVendor");

  private static final Sandbox sandbox =
      Sandbox.builder()
          .dar(Paths.get("./target/market-data-service.dar"))
          .parties(OPERATOR, MARKET, MARKET_DATA_VENDOR, ANALYTICS_VENDOR)
          .build();

  @ClassRule public static ExternalResource compile = sandbox.getClassRule();

  @Rule public ExternalResource sandboxRule = sandbox.getRule();

  private Trigger trigger;
  private DefaultLedgerAdapter ledger;

  @Before
  public void setup() throws Throwable {
    // Valid port is assigned only after the sandbox has been started.
    // Therefore trigger has to be configured at the point where this can be guaranteed.
    trigger =
        Trigger.builder()
            .dar(Paths.get("./target/market-data-service.dar"))
            .triggerName(
                "DA.RefApps.MarketDataService.Triggers.Enrichment:enrichCleanPriceWithAccrualTrigger")
            .sandboxPort(sandbox.getSandboxPort())
            .party(MARKET_DATA_VENDOR)
            .build();
    trigger.start();
    ledger = sandbox.getLedgerAdapter();
  }

  @After
  public void tearDown() {
    trigger.stop();
  }

  @Test
  public void observationValueUpdateTriggersEnrichment() throws InvalidProtocolBufferException {
    // Arrange
    // A date after the coupon date of the default bond (see the DAML model)
    Instant observationTime = Instant.parse("2019-05-03T10:15:30.00Z");
    String marketName = "Market";
    InstrumentId instrumentId = new InstrumentId("ISIN 123 XYZ");
    LocalDate maturityDate = LocalDate.now().plusWeeks(1);
    Long staleHours = 3L;
    ObservationReference label = new ObservationReference(marketName, instrumentId, maturityDate);
    ObservationValue dirtyPrice = new DirtyPrice(BigDecimal.valueOf(10));
    Observation marketObservation =
        new Observation(label, observationTime.minusSeconds(3600), dirtyPrice);

    Instant currentTime = Instant.parse("2020-01-03T10:15:30.00Z").minus(Period.ofDays(2));
    announceCurrentTime(currentTime); // Prevents expiration of license

    OperatorRole.ContractId operatorRoleCid = createOperatorRole();

    Tuple2<PublisherConsumerRelationship.ContractId, PublisherRole.ContractId> relations1 =
        makePublisherConsumerRelationship(operatorRoleCid, MARKET, MARKET_DATA_VENDOR);
    Tuple2<PublisherConsumerRelationship.ContractId, PublisherRole.ContractId> relations2 =
        makePublisherConsumerRelationship(operatorRoleCid, MARKET_DATA_VENDOR, ANALYTICS_VENDOR);

    ContractId marketStreamId =
        createDataStreamWithConsumer(
            relations1._1,
            relations1._2,
            MARKET,
            MARKET_DATA_VENDOR,
            marketObservation,
            observationTime,
            currentTime.plus(42, ChronoUnit.DAYS),
            staleHours);

    createDataStreamWithConsumer(
        relations2._1,
        relations2._2,
        MARKET_DATA_VENDOR,
        ANALYTICS_VENDOR,
        marketObservation,
        observationTime,
        currentTime.plus(42, ChronoUnit.DAYS),
        staleHours);

    // Act
    BigDecimal clean = BigDecimal.valueOf(10);
    CleanPrice cleanPrice = new CleanPrice(clean);
    ExerciseCommand updateObservation =
        marketStreamId.exerciseUpdateObservation(observationTime, cleanPrice);
    ledger.exerciseChoice(MARKET, updateObservation);

    // Assert

    // Market data vendor should have received a new publication
    // We are not interested in that value, this acts as an assertion
    getPublicationFor(MARKET_DATA_VENDOR);

    Publication publication = getPublicationFor(ANALYTICS_VENDOR);
    assertEquals(label, publication.observation.label);

    ObservationValue observedValue = publication.observation.value;
    assertTrue(observedValue instanceof EnrichedCleanDirtyPrice);
    EnrichedCleanDirtyPrice enrichedValue = (EnrichedCleanDirtyPrice) observedValue;

    assertTrue(enrichedValue.dirty.compareTo(enrichedValue.clean) > 0);
    assertTrue(enrichedValue.accrual.compareTo(BigDecimal.ZERO) > 0);
    assertTrue(
        enrichedValue
            .couponDate
            .get()
            .atStartOfDay()
            .toInstant(ZoneOffset.UTC)
            .isAfter(observationTime));
    assertTrue(cleanPrice.clean.compareTo(enrichedValue.clean) == 0);
    assertTrue(enrichedValue.rate.compareTo(new BigDecimal("0.01")) == 0);
  }

  @Test
  public void newPublicationWillStartEmptyStreams() throws InvalidProtocolBufferException {
    // Arrange
    // Market setup
    OperatorRole.ContractId operatorRoleCid = createOperatorRole();

    announceCurrentTime(
        Instant.parse("2020-01-03T10:15:30.00Z")
            .minus(Period.ofDays(2))); // Needed to start data streams

    Tuple2<PublisherConsumerRelationship.ContractId, PublisherRole.ContractId> relations1 =
        makePublisherConsumerRelationship(operatorRoleCid, MARKET, MARKET_DATA_VENDOR);
    Tuple2<PublisherConsumerRelationship.ContractId, PublisherRole.ContractId> relations2 =
        makePublisherConsumerRelationship(operatorRoleCid, MARKET_DATA_VENDOR, ANALYTICS_VENDOR);

    String marketName = "Market";
    InstrumentId instrumentId = new InstrumentId("ISIN 123 XYZ");
    LocalDate maturityDate = LocalDate.now().plusWeeks(1);
    ObservationReference label = new ObservationReference(marketName, instrumentId, maturityDate);
    ObservationValue dirtyPrice = new DirtyPrice(BigDecimal.valueOf(10));
    Observation marketObservation =
        new Observation(label, Instant.parse("2020-01-03T10:15:30.00Z"), dirtyPrice);

    Instant starting = Instant.parse("2020-01-03T10:15:30.00Z");
    Instant ending = starting.plus(Period.ofWeeks(1));
    Long staleHours = 3L;
    createEmptyDataStream(
        relations1._1,
        relations1._2,
        MARKET,
        MARKET_DATA_VENDOR,
        label,
        starting,
        ending,
        staleHours);
    createEmptyDataStream(
        relations2._1,
        relations2._2,
        MARKET_DATA_VENDOR,
        ANALYTICS_VENDOR,
        label,
        starting,
        ending,
        staleHours);

    // Act
    ledger.createContract(
        MARKET,
        Publication.TEMPLATE_ID,
        new Publication(
                marketObservation,
                new Publisher(MARKET.getValue()),
                new Consumer(MARKET_DATA_VENDOR.getValue()),
                Instant.parse("2020-01-03T10:15:30.00Z"),
                OPERATOR.getValue())
            .toValue());

    // Assert
    // Market should have received a new publication
    // We are not interested in that value, this acts as an assertion
    getPublicationFor(MARKET);
    ledger.getMatchedContract(MARKET_DATA_VENDOR, DataStream.TEMPLATE_ID, ContractId::new);
  }

  private OperatorRole.ContractId createOperatorRole() throws InvalidProtocolBufferException {
    OperatorRole operatorRole = new OperatorRole(OPERATOR.getValue());
    ledger.createContract(OPERATOR, OperatorRole.TEMPLATE_ID, operatorRole.toValue());
    return ledger.getCreatedContractId(
        OPERATOR, OperatorRole.TEMPLATE_ID, OperatorRole.ContractId::new);
  }

  private EmptyDataStream.ContractId createEmptyDataStream(
      PublisherConsumerRelationship.ContractId relationshipCid,
      PublisherRole.ContractId publisherRole,
      Party publisher,
      Party consumer,
      ObservationReference label,
      Instant starting,
      Instant ending,
      Long staleHours)
      throws InvalidProtocolBufferException {
    SubscriptionFee fee = new SubscriptionFee(new BigDecimal("10.000"));
    Publisher p = new Publisher(publisher.getValue());
    Consumer c = new Consumer(consumer.getValue());

    ledger.exerciseChoice(
        consumer, relationshipCid.exerciseRequestStream(label, starting, ending, staleHours));
    DataStreamRequest.ContractId streamRequestCid =
        ledger.getCreatedContractId(
            consumer, DataStreamRequest.TEMPLATE_ID, DataStreamRequest.ContractId::new);

    ledger.exerciseChoice(publisher, streamRequestCid.exerciseDataStreamRequest_Propose(fee));
    DataLicenseProposal.ContractId proposalCid =
        ledger.getCreatedContractId(
            publisher,
            DataLicenseProposal.TEMPLATE_ID,
            new DataLicenseProposal(
                    p,
                    c,
                    label,
                    starting,
                    ending,
                    toStaleMicroSecs(staleHours),
                    fee,
                    OPERATOR.getValue())
                .toValue(),
            DataLicenseProposal.ContractId::new);

    ledger.exerciseChoice(consumer, proposalCid.exerciseDataLicenseProposal_Accept());
    DataLicense.ContractId licenseCid =
        ledger.getCreatedContractId(consumer, DataLicense.TEMPLATE_ID, DataLicense.ContractId::new);

    ledger.exerciseChoice(publisher, publisherRole.exerciseRegisterLicense(licenseCid));

    // Not interested in the value, this acts as an assertion.
    return ledger.getCreatedContractId(
        publisher,
        EmptyDataStream.TEMPLATE_ID,
        new EmptyDataStream(OPERATOR.getValue(), label, Collections.singletonList(c), p).toValue(),
        EmptyDataStream.ContractId::new);
  }

  private RelTime toStaleMicroSecs(Long staleHours) {
    return new RelTime(staleHours * 60 * 60 * 1000000);
  }

  private Tuple2<PublisherConsumerRelationship.ContractId, PublisherRole.ContractId>
      makePublisherConsumerRelationship(
          OperatorRole.ContractId operatorRoleCid, Party publisher, Party consumer)
          throws InvalidProtocolBufferException {
    ledger.exerciseChoice(
        OPERATOR,
        operatorRoleCid.exerciseInvitePublisherConsumer(
            new Publisher(publisher.getValue()), new Consumer(consumer.getValue())));
    PublisherInvitation.ContractId publisherRoleInvitationCid =
        ledger.getCreatedContractId(
            OPERATOR, PublisherInvitation.TEMPLATE_ID, PublisherInvitation.ContractId::new);
    ledger.exerciseChoice(
        publisher, publisherRoleInvitationCid.exercisePublisherInvitation_Accept());
    // Need to match an exact record, otherwise subsequent calls might return an already consumed
    // contract id. This occurs if one tries to setup relationships where the consumer of the first
    // relationship is the publisher of the second.
    ConsumerInvitation consumerInvitation =
        new ConsumerInvitation(
            OPERATOR.getValue(),
            new Publisher(publisher.getValue()),
            new Consumer(consumer.getValue()));
    ConsumerInvitation.ContractId consumerRoleInvitationCid =
        ledger.getCreatedContractId(
            publisher,
            ConsumerInvitation.TEMPLATE_ID,
            consumerInvitation.toValue(),
            ConsumerInvitation.ContractId::new);
    ledger.exerciseChoice(consumer, consumerRoleInvitationCid.exerciseConsumerInvitation_Accept());
    PublisherConsumerRelationship.ContractId pubConsRelship =
        ledger.getCreatedContractId(
            consumer,
            PublisherConsumerRelationship.TEMPLATE_ID,
            PublisherConsumerRelationship.ContractId::new);

    PublisherRoleInvitation.ContractId pubRoleInvitationCid =
        ledger.getCreatedContractId(
            publisher,
            PublisherRoleInvitation.TEMPLATE_ID,
            PublisherRoleInvitation.ContractId::new);
    ledger.exerciseChoice(publisher, pubRoleInvitationCid.exercisePublisherRoleInvitation_Accept());
    PublisherRole.ContractId pubRole =
        ledger.getCreatedContractId(
            publisher, PublisherRole.TEMPLATE_ID, PublisherRole.ContractId::new);

    return new Tuple2<>(pubConsRelship, pubRole);
  }

  private DataStream.ContractId createDataStreamWithConsumer(
      PublisherConsumerRelationship.ContractId relationshipCid,
      PublisherRole.ContractId publisherRole,
      Party publisher,
      Party consumer,
      Observation observation,
      Instant starting,
      Instant ending,
      Long staleHours)
      throws InvalidProtocolBufferException {
    ObservationReference label = observation.label;

    EmptyDataStream.ContractId emptyStreamCid =
        createEmptyDataStream(
            relationshipCid,
            publisherRole,
            publisher,
            consumer,
            label,
            starting,
            ending,
            staleHours);
    ledger.exerciseChoice(publisher, emptyStreamCid.exerciseStartDataStream(observation));
    DataStream.ContractId dataStreamId =
        ledger.getCreatedContractId(publisher, DataStream.TEMPLATE_ID, DataStream.ContractId::new);

    getPublicationFor(consumer); // Initial publication, ignore
    return dataStreamId;
  }

  private void announceCurrentTime(Instant time) throws InvalidProtocolBufferException {
    List<String> observers =
        Arrays.asList(
            MARKET.getValue(), MARKET_DATA_VENDOR.getValue(), ANALYTICS_VENDOR.getValue());
    CurrentTime currentTime = new CurrentTime(OPERATOR.getValue(), time, observers);
    ledger.createContract(OPERATOR, CurrentTime.TEMPLATE_ID, currentTime.toValue());
  }

  private Publication getPublicationFor(Party party) {
    ContractWithId<Publication.ContractId> publication = getPublicationContract(party);
    return Publication.fromValue(publication.record);
  }

  private ContractWithId<Publication.ContractId> getPublicationContract(Party party) {
    return ledger.getMatchedContract(party, Publication.TEMPLATE_ID, Publication.ContractId::new);
  }
}
