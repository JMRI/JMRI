// JsonServer.java
package jmri.jmris.json;

import com.fasterxml.jackson.core.JsonParser.Feature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.NoSuchElementException;
import jmri.implementation.QuietShutDownTask;
import jmri.jmris.JmriConnection;
import jmri.jmris.JmriServer;
import static jmri.jmris.json.JSON.GOODBYE;
import static jmri.jmris.json.JSON.JSON;
import static jmri.jmris.json.JSON.JSON_PROTOCOL_VERSION;
import static jmri.jmris.json.JSON.TYPE;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is an implementation of a simple server for JMRI. There is currently no
 * handshaking in this server. You may just start sending commands.
 *
 * @author Paul Bender Copyright (C) 2010
 * @version $Revision: 21126 $
 *
 */
public class JsonServer extends JmriServer {

    private static final Logger log = LoggerFactory.getLogger(JsonServer.class);
    private ObjectMapper mapper;

    // Create a new server using the default port
    public JsonServer() {
        this(JsonServerManager.getJsonServerPreferences().getPort(), JsonServerManager.getJsonServerPreferences().getHeartbeatInterval());
    }

    public JsonServer(int port, int timeout) {
        super(port, timeout);
        this.mapper = new ObjectMapper().configure(Feature.AUTO_CLOSE_SOURCE, false);
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
        HashMap<String, String> properties = new HashMap<String, String>();
        properties.put(JSON, JSON_PROTOCOL_VERSION);
        this.advertise("_jmri-json._tcp.local.", properties); // NOI18N
    }

    // Handle communication to a client through inStream and outStream
    @Override
    public void handleClient(DataInputStream inStream, DataOutputStream outStream) throws IOException {
        ObjectReader reader = this.mapper.reader();
        JsonClientHandler handler = new JsonClientHandler(new JmriConnection(outStream), this.mapper);

        // Start by sending a welcome message
        handler.sendHello(this.timeout);

        while (true) {
            try {
                handler.onMessage(reader.readTree(inStream));
                // Read the command from the client
            } catch (IOException e) {
                // attempt to close the connection and throw the exception
                handler.onClose();
                throw e;
            } catch (NoSuchElementException nse) {
                // we get an NSE when we are finished with this client
                // so break out of the loop
                break;
            }
        }
        handler.onClose();
    }

    @Override
    public void stopClient(DataInputStream inStream, DataOutputStream outStream) throws IOException {
        outStream.writeBytes(this.mapper.writeValueAsString(this.mapper.createObjectNode().put(TYPE, GOODBYE)));
    }
}
