package jmri.server.json.turnout;

import static jmri.server.json.JSON.NAME;
import static jmri.server.json.JSON.PUT;
import static jmri.server.json.turnout.JsonTurnoutServiceFactory.TURNOUT;
import static jmri.server.json.turnout.JsonTurnoutServiceFactory.TURNOUTS;

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
import jmri.server.json.JsonConnection;
import jmri.server.json.JsonException;
import jmri.server.json.JsonSocketService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 *
 * @author Randall Wood
 */
public class JsonTurnoutSocketService extends JsonSocketService<JsonTurnoutHttpService> {

    private final HashMap<String, TurnoutListener> turnoutListeners = new HashMap<>();
    private final TurnoutsListener turnoutsListener = new TurnoutsListener();
    private final static Logger log = LoggerFactory.getLogger(JsonTurnoutSocketService.class);


    public JsonTurnoutSocketService(JsonConnection connection) {
        super(connection, new JsonTurnoutHttpService(connection.getObjectMapper()));
    }

    @Override
    public void onMessage(String type, JsonNode data, String method, Locale locale) throws IOException, JmriException, JsonException {
        this.setLocale(locale);
        String name = data.path(NAME).asText();
        if (method.equals(PUT)) {
            this.connection.sendMessage(this.service.doPut(type, name, data, locale));
        } else {
            this.connection.sendMessage(this.service.doPost(type, name, data, locale));
        }
        if (!this.turnoutListeners.containsKey(name)) {
            Turnout turnout = InstanceManager.getDefault(TurnoutManager.class).getTurnout(name);
            TurnoutListener listener = new TurnoutListener(turnout);
            if (turnout != null) {
                turnout.addPropertyChangeListener(listener);
                this.turnoutListeners.put(name, listener);
            }
        }
    }

    @Override
    public void onList(String type, JsonNode data, Locale locale) throws IOException, JmriException, JsonException {
        this.setLocale(locale);
        this.connection.sendMessage(this.service.doGetList(type, locale));

        log.debug("adding TurnoutsListener");
        InstanceManager.getDefault(TurnoutManager.class).addPropertyChangeListener(turnoutsListener); //add parent listener
        addListenersToChildren();

    }

    private void addListenersToChildren() {
        InstanceManager.getDefault(TurnoutManager.class).getSystemNameList().stream().forEach((tn) -> { //add listeners to each child (if not already)
            if (!turnoutListeners.containsKey(tn)) {
                log.debug("adding TurnoutListener for Turnout {}", tn);
                Turnout t = InstanceManager.getDefault(TurnoutManager.class).getTurnout(tn);
                if (t != null) {
                    turnoutListeners.put(tn, new TurnoutListener(t));
                    t.addPropertyChangeListener(this.turnoutListeners.get(tn));
                }
            }
        });
    }


    @Override
    public void onClose() {
        turnoutListeners.values().stream().forEach((turnout) -> {
            turnout.turnout.removePropertyChangeListener(turnout);
        });
        turnoutListeners.clear();
        InstanceManager.getDefault(TurnoutManager.class).removePropertyChangeListener(turnoutsListener);
    }

    private class TurnoutListener implements PropertyChangeListener {

        protected final Turnout turnout;

        public TurnoutListener(Turnout turnout) {
            this.turnout = turnout;
        }

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            log.debug("in TurnoutListener for '{}' '{}' ('{}'=>'{}')", this.turnout.getSystemName(), evt.getPropertyName(), evt.getOldValue(), evt.getNewValue());
            if (evt.getPropertyName().equals("KnownState")  //only send changes for values which are sent
                    || evt.getPropertyName().equals("inverted")
                    || evt.getPropertyName().equals("UserName")
                    || evt.getPropertyName().equals("Comment")) {
                try {
                    try {
                        connection.sendMessage(service.doGet(TURNOUT, this.turnout.getSystemName(), getLocale()));
                    } catch (JsonException ex) {
                        connection.sendMessage(ex.getJsonMessage());
                    }
                } catch (IOException ex) {
                    // if we get an error, de-register
                    turnout.removePropertyChangeListener(this);
                    turnoutListeners.remove(this.turnout.getSystemName());
                }
            }
        }
    }
    private class TurnoutsListener implements PropertyChangeListener {
        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            log.debug("in TurnoutsListener for '{}' ('{}' => '{}')", evt.getPropertyName(), evt.getOldValue(), evt.getNewValue());

            try {
                try {
                 // send the new list
                    connection.sendMessage(service.doGetList(TURNOUTS, getLocale()));
                    //child added or removed, reset listeners
                    if (evt.getPropertyName().equals("length")) { // NOI18N
                        addListenersToChildren();
                    }
                } catch (JsonException ex) {
                    log.warn("json error sending Turnouts: {}", ex.getJsonMessage());
                    connection.sendMessage(ex.getJsonMessage());
                }
            } catch (IOException ex) {
                // if we get an error, de-register
                log.debug("deregistering turnoutsListener due to IOException");
                InstanceManager.getDefault(TurnoutManager.class).removePropertyChangeListener(turnoutsListener);
            }
        }
    }

}
