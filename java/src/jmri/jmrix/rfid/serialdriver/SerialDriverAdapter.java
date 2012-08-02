// SerialDriverAdapter.java

package jmri.jmrix.rfid.serialdriver;

import jmri.jmrix.rfid.RfidPortController;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.InputStream;

import gnu.io.CommPortIdentifier;
import gnu.io.PortInUseException;
import gnu.io.SerialPort;
import gnu.io.SerialPortEvent;
import gnu.io.SerialPortEventListener;
import jmri.jmrix.rfid.RfidSystemConnectionMemo;
import jmri.jmrix.rfid.RfidTrafficController;

/**
 * Provide access to RFID devices via a serial comm port.
 * Derived from the oaktree code.
 * @author      Bob Jacobsen   Copyright (C) 2006, 2007, 2008
 * @author      Matthew Harris  Copyright (C) 2011
 * @version     $Revision$
 * @since       2.11.4
 */
public class SerialDriverAdapter extends RfidPortController implements jmri.jmrix.SerialPortAdapter {

    SerialPort activeSerialPort = null;

    public SerialDriverAdapter() {
        super();
        option1Name = "Adapter";
        option2Name = "Concentrator-Range";
        options.put(option1Name, new Option(option1Name, "Adapter:", new String[]{"Generic Stand-alone", "MERG Concentrator"}, false));
        options.put(option2Name, new Option(option2Name, "Concentrator range:", new String[]{"A-H","I-P"}, false));
    }

    @Override
    public RfidSystemConnectionMemo getSystemConnectionMemo() {
        return adapterMemo;
    }

    public String openPort(String portName, String appName)  {
        try {
            // get and open the primary port
            CommPortIdentifier portID = CommPortIdentifier.getPortIdentifier(portName);
            try {
                activeSerialPort = (SerialPort) portID.open(appName, 2000);  // name of program, msec to wait
            }
            catch (PortInUseException p) {
                return handlePortBusy(p, portName, log);
            }
            // try to set it for serial
            try {
                setSerialPort();
            } catch (gnu.io.UnsupportedCommOperationException e) {
                log.error("Cannot set serial parameters on port "+portName+": "+e.getMessage());
                return "Cannot set serial parameters on port "+portName+": "+e.getMessage();
            }


            // set framing (end) character
            try {
                log.debug("Serial framing was observed as: "+activeSerialPort.isReceiveFramingEnabled()
                      +" "+activeSerialPort.getReceiveFramingByte());
            } catch (Exception ef) {
                log.debug("failed to set serial framing: "+ef);
            }

            // set timeout; framing should work before this anyway
            try {
                activeSerialPort.enableReceiveTimeout(10);
                log.debug("Serial timeout was observed as: "+activeSerialPort.getReceiveTimeout()
                      +" "+activeSerialPort.isReceiveTimeoutEnabled());
            } catch (Exception et) {
                log.info("failed to set serial timeout: "+et);
            }
            
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
                // report now
                log.info(portName+" port opened at "
                         +activeSerialPort.getBaudRate()+" baud with"
                         +" DTR: "+activeSerialPort.isDTR()
                         +" RTS: "+activeSerialPort.isRTS()
                         +" DSR: "+activeSerialPort.isDSR()
                         +" CTS: "+activeSerialPort.isCTS()
                         +"  CD: "+activeSerialPort.isCD()
                         );
            }
            if (log.isDebugEnabled()) {
                // report additional status
                log.debug(" port flow control shows "+
                          (activeSerialPort.getFlowControlMode()==SerialPort.FLOWCONTROL_RTSCTS_OUT?"hardware flow control":"no flow control"));
            }
            if (log.isDebugEnabled()) {
                // arrange to notify later
                activeSerialPort.addEventListener(new SerialPortEventListener(){
                        public void serialEvent(SerialPortEvent e) {
                            int type = e.getEventType();
                            switch (type) {
                            case SerialPortEvent.DATA_AVAILABLE:
                                log.info("SerialEvent: DATA_AVAILABLE is "+e.getNewValue());
                                return;
                            case SerialPortEvent.OUTPUT_BUFFER_EMPTY:
                                log.info("SerialEvent: OUTPUT_BUFFER_EMPTY is "+e.getNewValue());
                                return;
                            case SerialPortEvent.CTS:
                                log.info("SerialEvent: CTS is "+e.getNewValue());
                                return;
                            case SerialPortEvent.DSR:
                                log.info("SerialEvent: DSR is "+e.getNewValue());
                                return;
                            case SerialPortEvent.RI:
                                log.info("SerialEvent: RI is "+e.getNewValue());
                                return;
                            case SerialPortEvent.CD:
                                log.info("SerialEvent: CD is "+e.getNewValue());
                                return;
                            case SerialPortEvent.OE:
                                log.info("SerialEvent: OE (overrun error) is "+e.getNewValue());
                                return;
                            case SerialPortEvent.PE:
                                log.info("SerialEvent: PE (parity error) is "+e.getNewValue());
                                return;
                            case SerialPortEvent.FE:
                                log.info("SerialEvent: FE (framing error) is "+e.getNewValue());
                                return;
                            case SerialPortEvent.BI:
                                log.info("SerialEvent: BI (break interrupt) is "+e.getNewValue());
                                return;
                            default:
                                log.info("SerialEvent of unknown type: "+type+" value: "+e.getNewValue());
                                return;
                            }
                        }
                    }
                                                  );
                try { activeSerialPort.notifyOnFramingError(true); }
                catch (Exception e) { log.debug("Could not notifyOnFramingError: "+e); }

                try { activeSerialPort.notifyOnBreakInterrupt(true); }
                catch (Exception e) { log.debug("Could not notifyOnBreakInterrupt: "+e); }

                try { activeSerialPort.notifyOnParityError(true); }
                catch (Exception e) { log.debug("Could not notifyOnParityError: "+e); }

                try { activeSerialPort.notifyOnOverrunError(true); }
                catch (Exception e) { log.debug("Could not notifyOnOverrunError: "+e); }

            }

            opened = true;

        } catch (gnu.io.NoSuchPortException p) {
            return handlePortNotFound(p, portName, log);
        } catch (Exception ex) {
            log.error("Unexpected exception while opening port "+portName+" trace follows: "+ex);
            ex.printStackTrace();
            return "Unexpected error while opening port "+portName+": "+ex;
        }

