package jmri.server.json.logixngicon;

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
 * JSON socket service provider for managing {@link jmri.jmrit.display.LogixNGIcon}s.
 *
 * @author Randall Wood
 * @author Daniel Bergqvist (C) 2023
 */
public class JsonLogixNGIconSocketService extends JsonSocketService<JsonLogixNGIconHttpService> {

    public JsonLogixNGIconSocketService(JsonConnection connection) {
        super(connection, new JsonLogixNGIconHttpService(connection.getObjectMapper()));
    }

    @Override
    public void onList(String type, JsonNode data, JsonRequest request) throws IOException, JmriException, JsonException {
        throw new JsonException(HttpServletResponse.SC_METHOD_NOT_ALLOWED, Bundle.getMessage(request.locale, "GetListNotAllowed", type), request.id);
    }

    @Override
    public void onMessage(String type, JsonNode data, JsonRequest request) throws IOException, JmriException, JsonException {
        if (request.method.equals(JSON.POST)) {
            connection.sendMessage(service.doPost(type, "logixngicon", data, request), request.id);
        }
    }

    @Override
    public void onClose() {
    }

}
