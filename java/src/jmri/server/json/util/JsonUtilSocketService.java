package jmri.server.json.util;

import com.fasterxml.jackson.databind.JsonNode;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.Locale;
import jmri.InstanceManager;
import jmri.JmriException;
import jmri.server.json.JSON;
import jmri.server.json.JsonConnection;
import jmri.server.json.JsonException;
import jmri.server.json.JsonSocketService;
import jmri.web.server.WebServerPreferences;

/**
 *
 * @author Randall Wood
 */
public class JsonUtilSocketService extends JsonSocketService<JsonUtilHttpService> {

    private PropertyChangeListener rrNameListener;

    public JsonUtilSocketService(JsonConnection connection) {
        super(connection, new JsonUtilHttpService(connection.getObjectMapper()));
    }

    /**
     * Package protected method for unit testing that allows a test HTTP service
     * to be used.
     * 
     * @param connection the connection to use
     * @param service    the supporting HTTP service
     */
    JsonUtilSocketService(JsonConnection connection, JsonUtilHttpService service) {
        super(connection, service);
    }

    @Override
    public void onMessage(String type, JsonNode data, String method, Locale locale, int id) throws IOException, JmriException, JsonException {
        String name = data.path(JSON.NAME).asText();
        switch (type) {
            case JSON.LOCALE:
                // do nothing - we only want to prevent an error at this point
                break;
            case JSON.PING:
                this.connection.sendMessage(this.connection.getObjectMapper().createObjectNode().put(JSON.TYPE, JSON.PONG), id);
                break;
            case JSON.GOODBYE:
                this.connection.sendMessage(this.connection.getObjectMapper().createObjectNode().put(JSON.TYPE, JSON.GOODBYE), id);
                break;
            case JSON.RAILROAD:
                this.connection.sendMessage(this.service.doGet(type, name, data, locale, id), id);
                this.rrNameListener = (PropertyChangeEvent evt) -> {
                    try {
                        this.handleRailroadChange();
                    } catch (IOException ex) {
                        InstanceManager.getDefault(WebServerPreferences.class).removePropertyChangeListener(this.rrNameListener);
                    }
                };
                InstanceManager.getOptionalDefault(WebServerPreferences.class).ifPresent(preferences -> preferences.addPropertyChangeListener(this.rrNameListener));
                break;
            default:
                this.connection.sendMessage(this.service.doPost(type, name, data, locale, id), id);
                break;
        }
    }

    @Override
    public void onList(String type, JsonNode data, Locale locale, int id) throws IOException, JmriException, JsonException {
        this.connection.sendMessage(this.service.doGetList(type, data, locale, id), id);
    }

    @Override
    public void onClose() {
        InstanceManager.getOptionalDefault(WebServerPreferences.class).ifPresent(preferences -> preferences.removePropertyChangeListener(this.rrNameListener));
    }

    private void handleRailroadChange() throws IOException {
        try {
            this.connection.sendMessage(this.service.doPost(JSON.RAILROAD, null, this.service.getObjectMapper().createObjectNode(), this.connection.getLocale(), 0), 0);
        } catch (JsonException ex) {
            this.connection.sendMessage(ex.getJsonMessage(), 0);
        }
    }
}
