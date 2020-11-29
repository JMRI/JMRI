package jmri.server.json.message;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import java.io.IOException;
import javax.servlet.http.HttpServletResponse;
import jmri.server.json.JSON;
import jmri.server.json.JsonException;
import jmri.server.json.JsonHttpService;
import jmri.server.json.JsonRequest;

/**
 *
 * @author Randall Wood Copyright 2018
 */
public class JsonMessageHttpService extends JsonHttpService {

    public JsonMessageHttpService(ObjectMapper mapper) {
        super(mapper);
    }

    @Override
    public JsonNode doGet(String type, String name, JsonNode data, JsonRequest request) throws JsonException {
        throw new JsonException(HttpServletResponse.SC_METHOD_NOT_ALLOWED, Bundle.getMessage(request.locale, "GetNotAllowed", type), request.id);
    }

    @Override
    public JsonNode doPost(String type, String name, JsonNode data, JsonRequest request) throws JsonException {
        throw new JsonException(HttpServletResponse.SC_METHOD_NOT_ALLOWED, Bundle.getMessage(request.locale, "PostNotAllowed", type), request.id);
    }

    @Override
    public ArrayNode doGetList(String type, JsonNode data, JsonRequest request) throws JsonException {
        throw new JsonException(HttpServletResponse.SC_BAD_REQUEST, Bundle.getMessage(request.locale, "UnlistableService", type), request.id);
    }

    @Override
    public JsonNode doSchema(String type, boolean server, JsonRequest request) throws JsonException {
        switch (type) {
            case JsonMessage.CLIENT:
                return doSchema(type,
                        server,
                        "jmri/server/json/message/client-server.json",
                        "jmri/server/json/message/client-client.json",
                        request.id);
            case JsonMessage.MESSAGE:
                if (server) {
                    try {
                        return doSchema(type, server,
                                this.mapper.readTree(this.getClass().getClassLoader().getResource("jmri/server/json/message/message-server.json")), request.id);
                    } catch (IOException ex) {
                        throw new JsonException(500, ex, request.id);
                    }
                } else {
                    throw new JsonException(HttpServletResponse.SC_BAD_REQUEST, Bundle.getMessage(request.locale, "NotAClientType", type), request.id);
                }
            case JSON.HELLO:
                return doSchema(type,
                        server,
                        "jmri/server/json/util/hello-server.json",
                        "jmri/server/json/util/hello-client.json",
                        request.id);
            default:
                throw new JsonException(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, Bundle.getMessage(request.locale, JsonException.ERROR_UNKNOWN_TYPE, type), request.id);
        }
    }

}
