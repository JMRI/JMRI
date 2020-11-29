package jmri.server.json;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import javax.servlet.http.HttpServletResponse;

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
    public JsonNode doGet(String type, String name, JsonNode parameters, JsonRequest request) throws JsonException {
        if (name.equals("JsonException")) {
            // not a standard code
            throw new JsonException(499, "Thrown for test", request.id);
        }
        ObjectNode root = mapper.createObjectNode();
        root.put(JSON.TYPE, type);
        ObjectNode data = root.putObject(JSON.DATA);
        data.put(JSON.NAME, name);
        return root;
    }

    @Override
    public JsonNode doPost(String type, String name, JsonNode data, JsonRequest request) throws JsonException {
        return message(type, data, request.id);
    }

    @Override
    public ArrayNode doGetList(String type, JsonNode data, JsonRequest request) throws JsonException {
        ArrayNode array = mapper.createArrayNode();
        array.add(message(type, mapper.createObjectNode(), request.id));
        array.add(message(type, mapper.createObjectNode(), request.id));
        return array;
    }

    @Override
    public JsonNode doSchema(String type, boolean server, JsonRequest request) throws JsonException {
        switch (type) {
            case JsonTestServiceFactory.TEST:
                // return an empty schema, which is valid, but accepts anything
                return mapper.createObjectNode();
            default:
                throw new JsonException(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                        Bundle.getMessage(request.locale, "ErrorUnknownType", type), request.id);
        }
    }

}
