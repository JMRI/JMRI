package jmri.jmris.simpleserver;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import jmri.InstanceManager;
import jmri.JmriException;
import jmri.Reporter;
import jmri.jmris.AbstractReporterServer;
import jmri.jmris.JmriConnection;

/**
 * Simple Server interface between the JMRI reporter manager and a network
 * connection
 *
 * @author Paul Bender Copyright (C) 2011
 * @author Randall Wood Copyright (C) 2013
 */
public class SimpleReporterServer extends AbstractReporterServer {

    private DataOutputStream output;
    private JmriConnection connection;

    public SimpleReporterServer(JmriConnection connection) {
        super();
        this.connection = connection;
    }

    public SimpleReporterServer(DataInputStream inStream, DataOutputStream outStream) {
        super();
        output = outStream;
    }


    /*
     * Protocol Specific Abstract Functions
     */
    @Override
    public void sendReport(String reporterName, Object r) throws IOException {
        addReporterToList(reporterName);
        if (r != null) {
            if (r instanceof jmri.Reportable ) {
               this.sendMessage("REPORTER " + reporterName + " " + ((jmri.Reportable)r).toReportString() + "\n");
            } else {
               this.sendMessage("REPORTER " + reporterName + " " + r.toString() + "\n");
            }
        } else {
            this.sendMessage("REPORTER " + reporterName + "\n");
        }
    }

    @Override
    public void sendErrorStatus(String reporterName) throws IOException {
        this.sendMessage("REPORTER ERROR\n");
    }

    @Override
    public void parseStatus(String statusString) throws JmriException, IOException {
        int index, index2;
        index = statusString.indexOf(" ") + 1;
        index2 = statusString.indexOf(" ", index + 1);
        int newlinepos = statusString.indexOf("\n");
        String reporterName = statusString.substring(index, index2>0?index2:newlinepos);
        initReporter(reporterName);
        // the string should be "REPORTER xxxxxx REPORTSTRING\n\r"
        // where xxxxxx is the reporter identifier and REPORTSTRING is
        // the report, which may contain spaces.
        if (index2 > 0 && ( newlinepos - (index2 + 1) > 0)) {
            setReporterReport(reporterName, statusString.substring(index2 + 1,newlinepos));
            // setReporterReport ALSO triggers sending the status report, so 
            // no further action is required to echo the status to the client.
        } else {
            // send the current status if the report
            try {
               Reporter reporter = InstanceManager.getDefault(jmri.ReporterManager.class).provideReporter(reporterName);
               sendReport(reporterName, reporter.getCurrentReport());
            } catch (IllegalArgumentException ex) {
                log.warn("Failed to provide Reporter \"{}\" in parseStatus", reporterName);
            }
        }
    }

    private void sendMessage(String message) throws IOException {
        if (this.output != null) {
            this.output.writeBytes(message);
        } else {
            this.connection.sendMessage(message);
        }
    }

    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(SimpleReporterServer.class);
}
