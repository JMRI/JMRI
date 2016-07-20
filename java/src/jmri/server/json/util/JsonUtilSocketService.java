package jmri.server.json.util;

import com.fasterxml.jackson.databind.JsonNode;
import java.io.IOException;
import java.util.Locale;
import jmri.JmriException;
import jmri.server.json.JSON;
import jmri.server.json.JsonConnection;
import jmri.server.json.JsonException;
import jmri.server.json.JsonSocketService;

/**
 *
 * @author Randall Wood
 */
public class JsonUtilSocketService extends JsonSocketService {

    private final JsonUtilHttpService service;

    public JsonUtilSocketService(JsonConnection connection) {
        super(connection);
        this.service = new JsonUtilHttpService(connection.getObjectMapper());
    }

    @Override
    public void onMessage(String type, JsonNode data, Locale locale) throws IOException, JmriException, JsonException {
        String name = data.path(JSON.NAME).asText();
        switch (type) {
            case JSON.LOCALE:
                // do nothing - we only want to prevent an error at this point
                break;
            case JSON.PING:
                this.connection.sendMessage(this.connection.getObjectMapper().createObjectNode().put(JSON.TYPE, JSON.PONG));
                break;
            case JSON.GOODBYE:
                this.connection.sendMessage(this.connection.getObjectMapper().createObjectNode().put(JSON.TYPE, JSON.GOODBYE));
                break;
            default:
                this.connection.sendMessage(this.service.doPost(type, name, data, locale));
                break;
        }
    }

    @Override
    public void onList(String type, JsonNode data, Locale locale) throws IOException, JmriException, JsonException {
        this.connection.sendMessage(this.service.doGetList(type, locale));
    }

    @Override
    public void onClose() {
        // nothing to cleanup
    }

}
