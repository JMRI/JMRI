// JsonServer.java
package jmri.jmris.json;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.NoSuchElementException;
import java.util.Scanner;
import jmri.implementation.QuietShutDownTask;
import jmri.jmris.JmriConnection;
import jmri.jmris.JmriServer;
import org.apache.log4j.Logger;

/**
 * This is an implementation of a simple server for JMRI. There is currently no
 * handshaking in this server. You may just start sending commands.
 *
 * @author Paul Bender Copyright (C) 2010
 * @version $Revision: 21126 $
 *
 */
public class JsonServer extends JmriServer {

    static Logger log = Logger.getLogger(JsonServer.class);

    // Create a new server using the default port
    public JsonServer() {
        this(JsonServerManager.getJsonServerPreferences().getPort(), JsonServerManager.getJsonServerPreferences().getHeartbeatInterval());
    }

    public JsonServer(int port, int timeout) {
        super(port, timeout);
        shutDownTask = new QuietShutDownTask("Stop JSON Server") { // NOI18N
            @Override
            public boolean execute() {
                try {
                    JsonServerManager.getJsonServer().stop();
                } catch (Exception ex) {
                    log.warn("ERROR shutting down JSON Server: " + ex.getLocalizedMessage());
                    if (log.isDebugEnabled()) {
                        log.debug("Details follow: ", ex);
                    }
                }
                return true;
            }
        };
    }

    @Override
    public void start() {
        log.info("Starting JSON Server on port " + this.portNo);
        super.start();
    }

    @Override
    public void stop() {
        log.info("Stopping JSON Server.");
        super.stop();
    }

    @Override
    protected void advertise() {
        this.advertise("_jmri-json._tcp.local."); // NOI18N
    }

    // Handle communication to a client through inStream and outStream
    @Override
    public void handleClient(DataInputStream inStream, DataOutputStream outStream) throws IOException {
        Scanner scanner = new Scanner(new InputStreamReader(inStream));
        JsonClientHandler handler = new JsonClientHandler(new JmriConnection(outStream));

        // Start by sending a welcome message
        handler.sendHello(this.timeout);

        while (true) {
            scanner.skip("[\r\n]*"); // skip any stray end of line characters. // NOI18N
            // Read the command from the client
            try {
                handler.onMessage(scanner.nextLine());
            } catch (IOException e) {
                scanner.close();
                handler.onClose();
                throw e;
            } catch (NoSuchElementException nse) {
                // we get an NSE when we are finished with this client
                // so break out of the loop
                break;
            }
        }
        scanner.close();
        handler.onClose();
    }

    @Override
    public void stopClient(DataInputStream inStream, DataOutputStream outStream) throws IOException {
        // write a raw JSON string so we don't have to instanciate a handler for this
        // or link the Server against the JSON token list
        outStream.writeBytes("{'type':'goodbye'}"); // NOI18N
    }
}