package jmri.server.json.signalMast;

import static jmri.server.json.JSON.NAME;
import static jmri.server.json.JSON.PUT;
import static jmri.server.json.signalMast.JsonSignalMast.SIGNAL_MAST;

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

/**
 *
 * @author Randall Wood (C) 2016
 */
public class JsonSignalMastSocketService extends JsonSocketService<JsonSignalMastHttpService> {

    private final HashMap<String, SignalMastListener> signalMasts = new HashMap<>();

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
        if (!this.signalMasts.containsKey(name)) {
            SignalMast signalMast = InstanceManager.getDefault(SignalMastManager.class).getSignalMast(name);
            if (signalMast != null) {
                SignalMastListener listener = new SignalMastListener(signalMast);
                signalMast.addPropertyChangeListener(listener);
                this.signalMasts.put(name, listener);
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
        signalMasts.values().stream().forEach((reporter) -> {
            reporter.signalMast.removePropertyChangeListener(reporter);
        });
        signalMasts.clear();
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
                    signalMasts.remove(this.signalMast.getSystemName());
                }
            }
        }
    }

}
