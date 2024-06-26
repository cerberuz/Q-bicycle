package uk.gov.dvla.poc.forms;

import com.amazon.ion.IonStruct;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.dvla.poc.service.Verifier;

import java.nio.ByteBuffer;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class VerifyLicenceForm {

    private int index;


}
