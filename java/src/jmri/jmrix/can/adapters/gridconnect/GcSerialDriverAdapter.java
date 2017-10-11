package jmri.jmrix.can.adapters.gridconnect;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.LinkedTransferQueue;

import jmri.jmrix.can.TrafficController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import purejavacomm.CommPortIdentifier;
import purejavacomm.NoSuchPortException;
import purejavacomm.PortInUseException;
import purejavacomm.SerialPort;
import purejavacomm.UnsupportedCommOperationException;

/**
 * Implements SerialPortAdapter for the GridConnect protocol.
 * <P>
 *
 * @author Bob Jacobsen Copyright (C) 2001, 2002
 * @author Andrew Crosland Copyright (C) 2008
 */
public class GcSerialDriverAdapter extends GcPortController implements jmri.jmrix.SerialPortAdapter {

    protected SerialPort activeSerialPort = null;

    public GcSerialDriverAdapter() {
        super(new jmri.jmrix.can.CanSystemConnectionMemo());
        option1Name = "Protocol"; // NOI18N
        options.put(option1Name, new Option("Connection Protocol", jmri.jmrix.can.ConfigurationManager.getSystemOptions()));
        this.manufacturerName = jmri.jmrix.merg.MergConnectionTypeList.MERG;
    }

    @Override
    public String openPort(String portName, String appName) {
        String[] baudRates = validBaudRates();
        int[] baudValues = validBaudValues();
        // open the port, check ability to set moderators
        try {
            // get and open the primary port
            CommPortIdentifier portID = CommPortIdentifier.getPortIdentifier(portName);
            try {
                activeSerialPort = (SerialPort) portID.open(appName, 2000);  // name of program, msec to wait
            } catch (PortInUseException p) {
                return handlePortBusy(p, portName, log);
            }

            // try to set it for comunication via SerialDriver
            try {
                // find the baud rate value, configure comm options
                int baud = baudValues[0];  // default, but also defaulted in the initial value of selectedSpeed
                for (int i = 0; i < baudRates.length; i++) {
                    if (baudRates[i].equals(mBaudRate)) {
                        baud = baudValues[i];
                    }
                }
                activeSerialPort.setSerialPortParams(baud, SerialPort.DATABITS_8, SerialPort.STOPBITS_1, SerialPort.PARITY_NONE);
            } catch (UnsupportedCommOperationException e) {
                log.error("Cannot set serial parameters on port " + portName + ": " + e.getMessage());
                return "Cannot set serial parameters on port " + portName + ": " + e.getMessage();
            }

            // disable flow control; hardware lines used for signaling, XON/XOFF might appear in data
            configureLeadsAndFlowControl(activeSerialPort, 0);
            activeSerialPort.enableReceiveTimeout(50);  // 50 mSec timeout before sending chars

            // set timeout
            // activeSerialPort.enableReceiveTimeout(1000);
            log.debug("Serial timeout was observed as: " + activeSerialPort.getReceiveTimeout()
                    + " " + activeSerialPort.isReceiveTimeoutEnabled());

            // get and save stream
            serialStream = activeSerialPort.getInputStream();

            // purge contents, if any
            purgeStream(serialStream);

            // report status?
            if (log.isInfoEnabled()) {
                log.info(portName + " port opened at "
                        + activeSerialPort.getBaudRate() + " baud, sees "
                        + " DTR: " + activeSerialPort.isDTR()
                        + " RTS: " + activeSerialPort.isRTS()
                        + " DSR: " + activeSerialPort.isDSR()
                        + " CTS: " + activeSerialPort.isCTS()
                        + "  CD: " + activeSerialPort.isCD()
                );
            }

            opened = true;

        } catch (NoSuchPortException p) {
            return handlePortNotFound(p, portName, log);
        } catch (UnsupportedCommOperationException | IOException ex) {
            log.error("Unexpected exception while opening port " + portName + " trace follows: " + ex);
            ex.printStackTrace();
            return "Unexpected error while opening port " + portName + ": " + ex;
        }

        return null; // indicates OK return

    }

    /**
     * set up all of the other objects to operate with a CAN RS adapter
     * connected to this port
     */
    @Override
    public void configure() {
        // Register the CAN traffic controller being used for this connection
        //GcTrafficController.instance();
        TrafficController tc = makeGcTrafficController();
        this.getSystemConnectionMemo().setTrafficController(tc);

        // Now connect to the traffic controller
        log.debug("Connecting port");
        tc.connectPort(this);

        this.getSystemConnectionMemo().setProtocol(getOptionState(option1Name));

        // do central protocol-specific configuration    
        //jmri.jmrix.can.ConfigurationManager.configure(getOptionState(option1Name));
        this.getSystemConnectionMemo().configureManagers();

    }

