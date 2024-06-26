package uk.gov.dvla.poc.bicycle.model;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.NoArgsConstructor;

import java.io.IOException;

@NoArgsConstructor
public class EventDeserializer extends JsonDeserializer<Event> {

    private ObjectMapper mapper = new ObjectMapper();

    @Override
    public Event deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException {
        ObjectCodec oc = jp.getCodec();
        JsonNode node = oc.readTree(jp);
        final String name = node.get("eventName").asText();
        final String description = node.get("eventDescription").asText();
        Event event = new Event();
        event.setEventDescription(description);
        event.setEventName(name);
        System.out.println("Event : " + event);
        return event;
    }

}
