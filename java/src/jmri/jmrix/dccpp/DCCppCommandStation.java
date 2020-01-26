package jmri.jmrix.dccpp;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Defines the standard/common routines used in multiple classes related to the
 * a DCC++ Command Station, on a DCC++ network.
 *
 * @author Bob Jacobsen Copyright (C) 2001
 * @author Portions by Paul Bender Copyright (C) 2003
 * @author Mark Underwood Copyright (C) 2015
 * @author Harald Barth Copyright (C) 2019
 *
 * Based on LenzCommandStation by Bob Jacobsen and Paul Bender
 */
public class DCCppCommandStation implements jmri.CommandStation {

    /* The First group of routines is for obtaining the Software and
     hardware version of the Command station */
    /**
     * We need to add a few data members for saving the version information we
     * get from the layout.
     *
     */
    private String baseStationType;
    private String codeBuildDate;
    private DCCppRegisterManager rmgr = null;
    private int maxNumSlots = 0;

    public DCCppCommandStation() {
        super();
    }

    public DCCppCommandStation(DCCppSystemConnectionMemo memo) {
        super();
        adaptermemo = memo;
    }

    public void setBaseStationType(String s) {
 baseStationType = s;
    }
    
    public String getBaseStationType() {
 return baseStationType;
    }

    public void setCodeBuildDate(String s) {
 codeBuildDate = s;
    }

    public String getCodeBuildDate() {
 return codeBuildDate;
    }

    /**
     * Parses the DCC++ CS status response to pull out the base station version
     * and software version.
     */
    protected void setCommandStationInfo(DCCppReply l) {
 // V1.0 Syntax
 //String syntax = "iDCC\\+\\+\\s+BASE\\s+STATION\\s+v([a-zA-Z0-9_.]+):\\s+BUILD\\s+((\\d+\\s\\w+\\s\\d+)\\s+(\\d+:\\d+:\\d+))";
 // V1.1 Syntax
 //String syntax = "iDCC\\+\\+BASE STATION FOR ARDUINO \\b(\\w+)\\b \\/ (ARDUINO|POLOLU\\sMC33926) MOTOR SHIELD: BUILD ((\\d+\\s\\w+\\s\\d+)\\s+(\\d+:\\d+:\\d+))";
 // V1.0/V1.1 Simplified
 //String syntax = "iDCC\\+\\+(.*): BUILD (.*)";
        // V1.2.1 Syntax
        // String syntax = "iDCC++ BASE STATION FOR ARDUINO \\b(\\w+)\\b \\/ (ARDUINO|POLOLU\\sMC33926) MOTOR SHIELD: ((\\d+\\s\\w+\\s\\d+)\\s+(\\d+:\\d+:\\d+))";
        // Changes from v1.1: space between "DCC++" and "BASE", and "BUILD" is removed.
        // V1.0/V1.1/V1.2 Simplified
        // String syntax = "iDCC\\+\\+\\s?(.*):\\s?(?:BUILD)? (.*)";
        
        baseStationType = l.getStatusVersionString();
        codeBuildDate = l.getStatusBuildDateString();
    }

    protected void setCommandStationMaxNumSlots(DCCppReply l) {
	if (maxNumSlots != 0) {
	    log.error("Command Station maxNumSlots already initialized");
	    return;
	}
        maxNumSlots = l.getValueInt(1);
        log.debug("maxNumSlots set to {}", maxNumSlots);
    }
    protected void setCommandStationMaxNumSlots(int n) {
	if (maxNumSlots != 0) {
	    log.error("Command Station maxNumSlots already initialized");
	    return;
	}
        maxNumSlots = n;
        log.debug("maxNumSlots set to {}", maxNumSlots);
    }
    protected int getCommandStationMaxNumSlots() {
	if (maxNumSlots <= 0) {
	    log.error("Command Station maxNumSlots not initialized yet");
	}
        return maxNumSlots;
    }

