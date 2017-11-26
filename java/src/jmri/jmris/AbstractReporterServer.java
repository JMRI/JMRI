package jmri.jmris;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import jmri.InstanceManager;
import jmri.JmriException;
import jmri.Reporter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract interface between the a JMRI reporter and a network connection
 *
 * @author Paul Bender Copyright (C) 2010
 * @author Randall Wood Copyright (C) 2013
 */
abstract public class AbstractReporterServer {

    private final HashMap<String, ReporterListener> reporters;
    private static final Logger log = LoggerFactory.getLogger(AbstractReporterServer.class);

    public AbstractReporterServer() {
        reporters = new HashMap<String, ReporterListener>();
    }

    /*
     * Protocol Specific Abstract Functions
     */
    abstract public void sendReport(String reporter, Object r) throws IOException;

    abstract public void sendErrorStatus(String reporter) throws IOException;

    abstract public void parseStatus(String statusString) throws JmriException, IOException;

    synchronized protected void addReporterToList(String reporterName) {
        if (!reporters.containsKey(reporterName)) {
            reporters.put(reporterName, new ReporterListener(reporterName));
            Reporter reporter = InstanceManager.getDefault(jmri.ReporterManager.class).getReporter(reporterName);
            if(reporter!=null) {
               reporter.addPropertyChangeListener(reporters.get(reporterName));
            }
        }
    }

    synchronized protected void removeReporterFromList(String reporterName) {
        if (reporters.containsKey(reporterName)) {
            Reporter reporter = InstanceManager.getDefault(jmri.ReporterManager.class).getReporter(reporterName);
            if(reporter!=null) {
               reporter.removePropertyChangeListener(reporters.get(reporterName));
            }
            reporters.remove(reporterName);
        }
    }

    public Reporter initReporter(String reporterName) throws IllegalArgumentException {
        Reporter reporter = InstanceManager.getDefault(jmri.ReporterManager.class).provideReporter(reporterName);
        this.addReporterToList(reporterName);
        return reporter;
    }

    /*
     * Set the report state of a reporter
     * 
     * @parm reporterName - the name of a reporter
     * @parm r - the object containing the report (currently a string).
     */
    public void setReporterReport(String reporterName, Object r) {
        Reporter reporter;
        // load address from reporterAddrTextField
        try {
            addReporterToList(reporterName);
            reporter = InstanceManager.getDefault(jmri.ReporterManager.class).getReporter(reporterName);
            if (reporter == null) {
                log.error("Reporter {} is not available", reporterName);
            } else {
                log.debug("about to set reporter State");
                reporter.setReport(r);
            }
        } catch (Exception ex) {
            log.error("set reporter report", ex);
        }
    }

    public void dispose() {
        for (Map.Entry<String, ReporterListener> entry: this.reporters.entrySet()) {
            Reporter reporter = InstanceManager.getDefault(jmri.ReporterManager.class).getReporter(entry.getKey());
            if(reporter!=null) {
               reporter.removePropertyChangeListener(entry.getValue());
            }
        }
        this.reporters.clear();
    }

    class ReporterListener implements PropertyChangeListener {

        ReporterListener(String reporterName) {
            name = reporterName;
            reporter = InstanceManager.getDefault(jmri.ReporterManager.class).getReporter(reporterName);
        }

        // update state as state of reporter changes
        @Override
        public void propertyChange(PropertyChangeEvent e) {
            // If the Commanded State changes, show transition state as "<inconsistent>"
            if (e.getPropertyName().equals("currentReport")) {
                String now;
                try {
                    now = e.getNewValue().toString();
                } catch (NullPointerException npe) {
                    // current report is null, which is expected.
                    now = null;
                }
                try {
                    sendReport(name, now);
                } catch (IOException ie) {
                    log.debug("Error Sending Status");
                    // if we get an error, de-register
                    reporter.removePropertyChangeListener(this);
                    removeReporterFromList(name);
                }
            }
        }
        String name = null;
        Reporter reporter = null;
    }
}
