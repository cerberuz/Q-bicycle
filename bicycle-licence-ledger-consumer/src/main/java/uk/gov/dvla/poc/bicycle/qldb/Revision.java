package uk.gov.dvla.poc.bicycle.qldb;


import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.dataformat.ion.IonTimestampSerializers;
import lombok.extern.slf4j.Slf4j;
import uk.gov.dvla.poc.bicycle.model.BicycleLicence;

import java.io.IOException;
import java.util.Arrays;
import java.util.Date;
import java.util.Objects;

/**
 * Represents a Revision including both user data and metadata.
 */
@Slf4j
public final class Revision {
    private final BlockAddress blockAddress;
    private final Metadata metadata;
    private final byte[] hash;
    @JsonDeserialize(using = RevisionDataDeserializer.class)
    private final RevisionData data;

    @JsonCreator
    public Revision(@JsonProperty("blockAddress") final BlockAddress blockAddress,
                    @JsonProperty("metadata") final Metadata metadata,
                    @JsonProperty("hash") final byte[] hash,
                    @JsonProperty("data") final RevisionData data) {
        this.blockAddress = blockAddress;
        this.metadata = metadata;
        this.hash = hash;
        this.data = data;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        Revision revision = (Revision) o;

        if (!Objects.equals(blockAddress, revision.blockAddress)) {
            return false;
        }
        if (!Objects.equals(metadata, revision.metadata)) {
            return false;
        }
        if (!Arrays.equals(hash, revision.hash)) {
            return false;
        }
        return Objects.equals(data, revision.data);
    }

    @Override
    public int hashCode() {
        int result = blockAddress != null ? blockAddress.hashCode() : 0;
        result = 31 * result + (metadata != null ? metadata.hashCode() : 0);
        result = 31 * result + Arrays.hashCode(hash);
        result = 31 * result + (data != null ? data.hashCode() : 0);
        return result;
    }

    /**
     * Converts a {@link Revision} object to string.
     *
     * @return the string representation of the {@link Revision} object.
     */
    @Override
    public String toString() {
        return "Revision{" +
                "blockAddress=" + blockAddress +
                ", metadata=" + metadata +
                ", hash=" + Arrays.toString(hash) +
                ", data=" + data +
                '}';
    }

    public BlockAddress getBlockAddress() {
        return blockAddress;
    }

    public Metadata getMetadata() {
        return metadata;
    }

    public byte[] getHash() {
        return hash;
    }

    public RevisionData getData() {
        return data;
    }

    /**
     * Represents the metadata field of a QLDB Document.
     */
    public static class Metadata {
        private final String id;
        private final long version;
        @JsonSerialize(using = IonTimestampSerializers.IonTimestampJavaDateSerializer.class)
        private final Date txTime;
        private final String txId;

        @JsonCreator
        public Metadata(@JsonProperty("id") final String id,
                        @JsonProperty("version") final long version,
                        @JsonProperty("txTime") final Date txTime,
                        @JsonProperty("txId") final String txId) {
            this.id = id;
            this.version = version;
            this.txTime = txTime;
            this.txId = txId;
        }

        /**
         * Converts a {@link Metadata} object to a string.
         *
         * @return the string representation of the {@link Revision} object.
         */
        @Override
        public String toString() {
            return "Metadata{" +
                    "id='" + id + '\'' +
                    ", version=" + version +
                    ", txTime=" + txTime +
                    ", txId='" + txId + '\'' +
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

            Metadata metadata = (Metadata) o;

            if (version != metadata.version) {
                return false;
            }
            if (!Objects.equals(id, metadata.id)) {
                return false;
            }
            if (!Objects.equals(txTime, metadata.txTime)) {
                return false;
            }
            return Objects.equals(txId, metadata.txId);
        }

        @Override
        public int hashCode() {
            int result = id != null ? id.hashCode() : 0;
            result = 31 * result + (int) (version ^ (version >>> 32));
            result = 31 * result + (txTime != null ? txTime.hashCode() : 0);
            result = 31 * result + (txId != null ? txId.hashCode() : 0);
            return result;
        }

        public String getId() {
            return id;
        }

        public long getVersion() {
            return version;
        }

        public Date getTxTime() {
            return txTime;
        }

        public String getTxId() {
            return txId;
        }
    }

    public static class RevisionDataDeserializer extends JsonDeserializer<RevisionData> {

        @Override
        public RevisionData deserialize(JsonParser jp, DeserializationContext dc) throws IOException {
            TableInfo tableInfo = (TableInfo) jp.getParsingContext().getParent().getCurrentValue();
            RevisionData revisionData;
            System.out.println("Deserialize revision data");
            switch (tableInfo.getTableName()) {
                case "licence":

                    revisionData = jp.readValueAs(BicycleLicence.class);
                    break;
                default:
                    throw new RuntimeException("Unsupported table " + tableInfo.getTableName());
            }

            return revisionData;
        }
    }
}