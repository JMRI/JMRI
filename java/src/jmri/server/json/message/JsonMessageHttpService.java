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

    @Override
    public JsonNode doSchema(String type, boolean server, Locale locale) throws JsonException {
        switch (type) {
            case JsonMessage.CLIENT:
                return doSchema(type,
                        server,
                        "jmri/server/json/message/client-server.json",
                        "jmri/server/json/message/client-client.json");
            case JsonMessage.MESSAGE:
                if (server) {
                    try {
                        return doSchema(type, server,
                                this.mapper.readTree(this.getClass().getClassLoader().getResource("jmri/server/json/message/message-server.json")));
                    } catch (IOException ex) {
                        throw new JsonException(500, ex);
                    }
                } else {
                    throw new JsonException(HttpServletResponse.SC_BAD_REQUEST, Bundle.getMessage(locale, "NotAClientType", type));
                }
            case JSON.HELLO:
                return doSchema(type,
                        server,
                        "jmri/server/json/util/hello-server.json",
                        "jmri/server/json/util/hello-client.json");
            default:
                throw new JsonException(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, Bundle.getMessage(locale, "ErrorUnknownType", type));
        }
    }

}
