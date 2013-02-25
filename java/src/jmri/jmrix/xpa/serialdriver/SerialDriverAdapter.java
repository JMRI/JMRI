// SerialDriverAdapter.java

package jmri.jmrix.xpa.serialdriver;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jmri.jmrix.xpa.XpaPortController;
import jmri.jmrix.xpa.XpaTrafficController;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.InputStream;

import gnu.io.CommPortIdentifier;
import gnu.io.PortInUseException;
import gnu.io.SerialPort;

/**
 * Implements SerialPortAdapter for a modem connected to an XPA.
 * <P>
 * This connects an XPA+Modem connected to an XPressNet based command 
 * station via a serial com port. Normally controlled by the 
 * SerialDriverFrame class.
 * <P>
 * The current implementation only handles the 9,600 baud rate.
 * It uses the first configuraiont variable for the modem initilization 
 * string.
 *
 * @author	Paul Bender   Copyright (C) 2004
 * @version	$Revision$
 */
public class SerialDriverAdapter extends XpaPortController implements jmri.jmrix.SerialPortAdapter {

    public SerialDriverAdapter(){
        super();
        option1Name = "ModemInitString";
        options.put(option1Name, new Option("Modem Initilization String : ", new String[]{"ATX0E0"}));
    }

    SerialPort activeSerialPort = null;

    public String openPort(String portName, String appName)  {
        // open the port, check ability to set moderators
        try {
            // get and open the primary port
            CommPortIdentifier portID = CommPortIdentifier.getPortIdentifier(portName);
            try {
                activeSerialPort = (SerialPort) portID.open(appName, 2000);  // name of program, msec to wait
            }
            catch (PortInUseException p) {
                return handlePortBusy(p, portName, log);
            }

            // try to set it for comunication via SerialDriver
            try {
                activeSerialPort.setSerialPortParams(9600, SerialPort.DATABITS_8, SerialPort.STOPBITS_1, SerialPort.PARITY_NONE);
            } catch (gnu.io.UnsupportedCommOperationException e) {
                log.error("Cannot set serial parameters on port "+portName+": "+e.getMessage());
                return "Cannot set serial parameters on port "+portName+": "+e.getMessage();
            }

            // set RTS high, DTR high
            activeSerialPort.setRTS(true);		// not connected in some serial ports and adapters
            activeSerialPort.setDTR(true);		// pin 1 in DIN8; on main connector, this is DTR

            // disable flow control; hardware lines used for signaling, XON/XOFF might appear in data
            activeSerialPort.setFlowControlMode(0);

            // set timeout
            // activeSerialPort.enableReceiveTimeout(1000);
            log.debug("Serial timeout was observed as: "+activeSerialPort.getReceiveTimeout()
                      +" "+activeSerialPort.isReceiveTimeoutEnabled());

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

        } catch (gnu.io.NoSuchPortException p) {
            return handlePortNotFound(p, portName, log);
        } catch (Exception ex) {
            log.error("Unexpected exception while opening port "+portName+" trace follows: "+ex);
            ex.printStackTrace();
            return "Unexpected error while opening port "+portName+": "+ex;
        }

        return null; // indicates OK return

    }

    /**
     * set up all of the other objects to operate with an XPA+Modem 
     * Connected to an XPressNet based command station connected to this 
     * port
     */
    public void configure() {

        // connect to the traffic controller
        XpaTrafficController.instance().connectPort(this);

        jmri.InstanceManager.setPowerManager(new jmri.jmrix.xpa.XpaPowerManager());

        jmri.InstanceManager.setTurnoutManager(jmri.jmrix.xpa.XpaTurnoutManager.instance());

        // start operation
        // sourceThread = new Thread(p);
        // sourceThread.start();
        sinkThread = new Thread(XpaTrafficController.instance());
        sinkThread.start();

        jmri.InstanceManager.setThrottleManager(new jmri.jmrix.xpa.XpaThrottleManager());

        jmri.jmrix.xpa.ActiveFlag.setActive();

    }

    private Thread sinkThread;

    // base class methods for the XpaPortController interface
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

    private boolean opened = false;
    InputStream serialStream = null;

    static public SerialDriverAdapter instance() {
        if (mInstance == null) mInstance = new SerialDriverAdapter();
        return mInstance;
    }
    static SerialDriverAdapter mInstance = null;
    
    String manufacturerName = jmri.jmrix.DCCManufacturerList.LENZ;
    
    public String getManufacturer() { return manufacturerName; }
    public void setManufacturer(String manu) { manufacturerName=manu; }

    static Logger log = LoggerFactory.getLogger(SerialDriverAdapter.class.getName());

}
