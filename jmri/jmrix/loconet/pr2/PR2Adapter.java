// PR2Adapter.java

package jmri.jmrix.loconet.pr2;

import jmri.jmrix.loconet.locobuffer.LocoBufferAdapter;
import javax.comm.SerialPort;

/**
 * Update the code in jmri.jmrix.loconet.locobuffer so that it 
 * refers to the switch settings on the new Digitrax PR2
 
 * @author			Bob Jacobsen   Copyright (C) 2004, 2005, 2006
 * @version			$Revision: 1.3 $
 */
public class PR2Adapter extends LocoBufferAdapter {


    public PR2Adapter() {
        super();
        m2Instance = this;
    }

    /**
     * Always use flow control, not considered a user-setable option
     */
    protected void setSerialPort(SerialPort activeSerialPort) throws javax.comm.UnsupportedCommOperationException {
        // find the baud rate value, configure comm options
        int baud = 57600;  // default, but also defaulted in the initial value of selectedSpeed
        for (int i = 0; i<validBaudNumber().length; i++ )
            if (validBaudRates()[i].equals(mBaudRate))
                baud = validBaudNumber()[i];
        activeSerialPort.setSerialPortParams(baud, SerialPort.DATABITS_8,
                                             SerialPort.STOPBITS_1, SerialPort.PARITY_NONE);

        // set RTS high, DTR high - done early, so flow control can be configured after
        activeSerialPort.setRTS(true);		// not connected in some serial ports and adapters
        activeSerialPort.setDTR(true);		// pin 1 in Mac DIN8; on main connector, this is DTR

        // configure flow control to always on
        int flow = SerialPort.FLOWCONTROL_RTSCTS_OUT; 
        activeSerialPort.setFlowControlMode(flow);
        log.debug("Found flow control "+activeSerialPort.getFlowControlMode()
                  +" RTSCTS_OUT="+SerialPort.FLOWCONTROL_RTSCTS_OUT
                  +" RTSCTS_IN= "+SerialPort.FLOWCONTROL_RTSCTS_IN);
    }

    /**
     * Set up all of the other objects to operate with a PR2
     * connected to this port. This overrides the version in
     * loconet.locobuffer, but it has to duplicate much of the
     * functionality there, so the code is basically copied.
     */
    public void configure() {
        // connect to a packetizing traffic controller
        // that does echoing
        jmri.jmrix.loconet.pr2.LnPacketizer packets = new jmri.jmrix.loconet.pr2.LnPacketizer();
        packets.connectPort(this);

        // do the common manager config
        configureCommandStation(mCanRead, mProgPowersOff, commandStationName);
        configureManagers();

        // start operation
        packets.startThreads();
        jmri.jmrix.loconet.ActiveFlag.setActive();

    }

   /**
     * Configure the subset of LocoNet managers valid for the PR2.
     * This overrides the method in LnPortController, which is more general.
     */
    static public void configureManagers() {
        jmri.InstanceManager.setPowerManager(new jmri.jmrix.loconet.pr2.LnPr2PowerManager());

        /* jmri.InstanceManager.setTurnoutManager(new jmri.jmrix.loconet.LnTurnoutManager()); */

        /* jmri.InstanceManager.setLightManager(new jmri.jmrix.loconet.LnLightManager()); */

        /* jmri.InstanceManager.setSensorManager(new jmri.jmrix.loconet.LnSensorManager()); */

        jmri.InstanceManager.setThrottleManager(new jmri.jmrix.loconet.LnPr2ThrottleManager());

        /* jmri.InstanceManager.setReporterManager(new jmri.jmrix.loconet.LnReporterManager()); */

    }

    /**
     * Get an array of valid baud rates. 
     */
    public String[] validBaudRates() {
        return validSpeeds;
    }
    protected String [] validSpeeds = new String[]{"57,600 baud"};
    /**
     * Get an array of valid baud rates as integers. This allows subclasses
     * to change the arrays of speeds.
     */
    public int[] validBaudNumber() {
        return validSpeedValues;
    }
    protected int [] validSpeedValues = new int[]{57600};

    /**
     * Since option 1 is not used for this, return an array with one empty element
     */
    public String[] validOption1() { return new String[]{""}; }

    /**
     * Option 1 not used, so return a null string.
     */
    public String option1Name() { return ""; }

    /**
     * The PR2 is itself a command station, so fix that choice
     * by providing just the one option
     */
    public String[] validOption2() { 
        String[] retval = {"PR2"}; 
        return retval;
    }


    static public boolean hasInstance() { return (null!=m2Instance); }
    static public LocoBufferAdapter instance() {
        if (m2Instance == null) {
        	m2Instance = new PR2Adapter();
        	log.debug("new default instance in Pr2Adapter");
        }
        log.debug("PR2Adapter.instance returns object of class "+m2Instance.getClass().getName());
        return m2Instance;
    }
    static private PR2Adapter m2Instance = null;

    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(PR2Adapter.class.getName());
}
