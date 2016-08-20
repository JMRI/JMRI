package jmri.jmris.json;

import static jmri.server.json.JSON.GOODBYE;
import static jmri.server.json.JSON.JSON;
import static jmri.server.json.JSON.JSON_PROTOCOL_VERSION;
import static jmri.server.json.JSON.TYPE;
import static jmri.server.json.JSON.ZEROCONF_SERVICE_TYPE;

import com.fasterxml.jackson.core.JsonParser.Feature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.NoSuchElementException;
import java.util.concurrent.TimeUnit;
import jmri.InstanceManager;
import jmri.implementation.QuietShutDownTask;
import jmri.jmris.JmriServer;
import jmri.server.json.JsonClientHandler;
import jmri.server.json.JsonConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is an implementation of a simple server for JMRI. There is currently no
 * handshaking in this server. You may just start sending commands.
 *
 * @author Paul Bender Copyright (C) 2010
 *
 */
public class JsonServer extends JmriServer {

    private static final Logger log = LoggerFactory.getLogger(JsonServer.class);
    private ObjectMapper mapper;

    public static JsonServer getDefault() {
        if (InstanceManager.getOptionalDefault(JsonServer.class) == null) {
            InstanceManager.store(new JsonServer(), JsonServer.class);
        }
        return InstanceManager.getDefault(JsonServer.class);
    }

    // Create a new server using the default port
    public JsonServer() {
        this(JsonServerPreferences.getDefault().getPort(), JsonServerPreferences.getDefault().getHeartbeatInterval());
    }

    public JsonServer(int port, int timeout) {
        super(port, timeout);
        this.mapper = new ObjectMapper().configure(Feature.AUTO_CLOSE_SOURCE, false);
        shutDownTask = new QuietShutDownTask("Stop JSON Server") { // NOI18N
            @Override
            public boolean execute() {
                try {
                    JsonServer.this.stop();
                } catch (Exception ex) {
                    log.warn("ERROR shutting down JSON Server: {}" + ex.getMessage());
                    log.debug("Details follow: ", ex);
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
        this.advertise(ZEROCONF_SERVICE_TYPE, properties);
    }

    // Handle communication to a client through inStream and outStream
    @Override
    public void handleClient(DataInputStream inStream, DataOutputStream outStream) throws IOException {
        ObjectReader reader = this.mapper.reader();
        JsonClientHandler handler = new JsonClientHandler(new JsonConnection(outStream));

        // Start by sending a welcome message
        handler.onMessage(JsonClientHandler.HELLO_MSG);

        while (true) {
            try {
                handler.onMessage(reader.readTree(inStream));
                // Read the command from the client
            } catch (IOException e) {
                // attempt to close the connection and throw the exception
                handler.dispose();
                throw e;
            } catch (NoSuchElementException nse) {
                // we get an NSE when we are finished with this client
                // so break out of the loop
                break;
            }
        }
        handler.dispose();
    }

    @Override
    public void stopClient(DataInputStream inStream, DataOutputStream outStream) throws IOException {
        outStream.writeBytes(this.mapper.writeValueAsString(this.mapper.createObjectNode().put(TYPE, GOODBYE)));
        try {
            // without this delay, the output stream could be closed before the
            // preparing to disconnect message is sent
            TimeUnit.MILLISECONDS.sleep(100);
        } catch (InterruptedException ex) {
            // log for debugging only, since we are most likely shutting down the
            // server or the program entirely at this point, so it doesn't matter
            log.debug("Wait to send clean shutdown message interrupted.");
        }
    }
}
