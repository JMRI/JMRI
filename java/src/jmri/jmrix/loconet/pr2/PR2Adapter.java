// PR2Adapter.java

package jmri.jmrix.loconet.pr2;

import jmri.jmrix.SystemConnectionMemo;
import jmri.jmrix.loconet.locobuffer.LocoBufferAdapter;
import jmri.jmrix.loconet.*;

import gnu.io.SerialPort;

/**
 * Update the code in jmri.jmrix.loconet.locobuffer so that it 
 * refers to the switch settings on the new Digitrax PR2
 
 * @author			Bob Jacobsen   Copyright (C) 2004, 2005, 2006
 * @version			$Revision$
 */
public class PR2Adapter extends LocoBufferAdapter {


    public PR2Adapter() {
        super();
        options.remove(option2Name);
        /*As this extends the locobuffer, we need to remove the SystemConnectionMemo,
        that it has created and replace it with our own. dispose has to be done to
        the registered connection details.*/
        if (adaptermemo!=null){
            adaptermemo.dispose();
        }
        adaptermemo = new PR2SystemConnectionMemo();
    }

    /**
     * Always use flow control, not considered a user-setable option
     */
    protected void setSerialPort(SerialPort activeSerialPort) throws gnu.io.UnsupportedCommOperationException {
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
        if (getOptionState(option1Name).equals(validOption1[1]))
            flow = SerialPort.FLOWCONTROL_NONE;
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
        jmri.jmrix.loconet.pr2.LnPr2Packetizer packets = new jmri.jmrix.loconet.pr2.LnPr2Packetizer();
        packets.connectPort(this);

        // create memo
        /*PR2SystemConnectionMemo memo 
            = new PR2SystemConnectionMemo(packets, new SlotManager(packets));*/
        adaptermemo.setSlotManager(new SlotManager(packets));
        adaptermemo.setLnTrafficController(packets);
        // do the common manager config
        adaptermemo.configureCommandStation(mCanRead, mProgPowersOff, commandStationName, 
                                            mTurnoutNoRetry, mTurnoutExtraSpace);
        adaptermemo.configureManagers();

        // start operation
        packets.startThreads();
        jmri.jmrix.loconet.ActiveFlag.setActive();

    }

    /**
     * Get an array of valid baud rates. 
     */
    public String[] validBaudRates() {
        return new String[]{"57,600 baud"};
    }

    /**
     * Get an array of valid baud rates as integers. This allows subclasses
     * to change the arrays of speeds.
     */
    public int[] validBaudNumber() {
        return new int[]{57600};
    }

    /**
     * Option 1 controls flow control option
     */
    /*public String option1Name() { return "PR2 connection uses "; }
    public String[] validOption1() { return new String[]{"hardware flow control (recommended)", "no flow control"}; }*/
    // meanings are assigned to these above, so make sure the order is consistent

    /**
     * The PR2 is itself a command station, so fix that choice
     * by providing just the one option
     */
    /*public String[] validOption2() { 
        String[] retval = {"PR2"}; 
        return retval;
    }*/
    
    public SystemConnectionMemo getSystemConnectionMemo() { return adaptermemo; }
    
    public void dispose(){
        if (adaptermemo!=null)
            adaptermemo.dispose();
        adaptermemo = null;
    }

    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(PR2Adapter.class.getName());
}
