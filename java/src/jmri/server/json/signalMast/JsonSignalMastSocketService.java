package jmri.server.json.signalMast;

import static jmri.server.json.JSON.NAME;
import static jmri.server.json.JSON.PUT;
import static jmri.server.json.signalMast.JsonSignalMast.SIGNAL_MAST;
import static jmri.server.json.signalMast.JsonSignalMast.SIGNAL_MASTS;

import com.fasterxml.jackson.databind.JsonNode;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.HashMap;
import java.util.Locale;
import jmri.InstanceManager;
import jmri.JmriException;
import jmri.SignalMast;
import jmri.SignalMastManager;
import jmri.server.json.JsonConnection;
import jmri.server.json.JsonException;
import jmri.server.json.JsonSocketService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Randall Wood (C) 2016
 */
public class JsonSignalMastSocketService extends JsonSocketService<JsonSignalMastHttpService> {

    private final HashMap<String, SignalMastListener> signalMastListeners = new HashMap<>();
    private final SignalMastsListener signalMastsListener = new SignalMastsListener();
    private final static Logger log = LoggerFactory.getLogger(JsonSignalMastSocketService.class);

    public JsonSignalMastSocketService(JsonConnection connection) {
        super(connection, new JsonSignalMastHttpService(connection.getObjectMapper()));
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
        if (!this.signalMastListeners.containsKey(name)) {
            SignalMast signalMast = InstanceManager.getDefault(SignalMastManager.class).getSignalMast(name);
            if (signalMast != null) {
                SignalMastListener listener = new SignalMastListener(signalMast);
                signalMast.addPropertyChangeListener(listener);
                this.signalMastListeners.put(name, listener);
            }
        }
    }

    @Override
    public void onList(String type, JsonNode data, Locale locale) throws IOException, JmriException, JsonException {
        this.setLocale(locale);
        this.connection.sendMessage(this.service.doGetList(type, locale));
        log.debug("adding SignalMastsListener");
        InstanceManager.getDefault(SignalMastManager.class).addPropertyChangeListener(signalMastsListener); //add parent listener
        addListenersToChildren();
    }

    private void addListenersToChildren() {
        InstanceManager.getDefault(SignalMastManager.class).getSystemNameList().stream().forEach((smn) -> { //add listeners to each child (if not already)
            if (!signalMastListeners.containsKey(smn)) {
                log.debug("adding SignalMastListener for SignalMast '{}'", smn);
                SignalMast sm = InstanceManager.getDefault(SignalMastManager.class).getSignalMast(smn);
                if (sm != null) {
                    signalMastListeners.put(smn, new SignalMastListener(sm));
                    sm.addPropertyChangeListener(this.signalMastListeners.get(smn));
                }
            }
        });
    }    

    @Override
    public void onClose() {
        signalMastListeners.values().stream().forEach((reporter) -> {
            reporter.signalMast.removePropertyChangeListener(reporter);
        });
        signalMastListeners.clear();
    }

    private class SignalMastListener implements PropertyChangeListener {

        protected final SignalMast signalMast;

        public SignalMastListener(SignalMast signalMast) {
            this.signalMast = signalMast;
        }

        @Override
        public void propertyChange(PropertyChangeEvent e) {
            if (e.getPropertyName().equals("Aspect")
                    || e.getPropertyName().equals("Held")
                    || e.getPropertyName().equals("Lit")) {
                try {
                    try {
                        connection.sendMessage(service.doGet(SIGNAL_MAST, this.signalMast.getSystemName(), getLocale()));
                    } catch (JsonException ex) {
                        connection.sendMessage(ex.getJsonMessage());
                    }
                } catch (IOException ie) {
                    // if we get an error, de-register
                    signalMast.removePropertyChangeListener(this);
                    signalMastListeners.remove(this.signalMast.getSystemName());
                }
            }
        }
    }
    
    private class SignalMastsListener implements PropertyChangeListener {
        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            log.debug("in SignalMastsListener for '{}' ('{}' => '{}')", evt.getPropertyName(), evt.getOldValue(), evt.getNewValue());

            try {
                try {
                 // send the new list
                    connection.sendMessage(service.doGetList(SIGNAL_MASTS, getLocale()));
                    //child added or removed, reset listeners
                    if (evt.getPropertyName().equals("length")) { // NOI18N
                        addListenersToChildren();
                    }
                } catch (JsonException ex) {
                    log.warn("json error sending SignalMasts: {}", ex.getJsonMessage());
                    connection.sendMessage(ex.getJsonMessage());
                }
            } catch (IOException ex) {
                // if we get an error, de-register
                log.debug("deregistering signalMastsListener due to IOException");
                InstanceManager.getDefault(SignalMastManager.class).removePropertyChangeListener(signalMastsListener);
            }
        }
    }

}
