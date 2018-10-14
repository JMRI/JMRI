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
    private WebServerPreferences webServerPreferences = InstanceManager.getDefault(WebServerPreferences.class);

    public JsonUtilSocketService(JsonConnection connection) {
        super(connection, new JsonUtilHttpService(connection.getObjectMapper()));
    }

    @Override
    public void onMessage(String type, JsonNode data, String method, Locale locale) throws IOException, JmriException, JsonException {
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
            case JSON.RAILROAD:
                this.connection.sendMessage(this.service.doGet(type, name, locale));
                this.rrNameListener = (PropertyChangeEvent evt) -> {
                    try {
                        try {
                            this.connection.sendMessage(this.service.doPost(JSON.RAILROAD, null, null, this.connection.getLocale()));
                        } catch (JsonException ex) {
                            this.connection.sendMessage(ex.getJsonMessage());
                        }
                    } catch (IOException ex) {
                        webServerPreferences.removePropertyChangeListener(this.rrNameListener);
                    }
                };
                webServerPreferences.addPropertyChangeListener(this.rrNameListener);
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
        webServerPreferences.removePropertyChangeListener(this.rrNameListener);
    }

}
