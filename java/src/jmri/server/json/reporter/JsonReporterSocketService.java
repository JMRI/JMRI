package jmri.server.json.reporter;

import static jmri.server.json.JSON.NAME;
import static jmri.server.json.JSON.PUT;
import static jmri.server.json.reporter.JsonReporter.REPORTER;
import static jmri.server.json.reporter.JsonReporter.REPORTERS;

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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Randall Wood (C) 2016
 */
public class JsonReporterSocketService extends JsonSocketService<JsonReporterHttpService> {

    private final HashMap<String, ReporterListener> reporterListeners = new HashMap<>();
    private final ReportersListener reportersListener = new ReportersListener();
    private final static Logger log = LoggerFactory.getLogger(JsonReporterSocketService.class);


    public JsonReporterSocketService(JsonConnection connection) {
        super(connection, new JsonReporterHttpService(connection.getObjectMapper()));
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
        if (!this.reporterListeners.containsKey(name)) {
            Reporter reporter = InstanceManager.getDefault(ReporterManager.class).getReporter(name);
            if (reporter != null) {
                ReporterListener listener = new ReporterListener(reporter);
                reporter.addPropertyChangeListener(listener);
                this.reporterListeners.put(name, listener);
            }
        }
    }

    @Override
    public void onList(String type, JsonNode data, Locale locale) throws IOException, JmriException, JsonException {
        this.setLocale(locale);
        this.connection.sendMessage(this.service.doGetList(type, locale));
        log.debug("adding ReportersListener");
        InstanceManager.getDefault(ReporterManager.class).addPropertyChangeListener(reportersListener); //add parent listener
        addListenersToChildren();
    }

    private void addListenersToChildren() {
        InstanceManager.getDefault(ReporterManager.class).getSystemNameList().stream().forEach((rn) -> { //add listeners to each child (if not already)
            if (!reporterListeners.containsKey(rn)) {
                log.debug("adding ReporterListener for Reporter '{}'", rn);
                Reporter r = InstanceManager.getDefault(ReporterManager.class).getReporter(rn);
                if (r != null) {
                    reporterListeners.put(rn, new ReporterListener(r));
                    r.addPropertyChangeListener(this.reporterListeners.get(rn));
                }
            }
        });
    }    

    @Override
    public void onClose() {
        reporterListeners.values().stream().forEach((reporter) -> {
            reporter.reporter.removePropertyChangeListener(reporter);
        });
        reporterListeners.clear();
    }

    private class ReporterListener implements PropertyChangeListener {

        protected final Reporter reporter;

        public ReporterListener(Reporter reporter) {
            this.reporter = reporter;
        }

        @Override
        public void propertyChange(PropertyChangeEvent e) {
//            if (e.getPropertyName().equals("currentReport")) {
                try {
                    try {
                        connection.sendMessage(service.doGet(REPORTER, this.reporter.getSystemName(), getLocale()));
                    } catch (JsonException ex) {
                        connection.sendMessage(ex.getJsonMessage());
                    }
                } catch (IOException ex) {
                    // if we get an error, de-register
                    reporter.removePropertyChangeListener(this);
                    reporterListeners.remove(this.reporter.getSystemName());
                }
//            }
        }
    }
    
    private class ReportersListener implements PropertyChangeListener {
        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            log.debug("in ReportersListener for '{}' ('{}' => '{}')", evt.getPropertyName(), evt.getOldValue(), evt.getNewValue());

            try {
                try {
                 // send the new list
                    connection.sendMessage(service.doGetList(REPORTERS, getLocale()));
                    //child added or removed, reset listeners
                    if (evt.getPropertyName().equals("length")) { // NOI18N
                        addListenersToChildren();
                    }
                } catch (JsonException ex) {
                    log.warn("json error sending Reporters: {}", ex.getJsonMessage());
                    connection.sendMessage(ex.getJsonMessage());
                }
            } catch (IOException ex) {
                // if we get an error, de-register
                log.debug("deregistering reportersListener due to IOException");
                InstanceManager.getDefault(ReporterManager.class).removePropertyChangeListener(reportersListener);
            }
        }
    }
    

}
