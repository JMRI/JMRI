package jmri.jmrix.dccpp.dccppovertcp;

import jmri.jmrix.dccpp.DCCppCommandStation;
import jmri.jmrix.dccpp.DCCppInitializationManager;
import jmri.jmrix.dccpp.DCCppNetworkPortController;
import jmri.jmrix.dccpp.DCCppPortController;
import jmri.jmrix.dccpp.DCCppSystemConnectionMemo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implements DCCppPortController for the DCCppOverTcp system network
 * connection.
 * <p>
 * This connects a DCC++ via a telnet connection. Normally controlled by the
 * DCCppTcpDriverFrame class. Based on LnTcpDriverAdapter.
 *
 * @author Bob Jacobsen Copyright (C) 2001, 2002, 2003
 * @author Alex Shepherd Copyright (C) 2003, 2006
 * @author Mark Underwood Copyright (C) 2015
 */
public class DCCppTcpDriverAdapter extends DCCppNetworkPortController {
    
    public DCCppTcpDriverAdapter() {
        super(new DCCppSystemConnectionMemo());
        // TODO: Figure out what these options are, and should be.
        //option2Name = "CommandStation";
        //option3Name = "TurnoutHandle";
        //options.put(option2Name, new Option("Command station type:", commandStationNames, false));
        //options.put(option3Name, new Option("Turnout command handling:", new String[]{"Normal", "Spread", "One Only", "Both"}));
    }
    
    /**
     * Set up all of the other objects to operate with a DCC++ connected via
     * this class.
     */
    @Override
    public void configure() {
        
        //setCommandStationType(getOptionState(option2Name));
        //setTurnoutHandling(getOptionState(option3Name));
        // connect to a packetizing traffic controller
        DCCppOverTcpPacketizer packets = new DCCppOverTcpPacketizer(new DCCppCommandStation());
        packets.connectPort(this);
        
        // set the traffic controller
        this.getSystemConnectionMemo().setDCCppTrafficController(packets);
        // do the common manager config
        
        new DCCppInitializationManager(this.getSystemConnectionMemo());
        
        // start operation
        packets.startThreads();
    }
    
    @Override
    public boolean status() {
        return opened;
    }
    
    // private control members
    private boolean opened = false;
    
    @Override
    public void configureOption1(String value) {
        super.configureOption1(value);
        log.debug("configureOption1: {}", value);
        setCommandStationType(value);
    }
    
    @Override
    public void setOutputBufferEmpty(boolean s) {
    }
    
    @Override
    public boolean okToSend() { return true; }
    
    private final static Logger log = LoggerFactory.getLogger(DCCppTcpDriverAdapter.class);

}
