package uk.gov.dvla.poc.bicycle.model;

import com.amazonaws.services.dynamodbv2.datamodeling.*;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.dvla.poc.bicycle.qldb.RevisionData;

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Data
@NoArgsConstructor
@DynamoDBTable(tableName="licence-dev")
public class BicycleLicence implements RevisionData {

    private String id;

    private int penaltyPoints;

    @JsonIgnore
    private String email;

    @JsonIgnore
    private String name;

    @JsonIgnore
    private String telephone;

    private List<Event> events = new ArrayList<>();

    @JsonCreator
    public BicycleLicence(@JsonProperty("id") final String id,
                   @JsonProperty("penaltyPoints") final int penaltyPoints, @JsonProperty("events") final List<Event> events) {
        this.id = id;
        this.penaltyPoints = penaltyPoints;
        this.events = events;
    }

    @JsonProperty("id")
    public String getId() {
        return id;
    }

    @JsonProperty("penaltyPoints")
    public int getPenaltyPoints() {
        return penaltyPoints;
    }


    @Override
    public String toString() {
        return "BicycleLicence{ id='" + id + '\'' +
                ", penaltyPoints='" + penaltyPoints + '\'' +
                '}';
    }

}
