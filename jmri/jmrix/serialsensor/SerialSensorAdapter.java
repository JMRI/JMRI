// SerialSensorAdapter.java

package jmri.jmrix.serialsensor;

import java.io.*;
import java.util.*;

import javax.comm.*;

import jmri.*;
import jmri.jmrix.*;

/**
 * Implements SerialPortAdapter for connecting to two sensors via the
 * serial port.  Sensor "1" will be via DCD, and sensor "2" via DSR
 *
 * @author			Bob Jacobsen   Copyright (C) 2003
 * @version			$Revision: 1.1 $
 */
public class SerialSensorAdapter extends AbstractPortController
                implements jmri.jmrix.SerialPortAdapter  {

    Vector portNameVector = null;
    SerialPort activeSerialPort = null;

    public void configure() {
        log.debug("Configure doesnt do anything here");
    }

    public Vector getPortNames() {
        // first, check that the comm package can be opened and ports seen
        portNameVector = new Vector();
        Enumeration portIDs = CommPortIdentifier.getPortIdentifiers();
        // find the names of suitable ports
        while (portIDs.hasMoreElements()) {
            CommPortIdentifier id = (CommPortIdentifier) portIDs.nextElement();
            // accumulate the names in a vector
            portNameVector.addElement(id.getName());
        }
        return portNameVector;
    }

    public String openPort(String portName, String appName)  {
        // open the port, check ability to set moderators
        try {
            // get and open the primary port
            CommPortIdentifier portID = CommPortIdentifier.getPortIdentifier(portName);
            try {
                activeSerialPort = (SerialPort) portID.open(appName, 100);  // name of program, msec to wait
            }
            catch (PortInUseException p) {
                return handlePortBusy(p, portName, log);
            }

            // try to set it for comunication via SerialDriver
            try {
                activeSerialPort.setSerialPortParams(9600, SerialPort.DATABITS_8, SerialPort.STOPBITS_1, SerialPort.PARITY_NONE);
            } catch (javax.comm.UnsupportedCommOperationException e) {
                log.error("Cannot set serial parameters on port "+portName+": "+e.getMessage());
                return "Cannot set serial parameters on port "+portName+": "+e.getMessage();
            }

            // set RTS high, DTR high
            activeSerialPort.setRTS(true);		// not connected in some serial ports and adapters
            activeSerialPort.setDTR(true);		// pin 1 in DIN8; on main connector, this is DTR

            // disable flow control; hardware lines used for signalling, XON/XOFF might appear in data
            activeSerialPort.setFlowControlMode(0);

            // set timeout
            // activeSerialPort.enableReceiveTimeout(1000);
            log.debug("Serial timeout was observed as: "+activeSerialPort.getReceiveTimeout()
                      +" "+activeSerialPort.isReceiveTimeoutEnabled());

            // arrange to notify of sensor changes
            activeSerialPort.addEventListener(new SerialPortEventListener(){
                    public void serialEvent(SerialPortEvent e) {
                        int type = e.getEventType();
                        switch (type) {
                        case SerialPortEvent.DSR:
                            log.info("SerialEvent: DSR is "+e.getNewValue());
                            notify("1", e.getNewValue());
                            return;
                        case SerialPortEvent.CD:
                            log.info("SerialEvent: CD is "+e.getNewValue());
                            notify("2", e.getNewValue());
                            return;
                        case SerialPortEvent.CTS:
                            log.info("SerialEvent: CTS is "+e.getNewValue());
                            notify("3", e.getNewValue());
                            return;
                        default:
                            if (log.isDebugEnabled()) log.debug("SerialEvent of type: "+type+" value: "+e.getNewValue());
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
            int count = serialStream.available();
            log.debug("input stream shows "+count+" bytes available");
            while ( count > 0) {
                serialStream.skip(count);
                count = serialStream.available();
            }

            // report status?
            if (log.isInfoEnabled()) {
                log.info(portName+" port opened at "
                         +activeSerialPort.getBaudRate()+" baud, sees "
                         +" DTR: "+activeSerialPort.isDTR()
                         +" RTS: "+activeSerialPort.isRTS()
                         +" DSR: "+activeSerialPort.isDSR()
                         +" CTS: "+activeSerialPort.isCTS()
                         +"  CD: "+activeSerialPort.isCD()
                         );
            }

            opened = true;

        }
        catch (Exception ex) {
            log.error("Unexpected exception while opening port "+portName+" trace follows: "+ex);
            ex.printStackTrace();
            return "Unexpected error while opening port "+portName+": "+ex;
        }

        return null; // indicates OK return

    }


    private Thread sinkThread;

    public DataInputStream getInputStream() {
        if (!opened) {
            log.error("getInputStream called before load(), stream not available");
            return null;
        }
        return new DataInputStream(serialStream);
    }

    public DataOutputStream getOutputStream() {
        if (!opened) log.error("getOutputStream called before load(), stream not available");
        try {
            return new DataOutputStream(activeSerialPort.getOutputStream());
        }
     	catch (java.io.IOException e) {
            log.error("getOutputStream exception: "+e);
     	}
     	return null;
    }

    public boolean status() {return opened;}

    /**
     * Get an array of valid baud rates. This is currently only 19,200 bps
     */
    public String[] validBaudRates() {
        return new String[]{"9,600 bps"};
    }

    /**
     * Set the baud rate.  This currently does nothing, as there's
     * only one possible value
     */
    public void configureBaudRate(String rate) {}

    /**
     * Since option 1 is not used for this, return an array with just a single string
     */
    public String[] validOption1() { return new String[]{""}; }

    /**
     * Option 1 not used, so return a null string.
     */
    public String option1Name() { return ""; }

    /**
     * The first port option isn't used, so just ignore this call.
     */
    public void configureOption1(String value) {}

    /**
     * Get an array of valid values for "option 2"; used to display valid options.
     * May not be null, but may have zero entries
     */
    public String[] validOption2() { return new String[]{""}; }

    /**
     * Get a String that says what Option 2 represents
     * May be an empty string, but will not be null
     */
    public String option2Name() { return ""; }

    /**
     * Set the second port option.  Only to be used after construction, but
     * before the openPort call
     */
    public void configureOption2(String value) throws jmri.jmrix.SerialConfigException {}

    // private control members
    private boolean opened = false;
    InputStream serialStream = null;

    /**
     * Do a sensor change on the event queue
     */
    public void notify(String sensor, boolean value) {
    }

    /**
     * Internal class to remember the Message object and destination
     * listener when a message is queued for notification.
     */
    class SerialNotifier implements Runnable {
        String mSensor;
        boolean mValue;
        SerialNotifier(String pSensor, boolean pValue) {
            mSensor = pSensor;
            mValue = pValue;
        }
        public void run() {
            log.debug("serial sensor notify starts");
            int value = Sensor.INACTIVE;
            if (mValue) value = Sensor.ACTIVE;
            try {
                InstanceManager.sensorManagerInstance().newSensor(null, mSensor)
                    .setKnownState(value);
            } catch (JmriException e) { log.error("Exception setting state: "+e); }
        }
    }

    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(SerialSensorAdapter.class.getName());

}
