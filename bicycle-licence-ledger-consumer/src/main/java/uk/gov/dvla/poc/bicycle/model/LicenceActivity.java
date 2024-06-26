package uk.gov.dvla.poc.bicycle.model;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBAttribute;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBHashKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTable;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTypeConvertedJson;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.dvla.poc.bicycle.qldb.RevisionData;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Data
@NoArgsConstructor
@DynamoDBTable(tableName="licence-dev")
public class LicenceActivity implements RevisionData {

    @DynamoDBHashKey(attributeName="id")
    private String id;

    @DynamoDBAttribute(attributeName="PenaltyPoints")
    private int penaltyPoints;

    @DynamoDBAttribute(attributeName="Activity")
    @DynamoDBTypeConvertedJson
    private List<Activity> activity = new ArrayList<>();

    @JsonCreator
    public LicenceActivity(@JsonProperty("id") final String id,
                           @JsonProperty("penaltyPoints") final int penaltyPoints) {
        this.id = id;
        this.penaltyPoints = penaltyPoints;
    }

    @JsonProperty("id")
    public String getId() {
        return id;
    }

    @JsonProperty("penaltyPoints")
    public int getPenaltyPoints() {
        return penaltyPoints;
    }

    public void addActivity(Activity anActivity) {
        activity.add(anActivity);
    }

    @Override
    public String toString() {
        return "LicenceActivity{ id='" + id + '\'' +
                ", penaltyPoints='" + penaltyPoints + '\'' +
                '}';
    }

}
