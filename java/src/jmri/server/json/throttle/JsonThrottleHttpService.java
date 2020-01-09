package jmri.server.json.throttle;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import javax.servlet.http.HttpServletResponse;

import jmri.server.json.JsonException;
import jmri.server.json.JsonHttpService;
import jmri.server.json.JsonRequest;

/**
 * @author Randall Wood Copyright 2018
 */
public class JsonThrottleHttpService extends JsonHttpService {

    public JsonThrottleHttpService(ObjectMapper mapper) {
        super(mapper);
    }

    @Override
    public JsonNode doGet(String type, String name, JsonNode data, JsonRequest request) throws JsonException {
        throw new JsonException(HttpServletResponse.SC_METHOD_NOT_ALLOWED,
                Bundle.getMessage(request.locale, "GetNotAllowed", type), request.id);
    }

    @Override
    public JsonNode doPost(String type, String name, JsonNode data, JsonRequest request) throws JsonException {
        throw new JsonException(HttpServletResponse.SC_METHOD_NOT_ALLOWED,
                Bundle.getMessage(request.locale, "PostNotAllowed", type), request.id);
    }

    @Override
    public ArrayNode doGetList(String type, JsonNode data, JsonRequest request) throws JsonException {
        throw new JsonException(HttpServletResponse.SC_BAD_REQUEST,
                Bundle.getMessage(request.locale, "UnlistableService", type), request.id);
    }

    @Override
    public JsonNode doSchema(String type, boolean server, JsonRequest request) throws JsonException {
        if (JsonThrottle.THROTTLE.equals(type)) {
            return doSchema(type,
                    server,
                    "jmri/server/json/throttle/throttle-server.json",
                    "jmri/server/json/throttle/throttle-client.json",
                    request.id);
        } else {
            throw new JsonException(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    Bundle.getMessage(request.locale, JsonException.ERROR_UNKNOWN_TYPE, type), request.id);
        }
    }

}
