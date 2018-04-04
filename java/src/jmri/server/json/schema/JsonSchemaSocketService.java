package jmri.server.json.schema;

import com.fasterxml.jackson.databind.JsonNode;
import java.io.IOException;
import java.util.Locale;
import javax.servlet.http.HttpServletResponse;
import jmri.JmriException;
import jmri.server.json.JSON;
import jmri.server.json.JsonConnection;
import jmri.server.json.JsonException;
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
    public void onMessage(String type, JsonNode data, String method, Locale locale) throws IOException, JmriException, JsonException {
        switch (method) {
            case JSON.DELETE:
                throw new JsonException(HttpServletResponse.SC_METHOD_NOT_ALLOWED, Bundle.getMessage(locale, "DeleteNotAllowed"));
            case JSON.POST:
                throw new JsonException(HttpServletResponse.SC_METHOD_NOT_ALLOWED, Bundle.getMessage(locale, "PostNotAllowed"));
            case JSON.PUT:
                throw new JsonException(HttpServletResponse.SC_METHOD_NOT_ALLOWED, Bundle.getMessage(locale, "PutNotAllowed"));
            case JSON.GET:
                this.connection.sendMessage(this.service.doPost(type, data.path(JSON.NAME).asText(JSON.JSON), data, locale));
                break;
            default:
                throw new JsonException(HttpServletResponse.SC_METHOD_NOT_ALLOWED, Bundle.getMessage(locale, "MethodNotImplemented"));
        }
    }

    @Override
    public void onList(String type, JsonNode data, Locale locale) throws IOException, JmriException, JsonException {
        this.connection.sendMessage(this.service.doGetList(type, locale));
    }

    @Override
    public void onClose() {
        // nothing to do
    }

}
