// LIUSBServerAdapter.java
package jmri.jmrix.lenz.liusbserver;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import jmri.jmrix.lenz.LenzCommandStation;
import jmri.jmrix.lenz.XNetInitializationManager;
import jmri.jmrix.lenz.XNetNetworkPortController;
import jmri.jmrix.lenz.XNetReply;
import jmri.jmrix.lenz.XNetSystemConnectionMemo;
import jmri.jmrix.lenz.XNetTrafficController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provide access to XPressNet via a the Lenz LIUSB Server. NOTES: The LIUSB
 * server binds only to localhost (127.0.0.1) on TCP ports 5550 and 5551. Port
 * 5550 is used for general communication. Port 5551 is used for broadcast
 * messages only. The LIUSB Server disconnects both ports if there is 60 seconds
 * of inactivity on the port. The LIUSB Server disconnects port 5550 if another
 * device puts the system into service mode.
 *
 * @author	Paul Bender (C) 2009-2010
 * @version	$Revision$
 */
public class LIUSBServerAdapter extends XNetNetworkPortController {

    static final int COMMUNICATION_TCP_PORT = 5550;
    static final int BROADCAST_TCP_PORT = 5551;
    static final String DEFAULT_IP_ADDRESS = "localhost";

    private java.util.TimerTask keepAliveTimer; // Timer used to periodically
    // send a message to both
    // ports to keep the ports 
    // open
    private static final int keepAliveTimeoutValue = 30000; // Interval 
    // to send a message
    // Must be < 60s.

    private BroadCastPortAdapter bcastAdapter = null;
    private CommunicationPortAdapter commAdapter = null;

    private DataOutputStream pout = null; // for output to other classes
    private DataInputStream pin = null; // for input from other classes
    // internal ends of the pipe
    private DataOutputStream outpipe = null;  // feed pin
    private Thread commThread;
    private Thread bcastThread;

    public LIUSBServerAdapter() {
        super();
        option1Name = "BroadcastPort";
        options.put(option1Name, new Option("Broadcast Port", new String[]{String.valueOf(LIUSBServerAdapter.BROADCAST_TCP_PORT), ""}));
        this.manufacturerName = jmri.jmrix.DCCManufacturerList.LENZ;
    }

    synchronized public String openPort(String portName, String appName) {
        if (log.isDebugEnabled()) {
            log.debug("openPort called");
        }
        // open the port in XPressNet mode
        try {
            bcastAdapter = new BroadCastPortAdapter(this);
            commAdapter = new CommunicationPortAdapter(this);
            bcastAdapter.connect();
            commAdapter.connect();
            pout = commAdapter.getOutputStream();
            PipedOutputStream tempPipeO = new PipedOutputStream();
            outpipe = new DataOutputStream(tempPipeO);
            pin = new DataInputStream(new PipedInputStream(tempPipeO));
        } catch (java.io.IOException e) {
            log.error("init (pipe): Exception: " + e.toString());
        } catch (Exception ex) {
            log.error("init (connect): Exception: " + ex.toString());
        }
        keepAliveTimer();
        return null; // normal operation
    }

    /**
     * Can the port accept additional characters? return true if the port is
     * opened.
     */
    public boolean okToSend() {
        return status();
    }

    // base class methods for the XNetNetworkPortController interface
    public DataInputStream getInputStream() {
        if (pin == null) {
            log.error("getInputStream called before load(), stream not available");
        }
        return pin;
    }

    public DataOutputStream getOutputStream() {
        if (pout == null) {
            log.error("getOutputStream called before load(), stream not available");
        }
        return pout;
    }

    public boolean status() {
        return (pout != null && pin != null);
    }

