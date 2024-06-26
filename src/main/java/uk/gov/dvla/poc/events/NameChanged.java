package uk.gov.dvla.poc.events;

import uk.gov.dvla.poc.model.Event;

public class NameChanged extends Event {

    public NameChanged() {
        super(NameChanged.class.getSimpleName(), "Customer has changed name on licence");
    }
}
