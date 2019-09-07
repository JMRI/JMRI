package jmri.server.json.throttle;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import java.util.Locale;
import javax.servlet.http.HttpServletResponse;

import jmri.server.json.JsonException;
import jmri.server.json.JsonHttpService;

/**
 * @author Randall Wood Copyright 2018
 */
public class JsonThrottleHttpService extends JsonHttpService {

    public JsonThrottleHttpService(ObjectMapper mapper) {
        super(mapper);
    }

    @Override
    public JsonNode doGet(String type, String name, JsonNode data, Locale locale, int id) throws JsonException {
        throw new JsonException(HttpServletResponse.SC_METHOD_NOT_ALLOWED,
                Bundle.getMessage(locale, "GetNotAllowed", type), id);
    }

    @Override
    public JsonNode doPost(String type, String name, JsonNode data, Locale locale, int id) throws JsonException {
        throw new JsonException(HttpServletResponse.SC_METHOD_NOT_ALLOWED,
                Bundle.getMessage(locale, "PostNotAllowed", type), id);
    }

    @Override
    public ArrayNode doGetList(String type, JsonNode data, Locale locale, int id) throws JsonException {
        throw new JsonException(HttpServletResponse.SC_BAD_REQUEST,
                Bundle.getMessage(locale, "UnlistableService", type), id);
    }

    @Override
    public JsonNode doSchema(String type, boolean server, Locale locale, int id) throws JsonException {
        if (JsonThrottle.THROTTLE.equals(type)) {
            return doSchema(type,
                    server,
                    "jmri/server/json/throttle/throttle-server.json",
                    "jmri/server/json/throttle/throttle-client.json",
                    id);
        } else {
            throw new JsonException(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    Bundle.getMessage(locale, JsonException.ERROR_UNKNOWN_TYPE, type), id);
        }
    }

}
