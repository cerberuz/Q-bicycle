package uk.gov.dvla.poc.events;

import uk.gov.dvla.poc.model.Event;

public class ContactDetailsChanged extends Event {

    public ContactDetailsChanged() {
        super(ContactDetailsChanged.class.getSimpleName(), "Customer has changed telephone number");
    }
}
