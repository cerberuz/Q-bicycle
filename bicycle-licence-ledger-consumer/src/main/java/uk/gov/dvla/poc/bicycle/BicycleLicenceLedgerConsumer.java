package uk.gov.dvla.poc.bicycle;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.Optional;

import com.amazon.ion.IonReader;
import com.amazon.ion.IonWriter;
import com.amazon.ion.system.IonReaderBuilder;
import com.amazon.ion.system.IonTextWriterBuilder;
import com.amazonaws.Response;
import com.amazonaws.services.kinesis.model.Record;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.KinesisEvent;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.dataformat.ion.IonObjectMapper;
import lombok.extern.slf4j.Slf4j;
import uk.gov.dvla.poc.bicycle.model.Activity;
import uk.gov.dvla.poc.bicycle.model.BicycleLicence;
import uk.gov.dvla.poc.bicycle.model.Event;
import uk.gov.dvla.poc.bicycle.model.LicenceActivity;
import uk.gov.dvla.poc.bicycle.qldb.Revision;
import uk.gov.dvla.poc.bicycle.qldb.RevisionData;
import uk.gov.dvla.poc.bicycle.qldb.RevisionDetailsRecord;
import uk.gov.dvla.poc.bicycle.qldb.StreamRecord;
import uk.gov.dvla.poc.bicycle.repository.BicycleLicenceDynamoDBRepository;

public class BicycleLicenceLedgerConsumer implements RequestHandler<KinesisEvent, Optional<Response>> {

    private static ObjectReader reader = new IonObjectMapper().readerFor(StreamRecord.class);

    private BicycleLicenceDynamoDBRepository dynamoDBRepository = new BicycleLicenceDynamoDBRepository();

    public Optional<Response> handleRequest(KinesisEvent event, Context context) {
        List<KinesisEvent.KinesisEventRecord> records = event.getRecords();

        records.forEach(r -> {
            try {

                Record record = r.getKinesis();
                printDetailsOfRecord(records, record);

                StreamRecord streamRecord = reader.readValue(r.getKinesis().getData().array());
                System.out.println("Record Type: " + streamRecord.getRecordType());
                if (streamRecord.getRecordType().equals("REVISION_DETAILS")) {
                    RevisionDetailsRecord streamRecordPayload = (RevisionDetailsRecord) streamRecord.getPayload();
                    Revision docRevision = streamRecordPayload.getRevision();

                    // Delete Block

                    String idToDelete = docRevision.getMetadata().getId();
                    if (docRevision.getData() == null) {
                        dynamoDBRepository.deleteById(idToDelete);
                    } else {
                        try {
                            BicycleLicence docRevisionData = (BicycleLicence) docRevision.getData();
                            LicenceActivity existingRecord = dynamoDBRepository.findById(docRevision.getMetadata().getId());
                            LicenceActivity licenceActivity = new LicenceActivity();
                            licenceActivity.setId(docRevision.getMetadata().getId());
                            licenceActivity.setPenaltyPoints(docRevisionData.getPenaltyPoints());
                            if (existingRecord != null) {
                                addExistingEvents(licenceActivity, existingRecord);
                                System.out.println("events size " + docRevisionData.getEvents());
                                for (Event revisionEvent : docRevisionData.getEvents()) {
                                    System.out.println("Creating Activity from event " + revisionEvent);
                                    Activity activity = new Activity();
                                    activity.setEventDate(docRevision.getMetadata().getTxTime().toString());
                                    activity.setEventName(revisionEvent.getEventName());
                                    activity.setEventDescription(revisionEvent.getEventDescription());
                                    System.out.println("Activity = " + activity);
                                    licenceActivity.addActivity(activity);
                                }
                            }
                            dynamoDBRepository.save(licenceActivity);
                        } catch (ClassCastException ce) {
                            System.err.println("Unable to cast object to bicycle licence " + ce.getMessage());
                        }
                    }
                } else {
                    System.out.println("Record Type not to be processed: " + streamRecord.getRecordType() + " skipping");
                }

            } catch (Exception e) {
                System.err.println("Error processing record. " + e.getMessage());
                e.printStackTrace();
            }
        });
        return null;
    }

    private void printDetailsOfRecord(List<KinesisEvent.KinesisEventRecord> records, Record record) {
        // TODO: Add support to provide activity on a licence as well as stripping of personal details
        System.out.println("Attempting to read kinesis record of length " + records.size());
        StringBuilder builder = new StringBuilder();
        builder.append("Processing Record with Seq: ").append(record.getSequenceNumber());
        builder.append("PartitionKey: ").append(record.getPartitionKey());

        try {
            builder.append("IonText: ").append(toIonText(record.getData()));
        } catch (Exception e) {
            System.out.println("This is the record that errors: ");
            System.out.println("Byte[] of data: " + record.getData().array());

            System.out.println("String data: " + new String(record.getData().array()));
        }
        System.out.println("---- \n" + builder.toString() + "\n");
    }

    private void addExistingEvents(LicenceActivity newActivity, LicenceActivity existingRecord) {
        newActivity.setActivity(existingRecord.getActivity());
    }

    private void rewrite(byte[] data, IonWriter writer) throws IOException {
        IonReader reader = IonReaderBuilder.standard().build(data);
        writer.writeValues(reader);
    }

    private String toIonText(ByteBuffer data) throws IOException {
        StringBuilder stringBuilder = new StringBuilder();
        try (IonWriter prettyWriter = IonTextWriterBuilder.pretty().build(stringBuilder)) {
            rewrite(data.array(), prettyWriter);
        }
        return stringBuilder.toString();
    }
}