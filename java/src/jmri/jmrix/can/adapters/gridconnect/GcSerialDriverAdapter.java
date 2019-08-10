package jmri.jmrix.can.adapters.gridconnect;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
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
 *
 * @author Bob Jacobsen Copyright (C) 2001, 2002
 * @author Andrew Crosland Copyright (C) 2008
 * @author Balazs Racz Copyright (C) 2017
 */
public class GcSerialDriverAdapter extends GcPortController {

    protected SerialPort activeSerialPort = null;

    public GcSerialDriverAdapter() {
        super(new jmri.jmrix.can.CanSystemConnectionMemo());
        option1Name = "Protocol"; // NOI18N
        options.put(option1Name, new Option(Bundle.getMessage("ConnectionProtocol"),
                jmri.jmrix.can.ConfigurationManager.getSystemOptions()));
        this.manufacturerName = jmri.jmrix.merg.MergConnectionTypeList.MERG;
    }

    @Override
    public String openPort(String portName, String appName) {
        // open the port, check ability to set moderators
        try {
            // get and open the primary port
            CommPortIdentifier portID = CommPortIdentifier.getPortIdentifier(portName);
            try {
                activeSerialPort = (SerialPort) portID.open(appName, 2000);  // name of program, msec to wait
            } catch (PortInUseException p) {
                return handlePortBusy(p, portName, log);
            }

            // try to set it for communication via SerialDriver
            try {
                // find the baud rate value, configure comm options
                int baud = currentBaudNumber(mBaudRate);
                activeSerialPort.setSerialPortParams(baud, SerialPort.DATABITS_8, SerialPort.STOPBITS_1, SerialPort.PARITY_NONE);
            } catch (UnsupportedCommOperationException e) {
                log.error("Cannot set serial parameters on port {}: {}", portName, e.getMessage());
                return "Cannot set serial parameters on port " + portName + ": " + e.getMessage();
            }

            // disable flow control; hardware lines used for signaling, XON/XOFF might appear in data
            configureLeadsAndFlowControl(activeSerialPort, 0);
            activeSerialPort.enableReceiveTimeout(50);  // 50 mSec timeout before sending chars

            // set timeout
            // activeSerialPort.enableReceiveTimeout(1000);
            log.debug("Serial timeout was observed as: {} {}",
                    activeSerialPort.getReceiveTimeout(),
                    activeSerialPort.isReceiveTimeoutEnabled());

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
            log.error("Unexpected exception while opening port {}", portName, ex);
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

    /**
     * Helper class wrapping the input serial port's InputStream. It starts a
     * helper thread at high priority that reads the input serial port as fast
     * as it can, buffering all incoming data in memory in a queue. The queue in
     * unbounded and readers will get the data from the queue.
     * <p>
     * This class is thread-safe.
     */
    private static class AsyncBufferInputStream extends FilterInputStream {

        AsyncBufferInputStream(InputStream inputStream, String portName) {
            super(inputStream);
            this.portName = portName;
            Thread rt = new Thread(this::readThreadBody);
            rt.setName("GcSerialPort InputBufferThread " + portName);
            rt.setDaemon(true);
            rt.setPriority(Thread.MAX_PRIORITY);
            rt.start();
        }

        /**
         * Helper function that tries to perform a read from the underlying port
         * with a given maximum length.
         *
         * @param len how many bytes to request from the port. Setting this to 1
         *            will apparently block the thread if there are zero bytes
         *            available.
         * @return a block of data read, or nullptr if fatal IO errors make
         *         further use of this port impossible.
         */
        private BufferEntry tryRead(int len) {
            BufferEntry tail = new BufferEntry();
            try {
                tail.data = new byte[len];
                tail.len = in.read(tail.data, 0, len);
                errorCount = 0;
            } catch (IOException e) {
                tail.e = e;
                if (++errorCount > MAX_IO_ERRORS_TO_ABORT) {
                    log.error("Closing read thread due to too many IO errors", e);
                    return null;
                } else {
                    log.warn("Error reading serial port {}", portName, e);
                }
            }
            return tail;
        }

        /**
         * Implementation of the buffering thread.
         */
        private void readThreadBody() {
            BufferEntry tail;
            while (true) {
                // Try to read one byte to block the thread.
                tail = tryRead(1);
                if (tail == null) {
                    return;
                }
                // NOTE: in order to reuse this class in a generic context, we need to add support
                // for the underlying input stream persistently returning EOF. That does not
                // happen on a serial port.
                if (tail.len > 0 || tail.e != null) {
                    readAhead.add(tail);
                } else {
                    continue;
                }
                // Read as many bytes as we have in large increments. REading 128 bytes is a good
                // compromise between throughput (4 gridconnect packets per kernel IO) but not
                // wasting a lot of memory if less data actually shows up.
                do {
                    tail = tryRead(128);
                    if (tail == null) {
                        return;
                    }
                    if (tail.len > 0 || tail.e != null) {
                        readAhead.add(tail);
                    } else {
                        break;
                    }
                } while (true);
            }
        }

        /**
         * We queue objects of this class between the read thread and the actual
         * read() methods.
         */
        private static class BufferEntry {

            // data payload
            byte[] data;
            // how many bytes of data are filled in
            int len = 0;
            // an exception was caught reading the input stream
            IOException e = null;
        }

        @Override
        public int read() throws IOException {
            throw new UnsupportedOperationException();
        }

        @Override
        public int read(byte[] bytes) throws IOException {
            throw new UnsupportedOperationException();
        }

        @Override
        public synchronized int read(byte[] bytes, int skip, int len) throws IOException {
            if (skip != 0) {
                throw new UnsupportedOperationException();
            }
            if (head == null || headOfs >= head.len) {
                try {
                    head = readAhead.take();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                if (head.e != null) {
                    throw head.e;
                }
                headOfs = 0;
                if (head.len < 0) {
                    return -1;
                }
            }
            int cp = head.len - headOfs;
            if (cp > len) {
                cp = len;
            }
            System.arraycopy(head.data, headOfs, bytes, 0, cp);
            headOfs += cp;
            return cp;
        }

        private final String portName;
        // After this many consecutive read attempts resulting in an exception we will terminate
        // the read thread and return the last exception to the reader.
        private final static int MAX_IO_ERRORS_TO_ABORT = 10;
        // Queue holding the buffered data.
        private final BlockingQueue<BufferEntry> readAhead = new LinkedBlockingQueue<>();
        // The last entry we got from the queue if there are still bytes we need to return from it.
        BufferEntry head = null;
        // Offset of next live byte in head.
        int headOfs = 0;
        // How many of the last consecutive read attempts have resulted in an exception.
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
                bufferedStream = new AsyncBufferInputStream(serialStream, activeSerialPort.getName());
            }
            return new DataInputStream(bufferedStream);
        }
    }

    @Override
    public DataOutputStream getOutputStream() {
        if (!opened) {
            log.error("getOutputStream called before load(), stream not available");
        }
        try {
            return new DataOutputStream(activeSerialPort.getOutputStream());
        } catch (java.io.IOException e) {
            log.error("getOutputStream exception: {}", e.getMessage());
        }
        return null;
    }

    @Override
    public boolean status() {
        return opened;
    }

    /**
     * {@inheritDoc}
     *
     * @return array of localized valid baud rates
     */
    @Override
    public String[] validBaudRates() {
        return new String[]{Bundle.getMessage("Baud57600"),
                Bundle.getMessage("Baud115200"), Bundle.getMessage("Baud230400"),
                Bundle.getMessage("Baud250000"), Bundle.getMessage("Baud333333"),
                Bundle.getMessage("Baud460800")};
    }

    /**
     * Get an array of valid baud rates.
     *
     * @return valid baud rates
     */
    @Override
    public int[] validBaudNumbers() {
        return new int[]{57600, 115200, 230400, 250000, 333333, 460800};
    }

    @Override
    public int defaultBaudIndex() {
        return 0;
    }

    /**
     * Migration method
     * @deprecated since 4.16
     */
    @Deprecated
    public int[] validBaudValues() {
        return validBaudNumbers();
    }

    // private control members
    private boolean opened = false;
    InputStream serialStream = null;
    // Stream wrapper that buffers the input bytes.
    private InputStream bufferedStream = null;

    private final static Logger log = LoggerFactory.getLogger(GcSerialDriverAdapter.class);

}
