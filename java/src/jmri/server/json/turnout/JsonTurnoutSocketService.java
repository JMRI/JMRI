package jmri.server.json.turnout;

import com.fasterxml.jackson.databind.JsonNode;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.HashMap;
import java.util.Locale;
import jmri.InstanceManager;
import jmri.JmriException;
import jmri.Turnout;
import jmri.TurnoutManager;
import static jmri.server.json.JSON.METHOD;
import static jmri.server.json.JSON.NAME;
import static jmri.server.json.JSON.PUT;
import jmri.server.json.JsonConnection;
import jmri.server.json.JsonException;
import jmri.server.json.JsonSocketService;
import static jmri.server.json.turnout.JsonTurnoutServiceFactory.TURNOUT;

/**
 *
 * @author Randall Wood
 */
public class JsonTurnoutSocketService extends JsonSocketService {

    private final JsonTurnoutHttpService service;
    private final HashMap<String, TurnoutListener> turnouts = new HashMap<>();
    private Locale locale;

    public JsonTurnoutSocketService(JsonConnection connection) {
        super(connection);
        this.service = new JsonTurnoutHttpService(connection.getObjectMapper());
    }

    @Override
    public void onMessage(String type, JsonNode data, Locale locale) throws IOException, JmriException, JsonException {
        this.locale = locale;
        String name = data.path(NAME).asText();
        if (data.path(METHOD).asText().equals(PUT)) {
            this.connection.sendMessage(this.service.doPut(type, name, data, locale));
        } else {
            this.connection.sendMessage(this.service.doPost(type, name, data, locale));
        }
        if (!this.turnouts.containsKey(name)) {
            Turnout turnout = InstanceManager.getDefault(TurnoutManager.class).getTurnout(name);
            TurnoutListener listener = new TurnoutListener(turnout);
            turnout.addPropertyChangeListener(listener);
            this.turnouts.put(name, listener);
        }
    }

    @Override
    public void onList(String type, JsonNode data, Locale locale) throws IOException, JmriException, JsonException {
        this.locale = locale;
        this.connection.sendMessage(this.service.doGetList(type, locale));
    }

    @Override
    public void onClose() {
        turnouts.values().stream().forEach((turnout) -> {
            turnout.turnout.removePropertyChangeListener(turnout);
        });
        turnouts.clear();
    }

    private class TurnoutListener implements PropertyChangeListener {

        protected final Turnout turnout;

        public TurnoutListener(Turnout turnout) {
            this.turnout = turnout;
        }

        @Override
        public void propertyChange(PropertyChangeEvent e) {
            // If the Commanded State changes, show transition state as "<inconsistent>"
            if (e.getPropertyName().equals("KnownState")) {
                try {
                    try {
                        connection.sendMessage(service.doGet(TURNOUT, this.turnout.getSystemName(), locale));
                    } catch (JsonException ex) {
                        connection.sendMessage(ex.getJsonMessage());
                    }
                } catch (IOException ex) {
                    // if we get an error, de-register
                    turnout.removePropertyChangeListener(this);
                    turnouts.remove(this.turnout.getSystemName());
                }
            }
        }
    }

}
