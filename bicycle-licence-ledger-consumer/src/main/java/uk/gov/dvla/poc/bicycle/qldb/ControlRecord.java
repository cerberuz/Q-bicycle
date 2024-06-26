package uk.gov.dvla.poc.bicycle.qldb;

/*
 * Copyright 2019 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: MIT-0
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this
 * software and associated documentation files (the "Software"), to deal in the Software
 * without restriction, including without limitation the rights to use, copy, modify,
 * merge, publish, distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
 * INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A
 * PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
 * HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
 * OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
 * SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import uk.gov.dvla.poc.bicycle.qldb.config.KinesisConfiguration;

import java.io.IOException;
import java.util.Date;
import java.util.Objects;

/**
 * Represents a control record on the QLDB stream. QLDB stream writes control
 * records to indicate its start and completion events.
 *
 * @see CreatedControlRecordPayload
 * @see CompletedControlRecordPayload
 */
@JsonDeserialize(using = ControlRecord.Deserializer.class)
public final class ControlRecord implements StreamRecord.StreamRecordPayload {

    private String recordType;
    private ControlRecordPayload payload;

    public ControlRecord(String recordType, ControlRecordPayload payload) {
        this.recordType = recordType;
        this.payload = payload;
    }

    @Override
    public String toString() {
        return "ControlRecord{" +
                "recordType='" + recordType + '\'' +
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

        ControlRecord that = (ControlRecord) o;

        if (!Objects.equals(recordType, that.recordType)) {
            return false;
        }
        return Objects.equals(payload, that.payload);
    }

    @Override
    public int hashCode() {
        int result = recordType != null ? recordType.hashCode() : 0;
        result = 31 * result + (payload != null ? payload.hashCode() : 0);
        return result;
    }

    public String getRecordType() {
        return recordType;
    }

    public ControlRecordPayload getPayload() {
        return payload;
    }

    abstract static class ControlRecordPayload {
    }

    public static class Deserializer extends StdDeserializer<ControlRecord> {
        public Deserializer() {
            this(null);
        }

        private Deserializer(Class<?> vc) {
            super(vc);
        }

        @Override
        public ControlRecord deserialize(JsonParser jsonParser, DeserializationContext ctx) throws IOException {
            ObjectCodec codec = jsonParser.getCodec();
            JsonNode node = codec.readTree(jsonParser);
            String controlRecordType = node.get("controlRecordType").textValue();
            JsonNode payloadJson = node.get("payload");
            ControlRecordPayload payload = null;

            switch (controlRecordType) {
                case "CREATED":
                    payload = codec.treeToValue(payloadJson, CreatedControlRecordPayload.class);
                    break;
                case "COMPLETED":
                    payload = new CompletedControlRecordPayload();
                    break;
                default:
                    throw new RuntimeException("Unsupported control record type: " + controlRecordType);
            }

            return new ControlRecord(controlRecordType, payload);
        }
    }

    private static class CreatedControlRecordPayload extends ControlRecordPayload {
        private Date inclusiveStartTime;
        private KinesisConfiguration kinesisConfiguration;
        private Boolean aggregationEnabled;
        private Date exclusiveEndTime;

        @JsonCreator
        CreatedControlRecordPayload(
                @JsonProperty("inclusiveStartTime") Date inclusiveStartTime,
                @JsonProperty("exclusiveEndTime") Date exclusiveEndTime,
                @JsonProperty("kinesisConfiguration") KinesisConfiguration kinesisConfiguration,
                @JsonProperty("aggregationEnabled") Boolean aggregationEnabled) {
            this.inclusiveStartTime = inclusiveStartTime;
            this.exclusiveEndTime = exclusiveEndTime;
            this.kinesisConfiguration = kinesisConfiguration;
            this.aggregationEnabled = aggregationEnabled;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }

            CreatedControlRecordPayload that = (CreatedControlRecordPayload) o;

            if (!Objects.equals(inclusiveStartTime, that.inclusiveStartTime)) {
                return false;
            }
            if (!Objects.equals(kinesisConfiguration, that.kinesisConfiguration)) {
                return false;
            }
            if (!Objects.equals(aggregationEnabled, that.aggregationEnabled)) {
                return false;
            }
            return Objects.equals(exclusiveEndTime, that.exclusiveEndTime);
        }

        @Override
        public int hashCode() {
            int result = inclusiveStartTime != null ? inclusiveStartTime.hashCode() : 0;
            result = 31 * result + (kinesisConfiguration != null ? kinesisConfiguration.hashCode() : 0);
            result = 31 * result + (aggregationEnabled != null ? aggregationEnabled.hashCode() : 0);
            result = 31 * result + (exclusiveEndTime != null ? exclusiveEndTime.hashCode() : 0);
            return result;
        }

        @Override
        public String toString() {
            return "CreatedControlRecordPayload{" +
                    "inclusiveStartTime=" + inclusiveStartTime +
                    ", kinesisConfiguration=" + kinesisConfiguration +
                    ", aggregationEnabled=" + aggregationEnabled +
                    ", exclusiveEndTime=" + exclusiveEndTime +
                    '}';
        }

        public Date getInclusiveStartTime() {
            return inclusiveStartTime;
        }

        public KinesisConfiguration getKinesisConfiguration() {
            return kinesisConfiguration;
        }

        public Boolean getAggregationEnabled() {
            return aggregationEnabled;
        }

        public Date getExclusiveEndTime() {
            return exclusiveEndTime;
        }
    }

    private static class CompletedControlRecordPayload extends ControlRecordPayload {
        @Override
        public String toString() {
            return "CompletedControlRecordPayload{}";
        }
    }
}
