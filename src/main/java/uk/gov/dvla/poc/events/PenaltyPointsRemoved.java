package uk.gov.dvla.poc.events;

import uk.gov.dvla.poc.model.Event;

public class PenaltyPointsRemoved extends Event {

    public PenaltyPointsRemoved() {
        super(PenaltyPointsRemoved.class.getSimpleName(), "Penalty points removed from licence");
    }
}
