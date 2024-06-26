package uk.gov.dvla.poc.model;

import com.amazon.ion.IonBlob;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class IntegrityInfo {

    private IonBlob hash;

    private int sequenceNo;

    private String strandId;
}
