//SimpleReporterServer.java
package jmri.jmris.simpleserver;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import jmri.InstanceManager;
import jmri.JmriException;
import jmri.Reporter;
import jmri.jmris.AbstractReporterServer;
import jmri.jmris.JmriConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Simple Server interface between the JMRI reporter manager and a network
 * connection
 *
 * @author Paul Bender Copyright (C) 2011
 * @author Randall Wood Copyright (C) 2013
 * @version $Revision$
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
            this.sendMessage("REPORTER " + reporterName + " " + r.toString() + "\n");
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
        initReporter(statusString.substring(index, index2>0?index2:statusString.length()));
        // the string should be "REPORTER xxxxxx REPORTSTRING\n\r"
        // where xxxxxx is the reporter identifier and REPORTSTRING is
        // the report, which may contain spaces.
        if (index2 > 0 && statusString.substring(index2 + 1).length() > 0) {
            setReporterReport(statusString.substring(index, index2).toUpperCase(), statusString.substring(index2 + 1));
        }
        //} else {
        // return report for this reporter/
        Reporter reporter = InstanceManager.reporterManagerInstance().provideReporter(statusString.substring(index).toUpperCase());
        sendReport(statusString.substring(index), reporter.getCurrentReport());

        //}
    }

    private void sendMessage(String message) throws IOException {
        if (this.output != null) {
            this.output.writeBytes(message);
        } else {
            this.connection.sendMessage(message);
        }
    }
    private final static Logger log = LoggerFactory.getLogger(SimpleReporterServer.class.getName());
}
