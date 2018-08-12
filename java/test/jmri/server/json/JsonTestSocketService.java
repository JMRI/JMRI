package jmri.server.json;

import static jmri.server.json.JsonTestServiceFactory.TEST;

import com.fasterxml.jackson.databind.JsonNode;
import java.io.IOException;
import java.util.Locale;
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
    public void onMessage(String type, JsonNode data, String method, Locale locale) throws IOException, JmriException, JsonException {
        if (data.path("throws").asText("").equals("JmriException")) {
            throw new JmriException(); // thrown for testing purposes
        }
        switch (method) {
            case JSON.GET:
                connection.sendMessage(service.doGet(type, data.path(JSON.NAME).asText(), locale));
                break;
            case JSON.POST:
                connection.sendMessage(service.doPost(type, data.path(JSON.NAME).asText(), data, locale));
                break;
            default:
                throw new JsonException(HttpServletResponse.SC_METHOD_NOT_ALLOWED, Bundle.getMessage(locale, "ErrorObject", TEST, data.path(JSON.NAME).asText()));
        }
    }

    /**
     * Return an array of two empty objects.
     * <p>
     * {@inheritDoc}
     */
    @Override
    public void onList(String type, JsonNode data, Locale locale) throws IOException, JmriException, JsonException {
        connection.sendMessage(service.doGetList(type, locale));
    }

    @Override
    public void onClose() {
        // nothing to do
    }

}