    /**
     * Provide the version string returned during the initial check.
     * This function is not yet implemented...
     */
    public String getVersionString() {
        return(baseStationType + ": BUILD " + codeBuildDate);
    }

    /**
     * Remember whether or not in service mode.
     */
    boolean mInServiceMode = false;

    /**
     * DCC++ command station does provide Ops Mode.
     */
    public boolean isOpsModePossible() {
 return true;
    }

    // A few utility functions
    /**
     * Get the Lower byte of a locomotive address from the decimal locomotive
     * address.
     */
    public static int getDCCAddressLow(int address) {
        /* For addresses below 128, we just return the address, otherwise,
         we need to return the upper byte of the address after we add the
         offset 0xC000. The first address used for addresses over 127 is 0xC080*/
        if (address < 128) {
            return (address);
        } else {
            int temp = address + 0xC000;
            temp = temp & 0x00FF;
            return temp;
        }
    }

    /**
     * Get the Upper byte of a locomotive address from the decimal locomotive
     * address.
     */
    public static int getDCCAddressHigh(int address) {
        /* this isn't actually the high byte, For addresses below 128, we
         just return 0, otherwise, we need to return the upper byte of the
         address after we add the offset 0xC000 The first address used for
         addresses over 127 is 0xC080*/
        if (address < 128) {
            return (0x00);
        } else {
            int temp = address + 0xC000;
            temp = temp & 0xFF00;
            temp = temp / 256;
            return temp;
        }
    }

    /* To implement the CommandStation Interface, we have to define the
     sendPacket function */
    /**
     * Send a specific packet to the rails.
     *
     * @param packet  Byte array representing the packet, including the
     *                error-correction byte. Must not be null.
     * @param repeats Number of times to repeat the transmission.
     */
    @Override
    public boolean sendPacket(byte[] packet, int repeats) {

        if (_tc == null) {
            log.error("Send Packet Called without setting traffic controller");
            return false;
        }

        int reg = 0;  // register 0, so this doesn't repeat
        DCCppMessage msg = DCCppMessage.makeWriteDCCPacketMainMsg(reg, packet.length, packet);
        log.debug("sendPacket:'{}'", msg.toString());

        for (int i = 0; i < repeats; i++) {
            _tc.sendDCCppMessage(msg, null);
        }
        return true;
    }

    /*
     * For the command station interface, we need to set the traffic 
     * controller.
     */
    public void setTrafficController(DCCppTrafficController tc) {
        _tc = tc;
    }

    private DCCppTrafficController _tc = null;

    public void setSystemConnectionMemo(DCCppSystemConnectionMemo memo) {
        adaptermemo = memo;
    }

    public DCCppSystemConnectionMemo getSystemConnectionMemo() {
        return adaptermemo;
    }

    private DCCppSystemConnectionMemo adaptermemo;

    private void creatermgr() {
	if (rmgr == null) {
	    rmgr = new DCCppRegisterManager(maxNumSlots);
	}
    }

    @Override
    public String getUserName() {
        if (adaptermemo == null) {
            return "DCC++";
        }
        return adaptermemo.getUserName();
    }

    @Override
    public String getSystemPrefix() {
        if (adaptermemo == null) {
            return "DCCPP";
        }
        return adaptermemo.getSystemPrefix();
    }

    public int requestNewRegister(int addr) {
	creatermgr();
	return(rmgr.requestRegister(addr));
    }

    public void releaseRegister(int addr) {
	creatermgr();
	rmgr.releaseRegister(addr);
    }

    // Return DCCppConstants.NO_REGISTER_FREE if address is not in list
    public int getRegisterNum(int addr) {
	creatermgr();
	return(rmgr.getRegisterNum(addr));
    }

    // Return DCCppConstants.REGISTER_UNALLOCATED if register is unused.
    public int getRegisterAddress(int num) {
	creatermgr();
	return(rmgr.getRegisterAddress(num));
    }

    /*
     * We need to register for logging
     */
    private final static Logger log = LoggerFactory.getLogger(DCCppCommandStation.class);

}

