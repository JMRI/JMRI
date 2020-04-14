package jmri.server.json;

import static jmri.server.json.JsonTestServiceFactory.TEST;

import com.fasterxml.jackson.databind.JsonNode;
import java.io.IOException;
import javax.servlet.http.HttpServletResponse;
import jmri.JmriException;

/**
 * JSON Test Socket service.
 *
 * @author Randall Wood Copyright 2018
 */
public class JsonTestSocketService extends JsonSocketService<JsonTestHttpService> {

    public JsonTestSocketService(JsonConnection connection) {
        super(connection, new JsonTestHttpService(connection.getObjectMapper()));
    }

    @Override
    public void onMessage(String type, JsonNode data, JsonRequest request) throws IOException, JmriException, JsonException {
        if (data.path("throws").asText("").equals("JmriException")) {
            throw new JmriException(); // thrown for testing purposes
        }
        switch (request.method) {
            case JSON.GET:
                connection.sendMessage(service.doGet(type, data.path(JSON.NAME).asText(), data, request), request.id);
                break;
            case JSON.POST:
                connection.sendMessage(service.doPost(type, data.path(JSON.NAME).asText(), data, request), request.id);
                break;
            default:
                throw new JsonException(HttpServletResponse.SC_METHOD_NOT_ALLOWED, Bundle.getMessage(request.locale, "ErrorObject", TEST, data.path(JSON.NAME).asText()), request.id);
        }
    }

    /**
     * Return an array of two empty objects.
     * <p>
     * {@inheritDoc}
     */
    @Override
    public void onList(String type, JsonNode data, JsonRequest request) throws IOException, JmriException, JsonException {
        connection.sendMessage(service.doGetList(type, data, request), request.id);
    }

    @Override
    public void onClose() {
        // nothing to do
    }

}
