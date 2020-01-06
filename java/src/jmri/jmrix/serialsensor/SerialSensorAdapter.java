package jmri.jmrix.serialsensor;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Comparator;
import java.util.ResourceBundle;
import java.util.TooManyListenersException;
import jmri.InstanceManager;
import jmri.JmriException;
import jmri.NamedBean;
import jmri.Sensor;
import jmri.jmrix.AbstractSerialPortController;
import jmri.jmrix.SystemConnectionMemo;
import jmri.util.NamedBeanComparator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import purejavacomm.CommPortIdentifier;
import purejavacomm.NoSuchPortException;
import purejavacomm.PortInUseException;
import purejavacomm.SerialPort;
import purejavacomm.SerialPortEvent;
import purejavacomm.SerialPortEventListener;
import purejavacomm.UnsupportedCommOperationException;

/**
 * Implements SerialPortAdapter for connecting to two sensors via the serial
 * port. Sensor "1" will be via DCD, and sensor "2" via DSR
 *
 * @author	Bob Jacobsen Copyright (C) 2003
 */
public class SerialSensorAdapter extends AbstractSerialPortController {

    SerialPort activeSerialPort = null;

    public SerialSensorAdapter() {
        super(new SystemConnectionMemo("S", Bundle.getMessage("TypeSerial")) {

            @Override
            protected ResourceBundle getActionModelResourceBundle() {
                return null;
            }

            @Override
            public <B extends NamedBean> Comparator<B> getNamedBeanComparator(Class<B> type) {
                return (o1, o2) -> o1.getSystemName().compareTo(o2.getSystemName());
            }
        });
    }

    @Override
    public void configure() {
        log.debug("Configure doesn't do anything here");
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
                activeSerialPort.setSerialPortParams(9600, SerialPort.DATABITS_8, SerialPort.STOPBITS_1, SerialPort.PARITY_NONE);
            } catch (UnsupportedCommOperationException e) {
                log.error("Cannot set serial parameters on port {}: {}", portName, e.getMessage());
                return "Cannot set serial parameters on port " + portName + ": " + e.getMessage();
            }

            // disable flow control; hardware lines used for signaling, XON/XOFF might appear in data
            // set RTS active low, DTR inactive high
            configureLeadsAndFlowControl(activeSerialPort, 0, true, false);

            // set timeout
            // activeSerialPort.enableReceiveTimeout(1000);
            log.debug("Serial timeout was observed as: {} {}", activeSerialPort.getReceiveTimeout()
                    + " " + activeSerialPort.isReceiveTimeoutEnabled());

            // arrange to notify of sensor changes
            activeSerialPort.addEventListener(new SerialPortEventListener() {
                @Override
                public void serialEvent(SerialPortEvent e) {
                    int type = e.getEventType();
                    switch (type) {
                        case SerialPortEvent.DSR:
                            log.info("SerialEvent: DSR is {}", e.getNewValue());
                            notify("1", e.getNewValue());
                            return;
                        case SerialPortEvent.CD:
                            log.info("SerialEvent: CD is {}", e.getNewValue());
                            notify("2", e.getNewValue());
                            return;
                        case SerialPortEvent.CTS:
                            log.info("SerialEvent: CTS is {}", e.getNewValue());
                            notify("3", e.getNewValue());
                            return;
                        default:
                            if (log.isDebugEnabled()) {
                                log.debug("SerialEvent of type: {} value: {}", type, e.getNewValue());
                            }
                            return;
                    }
                }

                /**
                 * Do a sensor change on the event queue
                 */
                public void notify(String sensor, boolean value) {
                    javax.swing.SwingUtilities.invokeLater(new SerialNotifier(sensor, value));
                }
            });
            // turn on notification
            activeSerialPort.notifyOnCTS(true);
            activeSerialPort.notifyOnDSR(true);
            activeSerialPort.notifyOnCarrierDetect(true);

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

        } catch (NoSuchPortException ex1) {
            log.error("No such port {} ", portName, ex1);
            return "No such port " + portName + ": " + ex1;
        } catch (TooManyListenersException ex3) {
            log.error("Too Many Listeners on port {} ", portName, ex3);
            return "Too Many Listeners on port " + portName + ": " + ex3;
        } catch (IOException ex4) {
            log.error("I/O error on port {} ", portName, ex4);
            return "I/O error on port " + portName + ": " + ex4;
        }

        return null; // indicates OK return
    }

    @Override
    public DataInputStream getInputStream() {
        if (!opened) {
            log.error("getInputStream called before load(), stream not available");
            return null;
        }
        return new DataInputStream(serialStream);
    }

    @Override
    public DataOutputStream getOutputStream() {
        if (!opened) {
            log.error("getOutputStream called before load(), stream not available");
        }
        try {
            return new DataOutputStream(activeSerialPort.getOutputStream());
        } catch (java.io.IOException e) {
            log.error("getOutputStream exception: ", e);
        }
        return null;
    }

    @Override
    public boolean status() {
        return opened;
    }

    /**
     * {@inheritDoc}
     * Currently only 19,200 bps.
     */
    @Override
    public String[] validBaudRates() {
        return new String[]{"9,600 bps"};
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int[] validBaudNumbers() {
        return new int[]{9600};
    }

    @Override
    public int defaultBaudIndex() {
        return 0;
    }

    /**
     * {@inheritDoc}
     * This currently does nothing, as there's only one
     * possible value.
     */
    @Override
    public void configureBaudRate(String rate) {
    }

    // private control members
    private boolean opened = false;
    InputStream serialStream = null;

    /**
     * Do a sensor change on the event queue.
     */
    public void notify(String sensor, boolean value) {
    }

    /**
     * Internal class to remember the Message object and destination listener
     * when a message is queued for notification.
     */
    static class SerialNotifier implements Runnable {

        String mSensor;
        boolean mValue;

        SerialNotifier(String pSensor, boolean pValue) {
            mSensor = pSensor;
            mValue = pValue;
        }

        @Override
        public void run() {
            log.debug("serial sensor notify starts");
            int value = Sensor.INACTIVE;
            if (mValue) {
                value = Sensor.ACTIVE;
            }
            try {
                InstanceManager.sensorManagerInstance().provideSensor(mSensor)
                        .setKnownState(value);
            } catch (JmriException | IllegalArgumentException e) {
                log.error("Exception setting state: ", e);
            }
        }
    }

    private final static Logger log = LoggerFactory.getLogger(SerialSensorAdapter.class);

}
