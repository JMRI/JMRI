package jmri.jmris.simpleserver;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.NoSuchElementException;
import java.util.ResourceBundle;
import java.util.Scanner;
import jmri.InstanceManager;
import jmri.JmriException;
import jmri.jmris.JmriServer;
import jmri.util.node.NodeIdentity;
import jmri.web.server.WebServerPreferences;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is an implementation of a simple server for JMRI. There is currently no
 * handshaking in this server. You may just start sending commands.
 *
 * @author Paul Bender Copyright (C) 2010
 *
 */
public class SimpleServer extends JmriServer {

    static ResourceBundle rb = ResourceBundle.getBundle("jmri.jmris.simpleserver.SimpleServerBundle");

    // Create a new server using the default port
    public SimpleServer() {
        this(Integer.parseInt(rb.getString("SimpleServerPort")));
    }

    public SimpleServer(int port) {
        super(port);
        InstanceManager.setDefault(SimpleServer.class,this);
        log.info("JMRI SimpleServer started on port " + port);
    }

    @Override
    protected void advertise() {
        this.advertise("_jmri-simple._tcp.local.");
    }

    // Handle communication to a client through inStream and outStream
    @Override
    public void handleClient(DataInputStream inStream, DataOutputStream outStream) throws IOException {
        Scanner inputScanner = new Scanner(new InputStreamReader(inStream, "UTF-8"));
        // Listen for commands from the client until the connection closes
        String cmd;

        // interface components
        SimplePowerServer powerServer = new SimplePowerServer(inStream, outStream);
        SimpleTurnoutServer turnoutServer = new SimpleTurnoutServer(inStream, outStream);
        SimpleLightServer lightServer = new SimpleLightServer(inStream, outStream);
        SimpleSensorServer sensorServer = new SimpleSensorServer(inStream, outStream);
        SimpleSignalHeadServer signalHeadServer = new SimpleSignalHeadServer(inStream, outStream);
        SimpleReporterServer reporterServer = new SimpleReporterServer(inStream, outStream);
        SimpleOperationsServer operationsServer = new SimpleOperationsServer(inStream, outStream);

        // Start by sending a welcome message
        outStream.writeBytes("JMRI " + jmri.Version.name() + " \n");
        outStream.writeBytes("RAILROAD " + InstanceManager.getDefault(WebServerPreferences.class).getRailroadName() + " \n");
        outStream.writeBytes("NODE " + NodeIdentity.networkIdentity() + " \n");

        while (true) {
            inputScanner.skip("[\r\n]*");// skip any stray end of line characters.
            // Read the command from the client
            try {
                cmd = inputScanner.nextLine();
            } catch (NoSuchElementException nse) {
                // we get an nse when we are finished with this client
                // so break out of the loop.
                break;
            }

            if (log.isDebugEnabled()) {
                log.debug("Received from client: " + cmd);
            }
            if (cmd.startsWith("POWER")) {
                try {
                    powerServer.parseStatus(cmd);
                    powerServer.sendStatus(InstanceManager.getDefault(jmri.PowerManager.class).getPower());
                } catch (JmriException je) {
                    outStream.writeBytes("not supported\n");
                }
            } else if (cmd.startsWith("TURNOUT")) {
                try {
                    turnoutServer.parseStatus(cmd);
                } catch (JmriException je) {
                    outStream.writeBytes("not supported\n");
                }
            } else if (cmd.startsWith("LIGHT")) {
                try {
                    lightServer.parseStatus(cmd);
                } catch (JmriException je) {
                    outStream.writeBytes("not supported\n");
                }
            } else if (cmd.startsWith("SENSOR")) {
                try {
                    sensorServer.parseStatus(cmd);
                } catch (JmriException je) {
                    outStream.writeBytes("not supported\n");
                }
            } else if (cmd.startsWith("SIGNALHEAD")) {
                try {
                    signalHeadServer.parseStatus(cmd);
                } catch (JmriException je) {
                    outStream.writeBytes("not supported\n");
                }
            } else if (cmd.startsWith("REPORTER")) {
                try {
                    reporterServer.parseStatus(cmd);
                } catch (JmriException je) {
                    outStream.writeBytes("not supported\n");
                }
            } else if (cmd.startsWith(SimpleOperationsServer.OPERATIONS)) {
                try {
                    operationsServer.parseStatus(cmd);
                } catch (JmriException je) {
                    outStream.writeBytes("not supported\n");
                }
            } else {
                outStream.writeBytes("Unknown Command " + cmd + "\n");
            }
        }
        inputScanner.close();
    }
    private final static Logger log = LoggerFactory.getLogger(SimpleServer.class);
}
