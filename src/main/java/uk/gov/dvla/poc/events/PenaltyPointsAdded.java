package uk.gov.dvla.poc.events;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.dvla.poc.model.Event;

public class PenaltyPointsAdded extends Event {

    public PenaltyPointsAdded() {
        super(PenaltyPointsAdded.class.getSimpleName(), "Penalty points added to licence for cycling to quickly");
    }
}
