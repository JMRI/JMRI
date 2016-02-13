// ClientRxHandler.java
package jmri.jmrix.dccpp.dccppovertcp;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.util.LinkedList;
import java.util.StringTokenizer;
import jmri.InstanceManager;
import jmri.jmrix.dccpp.DCCppSystemConnectionMemo;
import jmri.jmrix.dccpp.DCCppTrafficController;
import jmri.jmrix.dccpp.DCCppListener;
import jmri.jmrix.dccpp.DCCppMessage;
import jmri.jmrix.dccpp.DCCppReply;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementation of the DCCppOverTcp LbServer Server Protocol
 *
 * @author Alex Shepherd Copyright (C) 2006
 * @author Mark Underwood Copyright (C) 2015
 * @version	$Revision$
 */
public final class ClientRxHandler extends Thread implements DCCppListener {

    Socket clientSocket;
    BufferedReader inStream;
    OutputStream outStream;
    LinkedList<DCCppReply> replyQueue;
    Thread txThread;
    String inString;
    String remoteAddress;
    DCCppMessage lastSentMessage;
    final String sendPrefix = "SEND";

    public ClientRxHandler(String newRemoteAddress, Socket newSocket) {
        clientSocket = newSocket;
        setDaemon(true);
        setPriority(Thread.MAX_PRIORITY);
        remoteAddress = newRemoteAddress;
        setName("ClientRxHandler:" + remoteAddress);
        lastSentMessage = null;
        start();
    }

    @SuppressWarnings("null")
    public void run() {

        try {
            inStream = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            outStream = clientSocket.getOutputStream();

	    DCCppSystemConnectionMemo memo = InstanceManager.getDefault(DCCppSystemConnectionMemo.class);
	    memo.getDCCppTrafficController().addDCCppListener(~0, this);
            //DCCppTrafficController.instance().addDCCppListener(~0, this);

            txThread = new Thread(new ClientTxHandler(this));
            txThread.setDaemon(true);
            txThread.setPriority(Thread.MAX_PRIORITY);
            txThread.setName("ClientTxHandler:" + remoteAddress);
            txThread.start();

            while (!isInterrupted()) {
                inString = inStream.readLine();
                if (inString == null) {
                    log.debug("ClientRxHandler: Remote Connection Closed");
                    interrupt();
                } else {
                    log.debug("ClientRxHandler: Received: " + inString);

		    if (!inString.startsWith(sendPrefix)) {
			log.debug("Invalid packet format: {}", inString);
			continue;
		    }
		    final int trim = sendPrefix.length();
		    inString = inString.substring(trim);
		    //  Note: the substring call below also strips off the "< >"
		    DCCppMessage msg = DCCppMessage.parseDCCppMessage(inString.substring(inString.indexOf("<")+1,
									   inString.lastIndexOf(">")));
		    if (!msg.isValidMessageFormat()) {
			log.warn("Invalid Message Format {}", msg.toString());
			continue;
		    }

		    // TODO: Bad practice to use instance().
		    DCCppTrafficController.instance().sendDCCppMessage(msg, null);
		    // Keep the message we just sent so we can ACK it when we hear
		    // the echo from the LocoBuffer
		    lastSentMessage = msg;
                }
            }
        } catch (IOException ex) {
            log.debug("ClientRxHandler: IO Exception: ", ex);
        }
	// TODO: Bad practice to use instance();
        DCCppTrafficController.instance().removeDCCppListener(~0, this);
        txThread.interrupt();

        txThread = null;
        inStream = null;
        outStream = null;
        replyQueue.clear();
        replyQueue = null;

        try {
            clientSocket.close();
        } catch (IOException ex1) {
        }

        Server.getInstance().removeClient(this);
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
        StringBuffer outBuf;
        Thread parentThread;

        ClientTxHandler(Thread creator) {
            parentThread = creator;
        }

        public void run() {

            try {
                outBuf = new StringBuffer("VERSION JMRI Server ");
                outBuf.append(jmri.Version.name()).append("\r\n");
                outStream.write(outBuf.toString().getBytes());
		
		replyQueue = new LinkedList<DCCppReply>(); // Should this be in the other thread?

                while (!isInterrupted()) {
                    msg = null;

                    synchronized (replyQueue) {
                        if (replyQueue.isEmpty()) {
                            replyQueue.wait();
                        }

                        if (!replyQueue.isEmpty()) {
                            msg = replyQueue.removeFirst();
			    log.debug("Prepping to send message: {}", msg.toString());
                        }
                    }

                    if (msg != null) {
                        outBuf.setLength(0);
                        outBuf.append("RECEIVE ");
			outBuf.append("<");
                        outBuf.append(msg.toString());
			outBuf.append(">");
                        log.debug("ClientTxHandler: Send: " + outBuf.toString());
                        outBuf.append("\r\n");
                        // See if we are waiting for an echo of a sent message
                        // and if it is append the Ack to the client
                        if ((lastSentMessage != null) && lastSentMessage.equals(msg)) {
                            lastSentMessage = null;
                            outBuf.append("SENT OK\r\n");
                        }
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

    public void message(DCCppMessage msg) {
    }

    public void message(DCCppReply msg) {
        synchronized (replyQueue) {
            replyQueue.add(msg);
            replyQueue.notify();
	    log.debug("Message added to queue: {}", msg.toString());
        }
    }

    public void notifyTimeout(DCCppMessage m) {
    }

    private final static Logger log = LoggerFactory.getLogger(ClientRxHandler.class.getName());
}
