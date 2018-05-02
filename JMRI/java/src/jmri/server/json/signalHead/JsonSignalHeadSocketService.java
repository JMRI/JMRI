package jmri.server.json.signalHead;

import static jmri.server.json.JSON.NAME;
import static jmri.server.json.JSON.PUT;
import static jmri.server.json.signalHead.JsonSignalHead.SIGNAL_HEAD;

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

/**
 *
 * @author Randall Wood (C) 2016
 */
public class JsonSignalHeadSocketService extends JsonSocketService<JsonSignalHeadHttpService> {

    private final HashMap<String, SignalHeadListener> signalHeads = new HashMap<>();

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
        if (!this.signalHeads.containsKey(name)) {
            SignalHead signalHead = InstanceManager.getDefault(SignalHeadManager.class).getSignalHead(name);
            if (signalHead != null) {
                SignalHeadListener listener = new SignalHeadListener(signalHead);
                signalHead.addPropertyChangeListener(listener);
                this.signalHeads.put(name, listener);
            }
        }
    }

    @Override
    public void onList(String type, JsonNode data, Locale locale) throws IOException, JmriException, JsonException {
        this.setLocale(locale);
        this.connection.sendMessage(this.service.doGetList(type, locale));
    }

    @Override
    public void onClose() {
        signalHeads.values().stream().forEach((reporter) -> {
            reporter.signalHead.removePropertyChangeListener(reporter);
        });
        signalHeads.clear();
    }

    private class SignalHeadListener implements PropertyChangeListener {

        protected final SignalHead signalHead;

        public SignalHeadListener(SignalHead signalHead) {
            this.signalHead = signalHead;
        }

        @Override
        public void propertyChange(PropertyChangeEvent e) {
            if (e.getPropertyName().equals("Appearance") || e.getPropertyName().equals("Held")) {
                try {
                    try {
                        connection.sendMessage(service.doGet(SIGNAL_HEAD, this.signalHead.getSystemName(), getLocale()));
                    } catch (JsonException ex) {
                        connection.sendMessage(ex.getJsonMessage());
                    }
                } catch (IOException ie) {
                    // if we get an error, de-register
                    signalHead.removePropertyChangeListener(this);
                    signalHeads.remove(this.signalHead.getSystemName());
                }
            }
        }
    }

}
