package jmri.jmrix.ecos.simulator;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import jmri.PowerManager;
import jmri.implementation.QuietShutDownTask;
import jmri.jmris.JmriServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Server that simulates an EcoS device.
 *
 * @author Randall Wood Copyright 2018
 */
class EcosSimulatorServer extends JmriServer {

    private final static Logger log = LoggerFactory.getLogger(EcosSimulatorServer.class);
    private int trackPowerState = PowerManager.OFF;

    public EcosSimulatorServer() throws IOException {
        super(0);
        shutDownTask = new QuietShutDownTask("Stop EcoS Simulator Server") { // NOI18N
            @Override
            public boolean execute() {
                try {
                    EcosSimulatorServer.this.stop();
                } catch (Exception ex) {
                    log.warn("ERROR shutting down EcoS Simulator Server: \\{}{}", ex.getMessage());
                    log.debug("Details follow: ", ex);
                }
                return true;
            }
        };
    }

    @Override
    public void start() {
        super.start();
        log.info("Starting EcoS Simulator Server on port {}", this.getPort());
    }

    // Handle communication to a client through inStream and outStream
    @Override
    public void handleClient(DataInputStream inStream, DataOutputStream outStream) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(inStream, StandardCharsets.UTF_8));
        while (true) {
            while (reader.ready()) {
                outStream.writeUTF(this.generateReply(reader.readLine()));
            }
        }
    }

    // Send a stop message to the client if applicable
    @Override
    public void stopClient(DataInputStream inStream, DataOutputStream outStream) throws IOException {
        outStream.writeBytes("");
    }

    /**
     * This is the heart of the simulation. It translates an incoming
     * EcosMessage into an outgoing EcosReply.
     */
    private String generateReply(String msg) {
        log.debug("Generate Reply to message: \"{}\"", msg);

        String reply;
        String command;
        if (msg.indexOf(',') >= 1) {
            command = msg.substring(0, msg.indexOf(','));
        } else {
            command = msg;
        }
        log.error("Replying to \"{}\"", command);
        switch (command) {
            case "request(1": // power monitor
                reply = String.format("<EVENT 1>status[%s]", trackPowerState == PowerManager.ON ? "GO" : "STOP");
                break;
            case "get(1": // power get
                reply = String.format("<REPLY get(1,%s", trackPowerState == PowerManager.ON ? " go)" : " stop)");
                break;
            case "set(1": // power set
                trackPowerState = msg.contains("go") ? PowerManager.ON : PowerManager.OFF;
                reply = String.format("<REPLY set(1,%s", trackPowerState == PowerManager.ON ? " go)" : " stop)");
                break;
            default:
                reply = null; // all others
        }
        log.debug("Reply is: {}", reply);
        return reply;
    }

}
