package jmri.jmrix.ieee802154.xbee;

import com.digi.xbee.api.RemoteXBeeDevice;
import com.digi.xbee.api.exceptions.TimeoutException;
import com.digi.xbee.api.exceptions.XBeeException;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.ArrayList;
import jmri.jmrix.AbstractPortController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/*
 * This class provides an interface between the XBee messages that are sent
 * to and from the serial port connected to an XBee device.
 * Remote devices may be sending messages in transparent mode.
 *
 * Some of this code is derived from the XNetSimulator.
 *
 * @Author Paul Bender Copyright (C) 2014
 */
final public class XBeeIOStream extends AbstractPortController {

    private DataOutputStream pout = null; // for output to other classes
    private DataInputStream pin = null; // for input from other classes
    // internal ends of the pipes
    private DataOutputStream outpipe = null;  // data from xbee written here
    // ends up in pin.
    private DataInputStream inpipe = null; // data read from this pipe is
    // sent to the XBee.
    private Thread sourceThread;  // thread writing to the remote xbee
    private Thread sinkThread;  // thread reading from the remote xbee

    private RemoteXBeeDevice remoteXBee;
    private final XBeeTrafficController xtc;

    public XBeeIOStream(XBeeNode node, XBeeTrafficController tc) {
        super(tc.getAdapterMemo());
        remoteXBee = node.getXBee();
        try {
            PipedOutputStream tempPipeI = new PipedOutputStream();
            pout = new DataOutputStream(tempPipeI);
            inpipe = new DataInputStream(new PipedInputStream(tempPipeI));
            PipedOutputStream tempPipeO = new PipedOutputStream();
            outpipe = new DataOutputStream(tempPipeO);
            pin = new DataInputStream(new PipedInputStream(tempPipeO));
        } catch (java.io.IOException e) {
            log.error("init (pipe): Exception: {}", e.toString());
        }

        xtc = tc;
    }

    // routines defined as abstract in AbstractPortController
    @Override
    public DataInputStream getInputStream() {
        if (pin == null) {
            log.error("getInputStream called before load(), stream not available");
        }
        return pin;
    }

    @Override
    public DataOutputStream getOutputStream() {
        if (pout == null) {
            log.error("getOutputStream called before load(), stream not available");
        }
        return pout;
    }

    @Override
    public void connect() {
    }

    @Override
    public void configure() {
        // start the transmit thread
        sourceThread = new Thread(new TransmitThread(remoteXBee, xtc, inpipe));
        sourceThread.setName("xbee.XBeeIOStream Transmit thread");
        sourceThread.start();

        // start the receive thread
        sinkThread = new Thread(new ReceiveThread(remoteXBee, xtc, outpipe));
        sinkThread.setName("xbee.XBeeIOStream Receive thread");
        sinkThread.start();

    }

    @Override
    public boolean status() {
        return (pout != null && pin != null);
    }

    @Override
    public String getCurrentPortName() {
        return "NONE";
    }

    @Override
    public boolean getDisabled() {
        return false;
    }

    @Override
    public void setDisabled(boolean disabled) {
    }

    @Override
    public void dispose() {
        if (sourceThread != null) {
            sourceThread.interrupt();
            sourceThread = null;
        }
        if (sinkThread != null) {
            sinkThread.interrupt();
            sinkThread = null;
        }
        try {
            pin.close();
            pout.close();
            outpipe.close();
            inpipe.close();
        } catch (java.io.IOException ioe) {
            log.debug("IO Exception closing port during dispose call");
        }
        super.dispose();
    }

    @Override
    public void recover() {
    }

    static private class TransmitThread implements Runnable {

        private RemoteXBeeDevice node = null;
        private XBeeTrafficController xtc = null;
        private DataInputStream pipe = null;

        public TransmitThread(RemoteXBeeDevice n, XBeeTrafficController tc, DataInputStream input) {
            node = n;
            xtc = tc;
            pipe = input;
        }

        @Override
        public void run() { // start a new thread
            // this thread has one task.  It repeatedly reads from the input pipe
            // and sends data to the XBee.
            log.debug("XBee Transmit Thread Started");
            for (;;) {
                // the data we send is required to be a byte array.
                // The maximum number of values we
                // can collect is 100.
                ArrayList<Byte> data = new ArrayList<>();
                try {
                    do {
                        log.debug("Attempting byte read");
                        byte b = pipe.readByte();
                        log.debug("Read Byte: {}", b);
                        data.add(data.size(), b);
                    } while (data.size() < 100 && pipe.available() > 0);
                } catch (java.io.IOException e) {
                    log.error("IOException reading serial data from pipe before sending to XBee");
                }
                byte dataArray[] = new byte[data.size()];
                int i = 0;
                for (Byte n : data) {
                    dataArray[i++] = n;
                }
                if (log.isDebugEnabled()) {
                    log.debug("XBee Thread received message {}", jmri.util.StringUtil.hexStringFromBytes(dataArray));
                }
                try {
                    xtc.getXBee().sendData(node, dataArray);
                } catch (TimeoutException te) {
                    log.error("Timeout sending stream data to node {}.", node);
                } catch (XBeeException xbe) {
                    log.error("Exception sending stream data to node {}.", node);
                } catch (NullPointerException npe) {
                    if (Thread.interrupted()) {
                        return;
                    }
                }
            }
        }

    }

    static private class ReceiveThread implements Runnable {

        private final RemoteXBeeDevice node;
        private final XBeeTrafficController xtc;
        private final DataOutputStream pipe;

        public ReceiveThread(RemoteXBeeDevice n, XBeeTrafficController tc, DataOutputStream output) {
            node = n;
            xtc = tc;
            pipe = output;
        }

        @Override
        public void run() { // start a new thread
            // this thread has one task.  It repeatedly reads from the XBee
            // and writes data to the output pipe
            if (log.isDebugEnabled()) {
                log.debug("XBee Receive Thread Started");
            }
            for (;;) {
                try {
                    com.digi.xbee.api.models.XBeeMessage message = xtc.getXBee().readDataFrom(node, 100);
                    if (message != null) {
                        byte data[] = message.getData();
                        log.debug("Received {}", data);
                        for (int i = 0; i < data.length; i++) {
                            pipe.write(data[i]);
                        }
                    }
                } catch (java.io.IOException ioe) {
                    log.error("IOException writing serial data from XBee to pipe");
                } catch (java.lang.NullPointerException npe) {
                    log.error("NullPointerException writing serial data from XBee to pipe");
                    // this is fatal, return
                    return;
                }
            }
        }

    }

    private final static Logger log = LoggerFactory.getLogger(XBeeIOStream.class);

}
