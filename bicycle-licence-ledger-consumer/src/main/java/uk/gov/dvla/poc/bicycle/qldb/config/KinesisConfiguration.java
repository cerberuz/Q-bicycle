package uk.gov.dvla.poc.bicycle.qldb.config;

import com.amazonaws.annotation.SdkInternalApi;
import com.amazonaws.protocol.ProtocolMarshaller;
import com.amazonaws.protocol.StructuredPojo;
import java.io.Serializable;

public class KinesisConfiguration implements Serializable, Cloneable, StructuredPojo {
    private String streamArn;
    private Boolean aggregationEnabled;

    public KinesisConfiguration() {
    }

    public void setStreamArn(String streamArn) {
        this.streamArn = streamArn;
    }

    public String getStreamArn() {
        return this.streamArn;
    }

    public KinesisConfiguration withStreamArn(String streamArn) {
        this.setStreamArn(streamArn);
        return this;
    }

    public void setAggregationEnabled(Boolean aggregationEnabled) {
        this.aggregationEnabled = aggregationEnabled;
    }

    public Boolean getAggregationEnabled() {
        return this.aggregationEnabled;
    }

    public KinesisConfiguration withAggregationEnabled(Boolean aggregationEnabled) {
        this.setAggregationEnabled(aggregationEnabled);
        return this;
    }

    public Boolean isAggregationEnabled() {
        return this.aggregationEnabled;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        if (this.getStreamArn() != null) {
            sb.append("StreamArn: ").append(this.getStreamArn()).append(",");
        }

        if (this.getAggregationEnabled() != null) {
            sb.append("AggregationEnabled: ").append(this.getAggregationEnabled());
        }

        sb.append("}");
        return sb.toString();
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        } else if (obj == null) {
            return false;
        } else if (!(obj instanceof KinesisConfiguration)) {
            return false;
        } else {
            KinesisConfiguration other = (KinesisConfiguration)obj;
            if (other.getStreamArn() == null ^ this.getStreamArn() == null) {
                return false;
            } else if (other.getStreamArn() != null && !other.getStreamArn().equals(this.getStreamArn())) {
                return false;
            } else if (other.getAggregationEnabled() == null ^ this.getAggregationEnabled() == null) {
                return false;
            } else {
                return other.getAggregationEnabled() == null || other.getAggregationEnabled().equals(this.getAggregationEnabled());
            }
        }
    }

    public int hashCode() {
        int tempHashCode = 1;
        int hashCode = 31 * tempHashCode + (this.getStreamArn() == null ? 0 : this.getStreamArn().hashCode());
        hashCode = 31 * hashCode + (this.getAggregationEnabled() == null ? 0 : this.getAggregationEnabled().hashCode());
        return hashCode;
    }

    public KinesisConfiguration clone() {
        try {
            return (KinesisConfiguration)super.clone();
        } catch (CloneNotSupportedException var2) {
            throw new IllegalStateException("Got a CloneNotSupportedException from Object.clone() even though we're Cloneable!", var2);
        }
    }

    @SdkInternalApi
    public void marshall(ProtocolMarshaller protocolMarshaller) {
        KinesisConfigurationMarshaller.getInstance().marshall(this, protocolMarshaller);
    }
}
