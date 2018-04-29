package jmri.server.json.throttle;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import java.util.Locale;
import javax.servlet.http.HttpServletResponse;
import jmri.server.json.JsonException;
import jmri.server.json.JsonHttpService;

/**
 *
 * @author Randall Wood Copyright 2018
 */
public class JsonThrottleHttpService extends JsonHttpService {

    public JsonThrottleHttpService(ObjectMapper mapper) {
        super(mapper);
    }

    @Override
    public JsonNode doGet(String type, String name, Locale locale) throws JsonException {
        throw new JsonException(HttpServletResponse.SC_METHOD_NOT_ALLOWED, Bundle.getMessage(locale, "GetNotAllowed", type));
    }

    @Override
    public JsonNode doPost(String type, String name, JsonNode data, Locale locale) throws JsonException {
        throw new JsonException(HttpServletResponse.SC_METHOD_NOT_ALLOWED, Bundle.getMessage(locale, "PostNotAllowed", type));
    }

    @Override
    public ArrayNode doGetList(String type, Locale locale) throws JsonException {
        throw new JsonException(HttpServletResponse.SC_BAD_REQUEST, Bundle.getMessage(locale, "UnlistableService", type));
    }

}
