package jmri.server.json;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.util.Locale;

/**
 * JSON Test HTTP service.
 *
 * @author Randall Wood Copyright 2018
 */
public class JsonTestHttpService extends JsonHttpService {

    public JsonTestHttpService(ObjectMapper mapper) {
        super(mapper);
    }

    @Override
    public JsonNode doGet(String type, String name, Locale locale) throws JsonException {
        ObjectNode root = mapper.createObjectNode();
        root.put(JSON.TYPE, type);
        ObjectNode data = root.putObject(JSON.DATA);
        data.put(JSON.NAME, name);
        return root;
    }

    @Override
    public JsonNode doPost(String type, String name, JsonNode data, Locale locale) throws JsonException {
        ObjectNode root = mapper.createObjectNode();
        root.put(JSON.TYPE, type);
        root.set(JSON.DATA, data);
        return root;
    }

    @Override
    public ArrayNode doGetList(String type, Locale locale) throws JsonException {
        ArrayNode array = mapper.createArrayNode();
        array.add(mapper.createObjectNode());
        array.add(mapper.createObjectNode());
        return array;
    }

}
