package uk.gov.dvla.poc.model;

import com.amazon.ion.IonStruct;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class HistoryResult {

    private String documentId;
    private int version;

    private String transactionId;
    private String transactionTime;

    private IntegrityInfo integrityInfo;

    private BicycleLicence licence;

    private IonStruct blockAddress;

}
