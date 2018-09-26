package jmri.jmrix.loconet.loconetovertcp;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.util.LinkedList;
import java.util.StringTokenizer;
import jmri.jmrix.loconet.LnTrafficController;
import jmri.jmrix.loconet.LocoNetListener;
import jmri.jmrix.loconet.LocoNetMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementation of the LocoNetOverTcp LbServer Server Protocol.
 *
 * @author Alex Shepherd Copyright (C) 2006
 */
public final class ClientRxHandler extends Thread implements LocoNetListener {

    Socket clientSocket;
    BufferedReader inStream;
    OutputStream outStream;
    final LinkedList<LocoNetMessage> msgQueue = new LinkedList<>();
    volatile Thread txThread;
    String inString;
    String remoteAddress;
    LocoNetMessage lastSentMessage;
    LnTrafficController tc;

    public ClientRxHandler(String newRemoteAddress, Socket newSocket, LnTrafficController _tc) {
        tc = _tc;
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

        try {
            inStream = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            outStream = clientSocket.getOutputStream();

            tc.addLocoNetListener(~0, this);

            txThread = new Thread(new ClientTxHandler(this));
            txThread.setDaemon(true);
            txThread.setPriority(Thread.MAX_PRIORITY);
            txThread.setName("ClientTxHandler: " + remoteAddress);
            txThread.start();

            while (!isInterrupted()) {
                inString = inStream.readLine();
                if (inString == null) {
                    log.debug("ClientRxHandler: Remote Connection Closed");
                    interrupt();
                } else {
                    log.debug("ClientRxHandler: Received: {}", inString);

                    StringTokenizer st = new StringTokenizer(inString);
                    if (st.nextToken().equals("SEND")) {
                        LocoNetMessage msg = null;
                        int opCode = Integer.parseInt(st.nextToken(), 16);
                        int byte2 = Integer.parseInt(st.nextToken(), 16);

                        // Decide length
                        switch ((opCode & 0x60) >> 5) {
                            case 0: // 2 byte message

                                msg = new LocoNetMessage(2);
                                break;

                            case 1: // 4 byte message

                                msg = new LocoNetMessage(4);
                                break;

                            case 2: // 6 byte message

                                msg = new LocoNetMessage(6);
                                break;

                            case 3: // N byte message

                                if (byte2 < 2) {
                                    log.error("ClientRxHandler: LocoNet message length invalid: "
                                            + byte2 + " opcode: "
                                            + Integer.toHexString(opCode));
                                }
                                msg = new LocoNetMessage(byte2);
                                break;
                            default:
                                log.warn("Unhandled msg length: {}", (opCode & 0x60) >> 5);
                                break;
                        }
                        if (msg == null) {
                            log.error("msg is null!");
                            return;
                        }
                        // message exists, now fill it
                        msg.setOpCode(opCode);
                        msg.setElement(1, byte2);
                        int len = msg.getNumDataElements();
                        //log.debug("len: "+len);

                        for (int i = 2; i < len; i++) {
                            int b = Integer.parseInt(st.nextToken(), 16);
                            msg.setElement(i, b);
                        }

                        tc.sendLocoNetMessage(msg);
                        // Keep the message we just sent so we can ACK it when we hear
                        // the echo from the LocoBuffer
                        lastSentMessage = msg;
                    }
                }
            }
        } catch (IOException ex) {
            log.debug("ClientRxHandler: IO Exception: ", ex);
        }
        tc.removeLocoNetListener(~0, this);
        if (txThread != null) txThread.interrupt();

        txThread = null;
        inStream = null;
        outStream = null;
        msgQueue.clear();

        try {
            clientSocket.close();
        } catch (IOException ex1) {
        }

        LnTcpServer.getDefault().removeClient(this);
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

        LocoNetMessage msg;
        StringBuffer outBuf;
        Thread parentThread;

        ClientTxHandler(Thread creator) {
            parentThread = creator;
        }

        @Override
        public void run() {

            try {
                outBuf = new StringBuffer("VERSION JMRI Server ");
                outBuf.append(jmri.Version.name()).append("\r\n");
                outStream.write(outBuf.toString().getBytes());

                while (!isInterrupted()) {
                    msg = null;

                    synchronized (msgQueue) {
                        if (msgQueue.isEmpty()) {
                            msgQueue.wait();
                        }

                        if (!msgQueue.isEmpty()) {
                            msg = msgQueue.removeFirst();
                        }
                    }

                    if (msg != null) {
                        outBuf.setLength(0);
                        outBuf.append("RECEIVE ");
                        outBuf.append(msg.toString());
                        log.debug("ClientTxHandler: Send: {}", outBuf.toString());
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

    @Override
    public void message(LocoNetMessage msg) {
        synchronized (msgQueue) {
            msgQueue.add(msg);
            msgQueue.notify();
        }
    }

    /**
     * Kill this thread, usually for testing purposes
     */
    void dispose() {
        try {
            this.interrupt();
            this.join();
        } catch (InterruptedException ex) {
            log.warn("dispose() interrupted");
        }
    }

    private final static Logger log = LoggerFactory.getLogger(ClientRxHandler.class);

}
