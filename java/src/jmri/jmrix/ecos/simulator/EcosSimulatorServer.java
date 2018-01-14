package jmri.jmrix.ecos.simulator;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.NoSuchElementException;
import java.util.Scanner;
import jmri.PowerManager;
import jmri.implementation.QuietShutDownTask;
import jmri.jmris.JmriServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Server that simulates an EcoS device. Note that this simulator assumes the
 * EcoS client will only send valid messages.
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
        // Listen for commands from the client until the connection closes
        try (Scanner inputScanner = new Scanner(new InputStreamReader(inStream, "UTF-8"))) {
            // Listen for commands from the client until the connection closes
            String cmd;
            String reply;

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

                log.debug("Received from client: {}", cmd);
                // Only send non-null replies
                reply = this.generateReply(cmd);
                if (reply != null) {
                    outStream.writeBytes(reply);
                }
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

        String header;
        String body = "";
        String status = "OK";
        String command;
        int id = 0;
        String[] options = new String[0];
        if (msg.indexOf('(') >= 1) {
            command = msg.substring(0, msg.indexOf('('));
        } else {
            command = msg;
        }
        if (msg.indexOf(',') >= 1) {
            id = Integer.parseInt(msg.substring(command.length() + 1, msg.indexOf(',')));
            options = msg.substring(msg.indexOf(',') + 1, msg.length() - 1).trim().split("[,\\s]+");
        } else {
            id = Integer.parseInt(msg.substring(command.length() + 1, msg.length() - 1));
        }
        log.debug("Replying to cmd: \"{}\", id: {}, options: {}", command, id, options);
        switch (command) {
            case "create":
            // fall through with incorrect response to ensure response is sent
            // TODO: respond with quasi-legitimate response
            case "delete":
                // respond OK even if control has not been requested
                // TODO: respond with NERROR_NOCONTROL if control not requested, OK if control held
                header = String.format("<REPLY %s(%d, %s)>", command, id, String.join(", ", options));
                break;
            case "get":
                switch (id) {
                    case 1: // power or EcoS Status
                        header = String.format("<REPLY get(1, %s)>", String.join(", ", options));
                        switch (options[0]) {
                            case "info":
                                body = "1 ECoS\n1ProtocolVersion[0.1]\n1 ApplicationVersion[1.0.1]\n1 HardwareVersion[1.3]";
                                break;
                            case "status":
                                body = String.format("1 Status[%s]", trackPowerState == PowerManager.ON ? "GO" : "STOP");
                                break;
                            default:
                                header = null;
                        }
                        break;
                    default:
                        header = null;
                }
                break;
            case "queryObjects":
            // fall through with incorrect response to ensure response is sent
            // TODO: respond with quasi-legitimate response
            case "release":
            case "request":
                // handle "release" and "request" as if no error occured for now
                // certain combinations of id and options may produce an error
                // in a real system
                // TODO: if requesting control, and allowed, retain control to
                // make other response more legitimate
                header = String.format("<REPLY %s(%d, %s)>", command, id, String.join(", ", options));
                break;
            case "set":
                switch (id) {
                    case 1: // power or EcoS Status
                        // setting ID 1 to anything other "go" is treated as if setting to "stop"
                        trackPowerState = options[0].equals("go") ? PowerManager.ON : PowerManager.OFF;
                        header = String.format("<REPLY set(1, %s)", trackPowerState == PowerManager.ON ? "go" : "stop");
                        break;
                    default:
                        header = null;
                }
                break;
            default:
                log.error("Unexpected command \"{}\" received in \"{}\"", command, msg);
                header = null;
        }
        // Do not respond if header is null
        if (header == null) {
            return null;
        }
        if (body.isEmpty()) {
            log.debug("Reply is:\n{}\n<END 0 ({})>", header, status);
            return String.format("%s%n<END 0 (%s)>", header, status);
        }
        log.debug("Reply is:\n{}\n{}\n<END 0 ({})>", header, body, status);
        return String.format("%s%n%s%n<END 0 (%s)>", header, body, status);
    }

}
