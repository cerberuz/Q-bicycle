package uk.gov.dvla.poc.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BicycleLicence {

    @JsonIgnore
    private String documentId;

    private String id;

    private String name;

    private String email;

    private String telephone;

    private int penaltyPoints;

    private List<Event> events = new ArrayList<>();

    public void addEvent(Event evt) {
        events.add(evt);
    }

}
