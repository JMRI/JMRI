package jmri.server.json.schema;

import com.fasterxml.jackson.databind.JsonNode;
import java.io.IOException;
import javax.servlet.http.HttpServletResponse;
import jmri.JmriException;
import jmri.server.json.JSON;
import jmri.server.json.JsonConnection;
import jmri.server.json.JsonException;
import jmri.server.json.JsonRequest;
import jmri.server.json.JsonSocketService;

/**
 * JSON Service to provide schema data for the running JSON server.
 *
 * @author Randall Wood Copyright 2018
 */
public class JsonSchemaSocketService extends JsonSocketService<JsonSchemaHttpService> {

    JsonSchemaSocketService(JsonConnection connection) {
        super(connection, new JsonSchemaHttpService(connection.getObjectMapper()));
    }

    @Override
    public void onMessage(String type, JsonNode data, JsonRequest request) throws IOException, JmriException, JsonException {
        switch (request.method) {
            case JSON.DELETE:
                throw new JsonException(HttpServletResponse.SC_METHOD_NOT_ALLOWED, Bundle.getMessage(request.locale, "DeleteNotAllowed", type), request.id);
            case JSON.POST:
                throw new JsonException(HttpServletResponse.SC_METHOD_NOT_ALLOWED, Bundle.getMessage(request.locale, "PostNotAllowed", type), request.id);
            case JSON.PUT:
                throw new JsonException(HttpServletResponse.SC_METHOD_NOT_ALLOWED, Bundle.getMessage(request.locale, "PutNotAllowed", type), request.id);
            case JSON.GET:
                connection.sendMessage(service.doGet(type, data.path(JSON.NAME).asText(JSON.JSON), data, request), request.id);
                break;
            default:
                throw new JsonException(HttpServletResponse.SC_METHOD_NOT_ALLOWED, Bundle.getMessage(request.locale, "MethodNotImplemented", request.method, type), request.id);
        }
    }

    @Override
    public void onList(String type, JsonNode data, JsonRequest request) throws IOException, JmriException, JsonException {
        connection.sendMessage(service.doGetList(type, data, request), request.id);
    }

    @Override
    public void onClose() {
        // nothing to do
    }

}
