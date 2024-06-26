package uk.gov.dvla.poc.bicycle.model;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonDeserialize(using = EventDeserializer.class)
public class Event {

    private String eventName;

    private String eventDescription;

}
