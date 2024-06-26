package uk.gov.dvla.poc.bicycle.qldb.config;


import com.amazonaws.SdkClientException;
import com.amazonaws.annotation.SdkInternalApi;
import com.amazonaws.protocol.MarshallLocation;
import com.amazonaws.protocol.MarshallingInfo;
import com.amazonaws.protocol.MarshallingType;
import com.amazonaws.protocol.ProtocolMarshaller;

@SdkInternalApi
public class KinesisConfigurationMarshaller {
    private static final MarshallingInfo<String> STREAMARN_BINDING;
    private static final MarshallingInfo<Boolean> AGGREGATIONENABLED_BINDING;
    private static final KinesisConfigurationMarshaller instance;

    public KinesisConfigurationMarshaller() {
    }

    public static KinesisConfigurationMarshaller getInstance() {
        return instance;
    }

    public void marshall(KinesisConfiguration kinesisConfiguration, ProtocolMarshaller protocolMarshaller) {
        if (kinesisConfiguration == null) {
            throw new SdkClientException("Invalid argument passed to marshall(...)");
        } else {
            try {
                protocolMarshaller.marshall(kinesisConfiguration.getStreamArn(), STREAMARN_BINDING);
                protocolMarshaller.marshall(kinesisConfiguration.getAggregationEnabled(), AGGREGATIONENABLED_BINDING);
            } catch (Exception var4) {
                throw new SdkClientException("Unable to marshall request to JSON: " + var4.getMessage(), var4);
            }
        }
    }

    static {
        STREAMARN_BINDING = MarshallingInfo.builder(MarshallingType.STRING).marshallLocation(MarshallLocation.PAYLOAD).marshallLocationName("StreamArn").build();
        AGGREGATIONENABLED_BINDING = MarshallingInfo.builder(MarshallingType.BOOLEAN).marshallLocation(MarshallLocation.PAYLOAD).marshallLocationName("AggregationEnabled").build();
        instance = new KinesisConfigurationMarshaller();
    }
}