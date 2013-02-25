// PR3Adapter.java

package jmri.jmrix.loconet.pr3;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jmri.jmrix.SystemConnectionMemo;
import jmri.jmrix.loconet.locobuffer.LocoBufferAdapter;
import jmri.jmrix.loconet.*;

import gnu.io.SerialPort;

/**
 * Update the code in jmri.jmrix.loconet.locobuffer so that it 
 * refers to the switch settings on the new Digitrax PR3
 
 * @author			Bob Jacobsen   Copyright (C) 2004, 2005, 2006, 2008
 * @version			$Revision$
 */
public class PR3Adapter extends LocoBufferAdapter {


    public PR3Adapter() {
        super();
        
        options.remove(option2Name);
        options.put(option2Name, new Option("Command station type:", commandStationOptions(), false));
        
        /*As this extends the locobuffer, we need to remove the SystemConnectionMemo,
        that it has created and replace it with our own. dispose has to be done to
        the registered connection details.*/
        if (adaptermemo!=null){
            adaptermemo.dispose();
        }
        adaptermemo = new PR3SystemConnectionMemo();
    }

    /**
     * Always use flow control, not considered a user-settable option
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
     * Set up all of the other objects to operate with a PR3
     * connected to this port. This overrides the version in
     * loconet.locobuffer, but it has to duplicate much of the
     * functionality there, so the code is basically copied.
     */
    public void configure() {
        setCommandStationType(getOptionState(option2Name));
        setTurnoutHandling(getOptionState(option3Name));
        if (commandStationName.startsWith("PR3")) {
            // PR3 case
            // connect to a packetizing traffic controller
            // that does echoing
            jmri.jmrix.loconet.pr2.LnPr2Packetizer packets = new jmri.jmrix.loconet.pr2.LnPr2Packetizer();
            packets.connectPort(this);
    
            // create memo
            /*PR3SystemConnectionMemo memo 
                = new PR3SystemConnectionMemo(packets, new SlotManager(packets));*/
            adaptermemo.setSlotManager(new SlotManager(packets));
            adaptermemo.setLnTrafficController(packets);
            // do the common manager config
            adaptermemo.configureCommandStation(mCanRead, mProgPowersOff, commandStationName, 
                                                mTurnoutNoRetry, mTurnoutExtraSpace);
            PR3SystemConnectionMemo memo = (PR3SystemConnectionMemo)adaptermemo;
            memo.configureManagersPR2();
    
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
            packets.sendLocoNetMessage(msg);
            
        } else {
            // MS100 modes
            // connect to a packetizing traffic controller
            LnPacketizer packets = new LnPacketizer();
            packets.connectPort(this);
    
            // create memo
            /*PR3SystemConnectionMemo memo 
                = new PR3SystemConnectionMemo(packets, new SlotManager(packets));*/
            adaptermemo.setSlotManager(new SlotManager(packets));
            adaptermemo.setLnTrafficController(packets);
            // do the common manager config
            adaptermemo.configureCommandStation(mCanRead, mProgPowersOff, commandStationName, 
                                            mTurnoutNoRetry, mTurnoutExtraSpace);
            
            PR3SystemConnectionMemo memo = (PR3SystemConnectionMemo)adaptermemo;
            memo.configureManagersMS100();
    
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
            packets.sendLocoNetMessage(msg);
        }
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

    // Option 1 does flow control, inherited from LocoBufferAdapter
    
    /**
     * The PR3 can be used in numerous modes, so handle that
     */
    public String[] commandStationOptions() {
        String[] retval = new String[commandStationNames.length+2];
        retval[0] = "PR3 standalone programmer";
        for (int i=0; i<commandStationNames.length; i++) {
            retval[i+1] = commandStationNames[i];
        }
        retval[retval.length-1] = "Stand-alone LocoNet";
        return retval;
    }
    
    public SystemConnectionMemo getSystemConnectionMemo() { return adaptermemo; }
    
    public void dispose(){
        if (adaptermemo!=null)
            adaptermemo.dispose();
        adaptermemo = null;
    }

    static Logger log = LoggerFactory.getLogger(PR3Adapter.class.getName());
}
