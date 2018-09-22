package jmri.server.json.signalHead;

import static jmri.server.json.JSON.NAME;
import static jmri.server.json.JSON.PUT;
import static jmri.server.json.signalHead.JsonSignalHead.SIGNAL_HEAD;
import static jmri.server.json.signalHead.JsonSignalHead.SIGNAL_HEADS;

import com.fasterxml.jackson.databind.JsonNode;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.HashMap;
import java.util.Locale;
import jmri.InstanceManager;
import jmri.JmriException;
import jmri.SignalHead;
import jmri.SignalHeadManager;
import jmri.server.json.JsonConnection;
import jmri.server.json.JsonException;
import jmri.server.json.JsonSocketService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Randall Wood (C) 2016
 */
public class JsonSignalHeadSocketService extends JsonSocketService<JsonSignalHeadHttpService> {

    private final HashMap<String, SignalHeadListener> signalHeadListeners = new HashMap<>();
    private final SignalHeadsListener signalHeadsListener = new SignalHeadsListener();
    private final static Logger log = LoggerFactory.getLogger(JsonSignalHeadSocketService.class);

    public JsonSignalHeadSocketService(JsonConnection connection) {
        super(connection, new JsonSignalHeadHttpService(connection.getObjectMapper()));
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
        if (!this.signalHeadListeners.containsKey(name)) {
            SignalHead signalHead = InstanceManager.getDefault(SignalHeadManager.class).getSignalHead(name);
            if (signalHead != null) {
                SignalHeadListener listener = new SignalHeadListener(signalHead);
                signalHead.addPropertyChangeListener(listener);
                this.signalHeadListeners.put(name, listener);
            }
        }
    }

    @Override
    public void onList(String type, JsonNode data, Locale locale) throws IOException, JmriException, JsonException {
        this.setLocale(locale);
        this.connection.sendMessage(this.service.doGetList(type, locale));
        log.debug("adding SignalHeadsListener");
        InstanceManager.getDefault(SignalHeadManager.class).addPropertyChangeListener(signalHeadsListener); //add parent listener
        addListenersToChildren();
    }

    private void addListenersToChildren() {
        InstanceManager.getDefault(SignalHeadManager.class).getSystemNameList().stream().forEach((shn) -> { //add listeners to each child (if not already)
            if (!signalHeadListeners.containsKey(shn)) {
                log.debug("adding SignalHeadListener for SignalHead '{}'", shn);
                SignalHead sh = InstanceManager.getDefault(SignalHeadManager.class).getSignalHead(shn);
                if (sh != null) {
                    signalHeadListeners.put(shn, new SignalHeadListener(sh));
                    sh.addPropertyChangeListener(this.signalHeadListeners.get(shn));
                }
            }
        });
    }    

    @Override
    public void onClose() {
        signalHeadListeners.values().stream().forEach((reporter) -> {
            reporter.signalHead.removePropertyChangeListener(reporter);
        });
        signalHeadListeners.clear();
    }

    private class SignalHeadListener implements PropertyChangeListener {

        protected final SignalHead signalHead;

        public SignalHeadListener(SignalHead signalHead) {
            this.signalHead = signalHead;
        }

        @Override
        public void propertyChange(PropertyChangeEvent e) {
//            if (e.getPropertyName().equals("Appearance") || e.getPropertyName().equals("Held")) {
                try {
                    try {
                        connection.sendMessage(service.doGet(SIGNAL_HEAD, this.signalHead.getSystemName(), getLocale()));
                    } catch (JsonException ex) {
                        connection.sendMessage(ex.getJsonMessage());
                    }
                } catch (IOException ie) {
                    // if we get an error, de-register
                    signalHead.removePropertyChangeListener(this);
                    signalHeadListeners.remove(this.signalHead.getSystemName());
                }
//            }
        }
    }
    
    private class SignalHeadsListener implements PropertyChangeListener {
        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            log.debug("in SignalHeadsListener for '{}' ('{}' => '{}')", evt.getPropertyName(), evt.getOldValue(), evt.getNewValue());

            try {
                try {
                 // send the new list
                    connection.sendMessage(service.doGetList(SIGNAL_HEADS, getLocale()));
                    //child added or removed, reset listeners
                    if (evt.getPropertyName().equals("length")) { // NOI18N
                        addListenersToChildren();
                    }
                } catch (JsonException ex) {
                    log.warn("json error sending SignalHeads: {}", ex.getJsonMessage());
                    connection.sendMessage(ex.getJsonMessage());
                }
            } catch (IOException ex) {
                // if we get an error, de-register
                log.debug("deregistering signalHeadsListener due to IOException");
                InstanceManager.getDefault(SignalHeadManager.class).removePropertyChangeListener(signalHeadsListener);
            }
        }
    }

}
