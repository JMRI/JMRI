package jmri.server.json.message;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import java.io.IOException;
import java.util.Locale;
import javax.servlet.http.HttpServletResponse;
import jmri.server.json.JSON;
import jmri.server.json.JsonException;
import jmri.server.json.JsonHttpService;

/**
 *
 * @author Randall Wood Copyright 2018
 */
public class JsonMessageHttpService extends JsonHttpService {

    public JsonMessageHttpService(ObjectMapper mapper) {
        super(mapper);
    }

    @Override
    public JsonNode doGet(String type, String name, JsonNode data, Locale locale, int id) throws JsonException {
        throw new JsonException(HttpServletResponse.SC_METHOD_NOT_ALLOWED, Bundle.getMessage(locale, "GetNotAllowed", type), id);
    }

    @Override
    public JsonNode doPost(String type, String name, JsonNode data, Locale locale, int id) throws JsonException {
        throw new JsonException(HttpServletResponse.SC_METHOD_NOT_ALLOWED, Bundle.getMessage(locale, "PostNotAllowed", type), id);
    }

    @Override
    public ArrayNode doGetList(String type, JsonNode data, Locale locale, int id) throws JsonException {
        throw new JsonException(HttpServletResponse.SC_BAD_REQUEST, Bundle.getMessage(locale, "UnlistableService", type), id);
    }

    @Override
    public JsonNode doSchema(String type, boolean server, Locale locale, int id) throws JsonException {
        switch (type) {
            case JsonMessage.CLIENT:
                return doSchema(type,
                        server,
                        "jmri/server/json/message/client-server.json",
                        "jmri/server/json/message/client-client.json",
                        id);
            case JsonMessage.MESSAGE:
                if (server) {
                    try {
                        return doSchema(type, server,
                                this.mapper.readTree(this.getClass().getClassLoader().getResource("jmri/server/json/message/message-server.json")), id);
                    } catch (IOException ex) {
                        throw new JsonException(500, ex, id);
                    }
                } else {
                    throw new JsonException(HttpServletResponse.SC_BAD_REQUEST, Bundle.getMessage(locale, "NotAClientType", type), id);
                }
            case JSON.HELLO:
                return doSchema(type,
                        server,
                        "jmri/server/json/util/hello-server.json",
                        "jmri/server/json/util/hello-client.json",
                        id);
            default:
                throw new JsonException(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, Bundle.getMessage(locale, JsonException.ERROR_UNKNOWN_TYPE, type), id);
        }
    }

}
