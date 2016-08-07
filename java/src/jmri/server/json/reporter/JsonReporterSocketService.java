package jmri.server.json.reporter;

import static jmri.server.json.JSON.METHOD;
import static jmri.server.json.JSON.NAME;
import static jmri.server.json.JSON.PUT;
import static jmri.server.json.reporter.JsonReporter.REPORTER;

import com.fasterxml.jackson.databind.JsonNode;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.HashMap;
import java.util.Locale;
import jmri.InstanceManager;
import jmri.JmriException;
import jmri.Reporter;
import jmri.ReporterManager;
import jmri.server.json.JsonConnection;
import jmri.server.json.JsonException;
import jmri.server.json.JsonSocketService;

/**
 *
 * @author Randall Wood (C) 2016
 */
public class JsonReporterSocketService extends JsonSocketService {

    private final JsonReporterHttpService service;
    private final HashMap<String, ReporterListener> reporters = new HashMap<>();
    private Locale locale;

    public JsonReporterSocketService(JsonConnection connection) {
        super(connection);
        this.service = new JsonReporterHttpService(connection.getObjectMapper());
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
        if (!this.reporters.containsKey(name)) {
            Reporter reporter = InstanceManager.getDefault(ReporterManager.class).getReporter(name);
            if (reporter != null) {
                ReporterListener listener = new ReporterListener(reporter);
                reporter.addPropertyChangeListener(listener);
                this.reporters.put(name, listener);
            }
        }
    }

    @Override
    public void onList(String type, JsonNode data, Locale locale) throws IOException, JmriException, JsonException {
        this.locale = locale;
        this.connection.sendMessage(this.service.doGetList(type, locale));
    }

    @Override
    public void onClose() {
        reporters.values().stream().forEach((reporter) -> {
            reporter.reporter.removePropertyChangeListener(reporter);
        });
        reporters.clear();
    }

    private class ReporterListener implements PropertyChangeListener {

        protected final Reporter reporter;

        public ReporterListener(Reporter reporter) {
            this.reporter = reporter;
        }

        @Override
        public void propertyChange(PropertyChangeEvent e) {
            // If the Commanded State changes, show transition state as "<inconsistent>"
            if (e.getPropertyName().equals("currentReport")) {
                try {
                    try {
                        connection.sendMessage(service.doGet(REPORTER, this.reporter.getSystemName(), locale));
                    } catch (JsonException ex) {
                        connection.sendMessage(ex.getJsonMessage());
                    }
                } catch (IOException ex) {
                    // if we get an error, de-register
                    reporter.removePropertyChangeListener(this);
                    reporters.remove(this.reporter.getSystemName());
                }
            }
        }
    }

}
