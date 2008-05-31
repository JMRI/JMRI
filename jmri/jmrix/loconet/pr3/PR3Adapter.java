// PR3Adapter.java

package jmri.jmrix.loconet.pr3;

import jmri.jmrix.loconet.locobuffer.LocoBufferAdapter;
import jmri.jmrix.loconet.LnPacketizer;
import jmri.jmrix.loconet.LnTrafficController;
import jmri.jmrix.loconet.LocoNetMessage;

import javax.comm.SerialPort;

/**
 * Update the code in jmri.jmrix.loconet.locobuffer so that it 
 * refers to the switch settings on the new Digitrax PR3
 
 * @author			Bob Jacobsen   Copyright (C) 2004, 2005, 2006, 2008
 * @version			$Revision: 1.2 $
 */
public class PR3Adapter extends LocoBufferAdapter {


    public PR3Adapter() {
        super();
        m2Instance = this;
    }

    /**
     * Always use flow control, not considered a user-settable option
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
        if (mOpt1.equals(validOption1[1]))
            flow = SerialPort.FLOWCONTROL_NONE;
        activeSerialPort.setFlowControlMode(flow);
        log.debug("Found flow control "+activeSerialPort.getFlowControlMode()
                  +" RTSCTS_OUT="+SerialPort.FLOWCONTROL_RTSCTS_OUT
                  +" RTSCTS_IN= "+SerialPort.FLOWCONTROL_RTSCTS_IN);
    }

    /**
     * Set up all of the other objects to operate with a PR3
     * connected to this port. This overrides the version in
     * loconet.locobuffer, but it has to duplicate much of the
     * functionality there, so the code is basically copied.
     */
    public void configure() {

        if (commandStationName.startsWith("PR3")) {
            // PR3 case
            // connect to a packetizing traffic controller
            // that does echoing
            jmri.jmrix.loconet.pr2.LnPacketizer packets = new jmri.jmrix.loconet.pr2.LnPacketizer();
            packets.connectPort(this);
    
            // do the common manager config
            configureCommandStation(mCanRead, mProgPowersOff, commandStationName);
            configureManagersPR2();
    
            // start operation
            packets.startThreads();
            jmri.jmrix.loconet.ActiveFlag.setActive();
            
            // set mode
            LocoNetMessage msg = new LocoNetMessage( 6 ) ;
            msg.setOpCode( 0xD3 );
            msg.setElement( 1, 0x10 );
            msg.setElement( 2, 1 );  // set PR2
            msg.setElement( 3, 0 );
            msg.setElement( 4, 0 );
            LnTrafficController.instance().sendLocoNetMessage(msg);
            
        } else {
            // MS100 modes
            // connect to a packetizing traffic controller
            LnPacketizer packets = new LnPacketizer();
            packets.connectPort(this);
    
            // do the common manager config
            configureCommandStation(mCanRead, mProgPowersOff, commandStationName);
            configureManagersMS100();
    
            // start operation
            packets.startThreads();
            jmri.jmrix.loconet.ActiveFlag.setActive();
            
            // set mode
            LocoNetMessage msg = new LocoNetMessage( 6 ) ;
            msg.setOpCode( 0xD3 );
            msg.setElement( 1, 0x10 );
            msg.setElement( 2, 0 );  // set MS100, no power
            if (commandStationName.startsWith("Stand-alone"))
                msg.setElement( 2, 3 );  // set MS100, with power
            msg.setElement( 3, 0 );
            msg.setElement( 4, 0 );
            LnTrafficController.instance().sendLocoNetMessage(msg);
        }
    }

   /**
     * Configure the subset of LocoNet managers valid for the PR3 in PR2 mode.
     * This is used instead of the method in LnPortController, which is more general.
     */
    static public void configureManagersPR2() {
        
        jmri.InstanceManager.setPowerManager(new jmri.jmrix.loconet.pr2.LnPr2PowerManager());

        jmri.InstanceManager.setThrottleManager(new jmri.jmrix.loconet.LnPr2ThrottleManager());
    }

   /**
     * Configure the subset of LocoNet managers valid for the PR3 in MS100 mode.
     * This is used instead of the method in LnPortController, which is more general.
     */
    static public void configureManagersMS100() {
        
        jmri.InstanceManager.setPowerManager(new jmri.jmrix.loconet.LnPowerManager());

        jmri.InstanceManager.setTurnoutManager(new jmri.jmrix.loconet.LnTurnoutManager());

        jmri.InstanceManager.setLightManager(new jmri.jmrix.loconet.LnLightManager());

        jmri.InstanceManager.setSensorManager(new jmri.jmrix.loconet.LnSensorManager());

        jmri.InstanceManager.setThrottleManager(new jmri.jmrix.loconet.LnThrottleManager());

        jmri.InstanceManager.setReporterManager(new jmri.jmrix.loconet.LnReporterManager());

        jmri.InstanceManager.addClockControl(new jmri.jmrix.loconet.LnClockControl());

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

    // Option 1 does flow control, inherited from LocoBufferAdapter
    
    /**
     * The PR3 can be used in numerous modes, so handle that
     */
    public String[] validOption2() {
        String[] retval = new String[commandStationNames.length+2];
        retval[0] = "PR3 standalone programmer";
        for (int i=0; i<commandStationNames.length; i++) {
            retval[i+1] = commandStationNames[i];
        }
        retval[retval.length-1] = "Stand-alone LocoNet";
        return retval;
    }


    static public boolean hasInstance() { return (null!=m2Instance); }
    static public LocoBufferAdapter instance() {
        if (m2Instance == null) {
        	m2Instance = new PR3Adapter();
        	log.debug("new default instance in PR3Adapter");
        }
        log.debug("PR3Adapter.instance returns object of class "+m2Instance.getClass().getName());
        return m2Instance;
    }
    static private PR3Adapter m2Instance = null;

    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(PR3Adapter.class.getName());
}
