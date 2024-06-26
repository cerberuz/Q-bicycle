package uk.gov.dvla.poc.bicycle.repository;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapperConfig;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.dynamodbv2.model.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import uk.gov.dvla.poc.bicycle.model.Activity;
import uk.gov.dvla.poc.bicycle.model.BicycleLicence;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import uk.gov.dvla.poc.bicycle.model.Event;
import uk.gov.dvla.poc.bicycle.model.LicenceActivity;

import java.io.IOException;
import java.util.*;

public class BicycleLicenceDynamoDBRepository {

    private AmazonDynamoDB client = AmazonDynamoDBClientBuilder.standard()
            .withRegion(Regions.EU_WEST_1)
            .build();

    DynamoDBMapperConfig mapperConfig = DynamoDBMapperConfig.builder()
            .withSaveBehavior(DynamoDBMapperConfig.SaveBehavior.APPEND_SET)
            .withConsistentReads(DynamoDBMapperConfig.ConsistentReads.CONSISTENT)
            .withTableNameOverride(null)
            .withPaginationLoadingStrategy(DynamoDBMapperConfig.PaginationLoadingStrategy.EAGER_LOADING)
            .build();

    private ObjectMapper objectMapper = new ObjectMapper();

    private DynamoDBMapper mapper = new DynamoDBMapper(client, mapperConfig);
    private DynamoDB dynamoDB = new DynamoDB(client);

    private static final String TABLE_NAME = "licence-dev";

    public <S extends LicenceActivity> S save(S s) {
        System.out.println(s.toString());

        mapper.save(s);
        /*Item item = new Item().withPrimaryKey("id", s.getId())
                .withNumber("PenaltyPoints", s.getPenaltyPoints())
                .withMap("Activity", );*/
        //table.putItem(item);

        return s;
    }

    public LicenceActivity findById(String s) {
        Table table = dynamoDB.getTable(TABLE_NAME);
        System.out.println("Getting record by id: " + s);
        HashMap<String, AttributeValue> key = new HashMap<String, AttributeValue>();
        key.put("id", new AttributeValue().withS(s));
        GetItemRequest getItemRequest = new GetItemRequest()
                .withTableName(TABLE_NAME)
                .withKey(key);
        GetItemResult activityItemResult = client.getItem(getItemRequest);
        Map<String, AttributeValue> activityItem = activityItemResult.getItem();
        System.out.println("Activity Item from Dynamo: " + activityItem);
        LicenceActivity activity = null;
        if(activityItem != null) {
            activity = new LicenceActivity();
            activity.setId(activityItem.get("id").getS());
            activity.setPenaltyPoints(Integer.parseInt(activityItem.get("PenaltyPoints").getN()));

            // Parse event string to json
            try {
                String eventString = activityItem.get("Activity").getS();

                if (eventString != "") {

                    Activity[] events = objectMapper.readValue(eventString, Activity[].class);
                    for (Activity event : events) {
                        Activity activityModel = new Activity();
                        activityModel.setEventDescription(event.getEventDescription());
                        activityModel.setEventName(event.getEventName());
                        activityModel.setEventDate(event.getEventDate());
                        activity.addActivity(activityModel);
                    }
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
            // add json parser for event array to activity
        }


        // activity.setActivity();

        return activity;
    }

    public boolean deleteById(String s) {
        System.out.println("Deleting record with id: " + s);
        HashMap<String, AttributeValue> key = new HashMap<String, AttributeValue>();
        key.put("id", new AttributeValue().withS(s));
        DeleteItemRequest itemRequest = new DeleteItemRequest()
                .withTableName(TABLE_NAME)
                .withKey(key);
        DeleteItemResult activityItemResult = client.deleteItem(itemRequest);
        return true;
    }


}
