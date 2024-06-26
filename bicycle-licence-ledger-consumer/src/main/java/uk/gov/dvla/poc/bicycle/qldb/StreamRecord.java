package uk.gov.dvla.poc.bicycle.qldb;



import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

import java.io.IOException;
import java.util.Objects;

/**
 * Represents a record on the Qldb stream. An Amazon QLDB stream writes three
 * types of data records to a given Amazon Kinesis Data Streams resource:
 * control, block summary, and revision details.
 * <p>
 * Control records indicate the start and completion of your QLDB streams.
 * Whenever a revision is committed to your ledger, a QLDB stream writes all of
 * the associated journal block data in block summary and revision details
 * records.
 *
 * @see ControlRecord
 * @see BlockSummaryRecord
 * @see RevisionDetailsRecord
 */
@JsonDeserialize(using = StreamRecord.Deserializer.class)
public class StreamRecord {
    private String qldbStreamArn;
    private String recordType;
    private StreamRecordPayload payload;

    public StreamRecord(String qldbStreamArn, String recordType, StreamRecordPayload payload) {
        this.qldbStreamArn = qldbStreamArn;
        this.recordType = recordType;
        this.payload = payload;
    }

    @Override
    public String toString() {
        return "StreamRecord{" +
                "qldbStreamArn='" + qldbStreamArn + '\'' +
                ", recordType='" + recordType + '\'' +
                ", payload=" + payload +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        StreamRecord that = (StreamRecord) o;

        if (!Objects.equals(qldbStreamArn, that.qldbStreamArn)) {
            return false;
        }
        if (!Objects.equals(recordType, that.recordType)) {
            return false;
        }
        return Objects.equals(payload, that.payload);
    }

    @Override
    public int hashCode() {
        int result = qldbStreamArn != null ? qldbStreamArn.hashCode() : 0;
        result = 31 * result + (recordType != null ? recordType.hashCode() : 0);
        result = 31 * result + (payload != null ? payload.hashCode() : 0);
        return result;
    }

    public String getRecordType() {
        return recordType;
    }

    public StreamRecordPayload getPayload() {
        return payload;
    }

    public String getQldbStreamArn() {
        return qldbStreamArn;
    }

    public interface StreamRecordPayload { }

    static class Deserializer extends StdDeserializer<StreamRecord> {
        private Deserializer() {
            this(null);
        }

        private Deserializer(Class<?> vc) {
            super(vc);
        }

        @Override
        public StreamRecord deserialize(JsonParser jp, DeserializationContext dc)
                throws IOException {
            ObjectCodec codec = jp.getCodec();
            JsonNode node = codec.readTree(jp);
            String qldbStreamArn = node.get("qldbStreamArn").textValue();
            String recordType = node.get("recordType").textValue();
            JsonNode payloadJson = node.get("payload");
            StreamRecordPayload payload = null;

            switch (recordType) {
                case "CONTROL":
                    payload = codec.treeToValue(payloadJson, ControlRecord.class);
                    break;
                case "BLOCK_SUMMARY":
                    payload = codec.treeToValue(payloadJson, BlockSummaryRecord.class);
                    break;
                case "REVISION_DETAILS":
                    payload = codec.treeToValue(payloadJson, RevisionDetailsRecord.class);
                    break;
                default:
                    throw new RuntimeException("Unsupported record type: " + recordType);
            }

            return new StreamRecord(qldbStreamArn, recordType, payload);
        }

    }
}
