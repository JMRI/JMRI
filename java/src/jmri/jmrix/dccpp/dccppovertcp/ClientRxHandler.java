package jmri.jmrix.dccpp.dccppovertcp;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.util.LinkedList;
import jmri.InstanceManager;
import jmri.jmrix.dccpp.DCCppListener;
import jmri.jmrix.dccpp.DCCppMessage;
import jmri.jmrix.dccpp.DCCppReply;
import jmri.jmrix.dccpp.DCCppSystemConnectionMemo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementation of the DCCppOverTcp LbServer Server Protocol.
 *
 * @author Alex Shepherd Copyright (C) 2006
 * @author Mark Underwood Copyright (C) 2015
 */
public final class ClientRxHandler extends Thread implements DCCppListener {

    Socket clientSocket;
    BufferedReader inStream;
    OutputStream outStream;
    LinkedList<DCCppReply> replyQueue = new LinkedList<>(); // Init before Rx and Tx

    Thread txThread;
    String inString;
    String remoteAddress;
    DCCppMessage lastSentMessage;
    private static final String oldSendPrefix = "SEND"; // lack of space is correct for legacy code
    private static final String oldReceivePrefix = "RECEIVE "; // presence of space is correct for legacy code
    private static final String sendPrefix = "<";
    private static final String oldServerVersionString = "VERSION JMRI Server "; // CAREFUL: Changing this could break backward compatibility
    private static final String newServerVersionString = "VERSION DCC++ Server ";
    boolean useOldPrefix = false;

    public ClientRxHandler(String newRemoteAddress, Socket newSocket) {
        clientSocket = newSocket;
        setDaemon(true);
        setPriority(Thread.MAX_PRIORITY);
        remoteAddress = newRemoteAddress;
        setName("ClientRxHandler:" + remoteAddress);
        lastSentMessage = null;
        start();
    }

    @Override
    public void run() {
    
        DCCppSystemConnectionMemo memo = InstanceManager.getDefault(DCCppSystemConnectionMemo.class);
        
        try {
            txThread = new Thread(new ClientTxHandler(this));
            txThread.setDaemon(true);
            txThread.setPriority(Thread.MAX_PRIORITY);
            txThread.setName("ClientTxHandler:" + remoteAddress);

            inStream = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            outStream = clientSocket.getOutputStream();

            memo.getDCCppTrafficController().addDCCppListener(~0, this);

            txThread.start();

            while (!isInterrupted()) {
                inString = inStream.readLine();
                if (inString == null) {
                    log.debug("ClientRxHandler: Remote Connection Closed");
                    interrupt();
                } else {
                    log.debug("ClientRxHandler: Received: {}", inString);

                    // Check for the old server version string.  If present,
                    // append the old-style prefixes to transmissions.
                    // Not sure this ever happens. Only the client sends
                    // the version string.
                    if (inString.startsWith(oldServerVersionString)) {
                        useOldPrefix = true;
                    }
                    // Legacy support: If the old prefix is there, delete it.
                    // Also, set the flag so we will start sending old-style
                    // prefixes.
                    if (inString.startsWith(oldSendPrefix)) {
                        useOldPrefix = true;
                        final int trim = oldSendPrefix.length();
                        inString = inString.substring(trim);
                        log.debug("Adapted String: {}", inString);
                    }
                    // Check for the opening bracket
                    if (!inString.startsWith(sendPrefix)) {
                        log.debug("Invalid packet format: {}", inString);
                        continue;
                    }

                    // BUG FIX: Incoming DCCppOverTCP messages are already formatted for DCC++ and don't
                    // need to be parsed. Indeed, trying to parse them will screw them up.
                    // So instead, we de-@Deprecated the string constructor so that we can
                    // directly create a DCCppMessage from the incoming string without translation/parsing.
                    DCCppMessage msg = new DCCppMessage(inString.substring(inString.indexOf('<') + 1,
                            inString.lastIndexOf('>')));
                    if (!msg.isValidMessageFormat()) {
                        log.warn("Invalid Message Format {}", msg);
                        continue;
                    }

                    memo.getDCCppTrafficController().sendDCCppMessage(msg, null);
                    // Keep the message we just sent so we can ACK it when we hear
                    // the echo from the LocoBuffer
                    lastSentMessage = msg;
                }
            }
        } catch (IOException ex) {
            log.debug("ClientRxHandler: IO Exception: ", ex);
        }

        memo.getDCCppTrafficController().removeDCCppListener(~0, this);
        txThread.interrupt();

        txThread = null;
        inStream = null;
        outStream = null;
        replyQueue.clear();
        replyQueue = null;

        try {
            clientSocket.close();
        } catch (IOException ex1) {
            log.trace("Exception while closing client socket",ex1);
        }

        InstanceManager.getDefault(Server.class).removeClient(this);
        log.info("ClientRxHandler: Exiting");
    }

    public void close() {
        try {
            clientSocket.close();
        } catch (IOException ex1) {
            log.error("close, which closing clientSocket", ex1);
        }
    }

    class ClientTxHandler implements Runnable {

        DCCppReply msg;
        StringBuilder outBuf;
        Thread parentThread;

        ClientTxHandler(Thread creator) {
            parentThread = creator;
        }

        @Override
        public void run() {

            try {
                outBuf = new StringBuilder(newServerVersionString);
                outBuf.append(jmri.Version.name()).append("\r\n");
                outStream.write(outBuf.toString().getBytes());

                while (!isInterrupted()) {
                    msg = null;

                    synchronized (replyQueue) {
                        if (replyQueue.isEmpty()) {
                            replyQueue.wait();
                        }

                        if (!replyQueue.isEmpty()) {
                            msg = replyQueue.removeFirst();
                            log.debug("Prepping to send message: {}", msg);
                        }
                    }

                    if (msg != null) {
                        outBuf.setLength(0);
                        if (useOldPrefix) {
                            outBuf.append(oldReceivePrefix);
                        }
                        outBuf.append("<");
                        outBuf.append(msg.toString());
                        outBuf.append(">");
                        log.debug("ClientTxHandler: Send: {}", outBuf);
                        outBuf.append("\r\n");
                        outStream.write(outBuf.toString().getBytes());
                        outStream.flush();
                    }
                }
            } catch (IOException ex) {
                log.error("ClientTxHandler: IO Exception");
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt(); // retain if needed later
                log.debug("ClientTxHandler: Interrupted Exception");
            }
            // Interrupt the Parent to let it know we are exiting for some reason
            parentThread.interrupt();

            parentThread = null;
            msg = null;
            outBuf = null;
            log.info("ClientTxHandler: Exiting");
        }
    }

    @Override
    public void message(DCCppMessage msg) {
        // no need to handle outgonig messages
    }

    @Override
    public void message(DCCppReply msg) {
        synchronized (replyQueue) {
            replyQueue.add(msg);
            replyQueue.notify();
            log.debug("Message added to queue: {}", msg);
        }
    }

    @Override
    public void notifyTimeout(DCCppMessage m) {
        // ToDo : handle timeouts
    }

    private static final Logger log = LoggerFactory.getLogger(ClientRxHandler.class);

}