        return null; // normal operation
    }

    /**
     * Can the port accept additional characters? Yes, always
     */
    public boolean okToSend() {
        return true;
    }

    /**
     * set up all of the other objects to operate
     * connected to this port
     */
    public void configure() {
        RfidTrafficController control = null;
        // set up the system connection first
        String opt1 = getOptionState(option1Name);
        if (opt1.equals("Generic Stand-alone")) {
            // create a Generic Stand-alone port controller
            log.debug("Create Generic Standalone SpecificTrafficController");
            adapterMemo = new jmri.jmrix.rfid.generic.standalone.SpecificSystemConnectionMemo();
            control = new jmri.jmrix.rfid.generic.standalone.SpecificTrafficController(adapterMemo);
        } else if (opt1.equals("MERG Concentrator")) {
            // create a MERG Concentrator port controller
            log.debug("Create MERG Concentrator SpecificTrafficController");
            adapterMemo = new jmri.jmrix.rfid.merg.concentrator.SpecificSystemConnectionMemo();
            control = new jmri.jmrix.rfid.merg.concentrator.SpecificTrafficController(adapterMemo, getOptionState(option2Name));
        } else {
            // no connection at all - warn
            log.warn("protocol option "+opt1+" defaults to Generic Stand-alone");
            // create a Generic Stand-alone port controller
            adapterMemo = new jmri.jmrix.rfid.generic.standalone.SpecificSystemConnectionMemo();
            control = new jmri.jmrix.rfid.generic.standalone.SpecificTrafficController(adapterMemo);
        }    

        // connect to the traffic controller
        adapterMemo.setRfidTrafficController(control);
        control.setAdapterMemo(adapterMemo);
        adapterMemo.configureManagers();
        control.connectPort(this);

        // declare up
        jmri.jmrix.rfid.ActiveFlag.setActive();
    }

    // base class methods for the RfidPortController interface
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
            log.error("getOutputStream exception: "+e.getMessage());
     	}
     	return null;
    }

    public boolean status() {return opened;}

    /**
     * Local method to do specific port configuration
     */
    protected void setSerialPort() throws gnu.io.UnsupportedCommOperationException {
        // find the baud rate value, configure comm options
        int baud = 9600;  // default, but also defaulted in the initial value of selectedSpeed
        
        // check for specific port type
        activeSerialPort.setSerialPortParams(baud, SerialPort.DATABITS_8,
                                SerialPort.STOPBITS_1, SerialPort.PARITY_NONE);

        // set RTS high, DTR high - done early, so flow control can be configured after
        activeSerialPort.setRTS(true);		// not connected in some serial ports and adapters
        activeSerialPort.setDTR(true);		// pin 1 in DIN8; on main connector, this is DTR

        // find and configure flow control
        int flow = SerialPort.FLOWCONTROL_NONE; // default
        activeSerialPort.setFlowControlMode(flow);
    }

    /**
     * Get an array of valid baud rates.
     */
    @edu.umd.cs.findbugs.annotations.SuppressWarnings(value="EI_EXPOSE_REP")
    public String[] validBaudRates() {
        return validSpeeds;
    }

    /**
     * Set the baud rate.
     */
    @Override
    public void configureBaudRate(String rate) {
        log.debug("configureBaudRate: "+rate);
        selectedSpeed = rate;
        super.configureBaudRate(rate);
    }
    
    protected String [] validSpeeds = new String[]{"(automatic)"};
    protected int [] validSpeedValues = new int[]{9600};
    protected String selectedSpeed=validSpeeds[0];
    
    // private control members
    private boolean opened = false;
    InputStream serialStream = null;
    
    String manufacturerName = jmri.jmrix.DCCManufacturerList.RFID;
    
    @Override
    public String getManufacturer() { return manufacturerName; }
    @Override
    public void setManufacturer(String manu) { manufacturerName=manu; }
    
    private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(SerialDriverAdapter.class.getName());

}