    /**
     * set up all of the other objects to operate with a LIUSB Server interface
     */
    public void configure() {
        if (log.isDebugEnabled()) {
            log.debug("configure called");
        }
        // connect to a packetizing traffic controller
        XNetTrafficController packets = (new LIUSBServerXNetPacketizer(new LenzCommandStation()));
        packets.connectPort(this);

        // start operation
        // packets.startThreads();
        this.getSystemConnectionMemo().setXNetTrafficController(packets);

        // Start the threads that handle the network communication.
        startCommThread();
        startBCastThread();

        new XNetInitializationManager(this.getSystemConnectionMemo());

        jmri.jmrix.lenz.ActiveFlag.setActive();

    }

    /**
     * Start the Communication port thread.
     */
    private void startCommThread() {
        commThread = new Thread(new Runnable() {
            public void run() { // start a new thread
                // this thread has one task.  It repeatedly reads from the two 
                // incomming network connections and writes the resulting 
                // messages from the network ports and writes any data 
                // received to the output pipe. 
                if (log.isDebugEnabled()) {
                    log.debug("Communication Adapter Thread Started");
                }
                XNetReply r;
                BufferedReader bufferedin
                        = new BufferedReader(
                                new InputStreamReader(commAdapter.getInputStream(),
                                        java.nio.charset.Charset.forName("UTF-8")));
                for (;;) {
                    try {
                        synchronized (commAdapter) {
                            r = loadChars(bufferedin);
                        }
                    } catch (java.io.IOException e) {
                        // continue;
                        // start the process of trying to recover from
                        // a failed connection.
                        commAdapter.recover();
                        break; // then exit the for loop.
                    }
                    if (log.isDebugEnabled()) {
                        log.debug("Network Adapter Received Reply: "
                                + r.toString());
                    }
                    writeReply(r);
                }
            }
        });
        commThread.start();
    }

    /**
     * Start the Broadcast Port thread.
     */
    private void startBCastThread() {
        bcastThread = new Thread(new Runnable() {
            public void run() { // start a new thread
                // this thread has one task.  It repeatedly reads from the two 
                // incomming network connections and writes the resulting 
                // messages from the network ports and writes any data received 
                // to the output pipe. 
                if (log.isDebugEnabled()) {
                    log.debug("Broadcast Adapter Thread Started");
                }
                XNetReply r;
                BufferedReader bufferedin
                        = new BufferedReader(
                                new InputStreamReader(bcastAdapter.getInputStream(),
                                        java.nio.charset.Charset.forName("UTF-8")));
                for (;;) {
                    try {
                        synchronized (bcastAdapter) {
                            r = loadChars(bufferedin);
                        }
                    } catch (java.io.IOException e) {
                        //continue;
                        // start the process of trying to recover from
                        // a failed connection.
                        bcastAdapter.recover();
                        break; // then exit the for loop.
                    }
                    if (log.isDebugEnabled()) {
                        log.debug("Network Adapter Received Reply: "
                                + r.toString());
                    }
                    r.setUnsolicited(); // Anything coming through the 
                    // broadcast port is an 
                    // unsolicited message.
                    writeReply(r);
                }
            }
        });
        bcastThread.start();
    }

    /**
     * Local method to do specific configuration
     */
    @Deprecated
    static public LIUSBServerAdapter instance() {
        if (mInstance == null) {
            mInstance = new LIUSBServerAdapter();
        }
        return mInstance;
    }
    volatile static LIUSBServerAdapter mInstance = null;

    private synchronized void writeReply(XNetReply r) {
        if (log.isDebugEnabled()) {
            log.debug("Write reply to outpipe: " + r.toString());
        }
        int i;
        int len = (r.getElement(0) & 0x0f) + 2;  // opCode+Nbytes+ECC
        for (i = 0; i < len; i++) {
            try {
                outpipe.writeByte((byte) r.getElement(i));
            } catch (java.io.IOException ex) {
            }
        }
    }