    protected GcTrafficController makeGcTrafficController() {
        return new GcTrafficController();
    }

    private class AsyncBufferInputStream extends FilterInputStream {
        AsyncBufferInputStream(InputStream inputStream) {
            super(inputStream);
            Thread rt = new Thread(()->readThreadBody());
            rt.setDaemon(true);
            rt.setPriority(Thread.MAX_PRIORITY);
            rt.start();
        }

        private BufferEntry tryRead(int len) {
            BufferEntry tail = new BufferEntry();
            try {
                tail.data = new byte[len];
                tail.len = in.read(tail.data, 0, len);
                errorCount = 0;
            } catch (IOException e) {
                tail.e = e;
                e.printStackTrace();
                if (++errorCount > MAX_IO_ERRORS_TO_ABORT) {
                    log.error("Closing read thread due to too many IO errors");
                    return null;
                }
            }
            return tail;
        }

        private void readThreadBody() {
            BufferEntry tail;
            while(true) {
                // Try to read one byte to block the thread.
                tail = tryRead(1);
                if (tail == null) return;
                // TODO: we need to add support for the underlying input stream persistently returning EOF.
                if (tail.len > 0 || tail.e != null) {
                    readAhead.add(tail);
                } else {
                    continue;
                }
                // Read as many bytes as we have in large increments.
                do {
                    tail = tryRead(128);
                    if (tail == null) return;
                    if (tail.len > 0 || tail.e != null) {
                        readAhead.add(tail);
                    } else {
                        break;
                    }
                } while(true);
            }
        }

        private class BufferEntry {
            byte[] data;
            int len = 0;
            IOException e = null;
        }

        @Override
        public int read() throws IOException {
            throw new IOException("unimplemented");
        }

        @Override
        public int read(byte[] bytes) throws IOException {
            throw new IOException("unimplemented");
        }

        @Override
        public synchronized int read(byte[] bytes, int skip, int len) throws IOException {
            if (skip != 0) {
                throw new IOException("unimplemented");
            }
            if (head == null || headOfs >= head.len) {
                while (true) {
                    try {
                        head = readAhead.take();
                        break;
                    } catch (InterruptedException e) {
                    }
                }
                if (head.e != null) {
                    throw head.e;
                }
                headOfs = 0;
                if (head.len < 0) return -1;
            }
            int cp = head.len - headOfs;
            if (cp > len) cp = len;
            System.arraycopy(head.data, headOfs, bytes, 0, cp);
            headOfs += cp;
            return cp;
        }

        private final static int MAX_IO_ERRORS_TO_ABORT = 10;
        private BlockingQueue<BufferEntry> readAhead = new LinkedBlockingQueue<>();
        BufferEntry head = null;
        int headOfs = 0;
        int errorCount = 0;
    }

    // base class methods for the PortController interface
    @Override
    public DataInputStream getInputStream() {
        if (!opened) {
            log.error("getInputStream called before load(), stream not available");
            return null;
        }
        synchronized (this) {
            if (bufferedStream == null) {
                bufferedStream = new AsyncBufferInputStream(serialStream);
            } else {
                log.error("Creating multiple input streams.");
            }
        }
        //return new DataInputStream(serialStream);
        return new DataInputStream(bufferedStream);
    }

    @Override
    public DataOutputStream getOutputStream() {
        if (!opened) {
            log.error("getOutputStream called before load(), stream not available");
        }
        try {
            return new DataOutputStream(activeSerialPort.getOutputStream());
        } catch (java.io.IOException e) {
            log.error("getOutputStream exception: " + e);
        }
        return null;
    }

    @Override
    public boolean status() {
        return opened;
    }

    /**
     * Get an array of valid baud rates.
     */
    @Override
    public String[] validBaudRates() {
        return new String[]{"57,600", "115,200", "230,400", "250,000", "333,333", "460,800"};
    }

    /**
     * And the corresponding values.
     */
    public int[] validBaudValues() {
        return new int[]{57600, 115200, 230400, 250000, 333333, 460800};
    }

    // private control members
    private boolean opened = false;
    InputStream serialStream = null;
    private InputStream bufferedStream = null;

    private final static Logger log = LoggerFactory.getLogger(GcSerialDriverAdapter.class);

}