    /**
     * Get characters from the input source, and file a message.
     * <P>
     * Returns only when the message is complete.
     * <P>
     * Only used in the Receive thread.
     *
     * @param msg     message to fill
     * @param istream character source.
     * @throws IOException when presented by the input source.
     */
    private XNetReply loadChars(java.io.BufferedReader istream) throws java.io.IOException {
        // The LIUSBServer sends us data as strings of hex values.
        // These hex values are followed by a <cr><lf>
        String s = "";
        s = istream.readLine();
        if (log.isDebugEnabled()) {
            log.debug("Received from port: " + s);
        }
        if (s == null) {
            return null;
        } else {
            return new XNetReply(s);
        }
    }

    /**
     * This is called when a connection is initially lost. For this connection,
     * it calls the default recovery method for both of the internal adapters.
     */
    @Override
    synchronized public void recover() {
        bcastAdapter.recover();
        commAdapter.recover();
    }

    /**
     * Customizable method to deal with resetting a system connection after a
     * successful recovery of a connection.
     */
    @Override
    protected void resetupConnection() {
        this.getSystemConnectionMemo().getXNetTrafficController().connectPort(this);
    }

    //Internal class for broadcast port connection
    private static class BroadCastPortAdapter extends jmri.jmrix.AbstractNetworkPortController {

        private LIUSBServerAdapter parent;

        public BroadCastPortAdapter(LIUSBServerAdapter p) {
            super(p.getSystemConnectionMemo());
            parent = p;
            allowConnectionRecovery = true;
            setHostName(DEFAULT_IP_ADDRESS);
            setPort(BROADCAST_TCP_PORT);
        }

        public void configure() {
        }

        @Override
        public String getManufacturer() {
            return this.parent.getManufacturer();
        }

        @Override
        protected void resetupConnection() {
            parent.startBCastThread();
        }

        @Override
        public XNetSystemConnectionMemo getSystemConnectionMemo() {
            return this.parent.getSystemConnectionMemo();
        }

        @Override
        @SuppressWarnings("OverridingMethodsMustInvokeSuper")
        public void dispose() {
            // override to prevent super class from disposing of the
            // SystemConnectionMemo since this object does not own it
        }
    }

    // Internal class for communication port connection
    private static class CommunicationPortAdapter extends jmri.jmrix.AbstractNetworkPortController {

        private LIUSBServerAdapter parent;

        public CommunicationPortAdapter(LIUSBServerAdapter p) {
            super(p.getSystemConnectionMemo());
            parent = p;
            allowConnectionRecovery = true;
            setHostName(DEFAULT_IP_ADDRESS);
            setPort(COMMUNICATION_TCP_PORT);
        }

        public void configure() {
        }

        @Override
        public String getManufacturer() {
            return this.parent.getManufacturer();
        }

        @Override
        protected void resetupConnection() {
            parent.startCommThread();
        }

        @Override
        public XNetSystemConnectionMemo getSystemConnectionMemo() {
            return this.parent.getSystemConnectionMemo();
        }

        @Override
        @SuppressWarnings("OverridingMethodsMustInvokeSuper")
        public void dispose() {
            // override to prevent super class from disposing of the
            // SystemConnectionMemo since this object does not own it
        }

    }

    /*
     * Set up the keepAliveTimer, and start it.
     */
    private void keepAliveTimer() {
        if (keepAliveTimer == null) {
            keepAliveTimer = new java.util.TimerTask(){
                public void run () {
                    /* If the timer times out, just send a character to the
                     *  ports.
                     */
                    try {
                        bcastAdapter.getOutputStream().write('z');
                        commAdapter.getOutputStream().write('z');
                    } catch (java.io.IOException ex) {
                        //We need to do something here, because the
                        //communication port drops when another device
                        //puts the command station into service mode.
                        ex.printStackTrace();
                    }
                }
            };
        }
        else {
           keepAliveTimer.cancel();
        }
        new java.util.Timer().schedule(keepAliveTimer,keepAliveTimeoutValue,keepAliveTimeoutValue);
    }

    private final static Logger log = LoggerFactory.getLogger(LIUSBServerAdapter.class.getName());

}
