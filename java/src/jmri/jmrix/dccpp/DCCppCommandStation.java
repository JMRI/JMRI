package jmri.jmrix.dccpp;

import javax.annotation.Nonnull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Defines the standard/common routines used in multiple classes related to the
 * DCC++ Command Station, on a DCC++ network.
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
    @Nonnull private String stationType = "Unknown";
    @Nonnull private String build       = "Unknown";
    @Nonnull private String version     = "0.0.0";
    private DCCppRegisterManager rmgr = null;
    private int maxNumSlots = DCCppConstants.MAX_MAIN_REGISTERS; //default to register size

    public DCCppCommandStation() {
        super();
    }

    public DCCppCommandStation(DCCppSystemConnectionMemo memo) {
        super();
        adaptermemo = memo;
    }

    public void setStationType(String s) {
        if (!stationType.equals(s)) {
            log.info("Station Type set to '{}'", s);
            stationType = s;            
        }
    }
    
    /**
     * Get the Station Type of the connected Command Station
     * it is populated by response from the CS, initially "Unknown" 
     * @return StationType
     */
    @Nonnull
    public String getStationType() {
        return stationType;
    }

    public void setBuild(String s) {
        if (!build.equals(s)) {
            log.info("Build set to '{}'", s);
            build = s;            
        }
    }

    /**
     * Get the Build of the connected Command Station
     * it is populated by response from the CS, initially "Unknown" 
     * @return Build
     */
    @Nonnull
    public String getBuild() {
        return build;
    }

    public void setVersion(String s) {
        if (!version.equals(s)) {
            if (jmri.Version.isCanonicalVersion(s)) {
                log.info("Version set to '{}'", s);
                version = s;
            } else {
                log.warn("'{}' is not a canonical version, version not changed", s);
            }
        }
    }

    /**
     * Get the canonical version of the connected Command Station
     * it is populated by response from the CS, so initially '0.0.0' 
     * @return Version
     */
    @Nonnull
    public String getVersion() {
        return version;
    }

    /**
     * Parse the DCC++ CS status response to pull out the base station version
     * and software version.
     * @param l status response to query.
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

        setStationType(l.getStationType());
        setBuild(l.getBuildString());
        setVersion(l.getVersion());
    }

    protected void setCommandStationMaxNumSlots(DCCppReply l) {
        int newNumSlots = l.getValueInt(1);
        setCommandStationMaxNumSlots(newNumSlots);
    }
    protected void setCommandStationMaxNumSlots(int newNumSlots) {
        if (newNumSlots < maxNumSlots) {
            log.warn("Command Station maxNumSlots cannot be reduced from {} to {}", maxNumSlots, newNumSlots);
            return;
        }
        if (newNumSlots != maxNumSlots) {
            log.info("changing maxNumSlots from {} to {}", maxNumSlots, newNumSlots);
            maxNumSlots = newNumSlots;
        }
    }
    protected int getCommandStationMaxNumSlots() {
        return maxNumSlots;
    }

    /**
     * Provide the version string returned during the initial check.
     * @return version string.
     */
    public String getVersionString() {
        return(stationType + ": BUILD " + build);
    }

    /**
     * Remember whether or not in service mode.
     */
    boolean mInServiceMode = false;

    /**
     * DCC++ command station does provide Ops Mode.
     * @return always true.
     */
    public boolean isOpsModePossible() {
        return true;
    }

    /**
     * Does this command station require JMRI to send periodic function refresh packets?
     * @return true if required, false if not
     */
    public boolean isFunctionRefreshRequired() {
        boolean ret = true;
        try {
            //command stations starting with 3 handle their own function refresh
            ret = (jmri.Version.compareCanonicalVersions(version, "3.0.0") < 0);
        } catch (IllegalArgumentException ignore) {
        }
        return ret;  
    }

    /**
     * Can this command station handle the Read with a starting value ('V'erify)
     * @return true if yes or false if no
     */
    public boolean isReadStartValSupported() {
        boolean ret = false;
        try {
            //command stations starting with 3 can handle reads with startVals
            ret = (jmri.Version.compareCanonicalVersions(version, "3.0.0") >= 0);
        } catch (IllegalArgumentException ignore) {
        }
        return ret;  
    }

    /**
     * Can this command station handle the Servo and Vpin Turnout creation message formats?
     * @return true if yes or false if no
     */
    public boolean isServoTurnoutCreationSupported() {
        boolean ret = false;
        try {
            // SERVO and VPIN turnout commands added at 3.2.0
            ret = (jmri.Version.compareCanonicalVersions(version, "3.2.0") >= 0);
        } catch (IllegalArgumentException ignore) {
        }
        return ret;  
    }

    // A few utility functions
    /**
     * Get the Lower byte of a locomotive address from the decimal locomotive
     * address.
     * @param address loco address.
     * @return loco address byte lo.
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
     * @param address loco address.
     * @return high byte of address.
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
    public boolean sendPacket(@Nonnull byte [] packet, int repeats) {

        if (_tc == null) {
            log.error("Send Packet Called without setting traffic controller");
            return false;
        }

        int reg = 0;  // register 0, so this doesn't repeat
        //  DCC++ BaseStation code appends its own error-correction byte.
        // So we have to omit the JMRI-generated one.
        DCCppMessage msg = DCCppMessage.makeWriteDCCPacketMainMsg(reg, packet.length - 1, packet);
        assert msg != null;
        log.debug("sendPacket:'{}'", msg);

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
    @Nonnull
    public String getSystemPrefix() {
        if (adaptermemo == null) {
            return "D";
        }
        return adaptermemo.getSystemPrefix();
    }

    public int requestNewRegister(int addr) {
        creatermgr();
        return (rmgr.requestRegister(addr));
    }

    public void releaseRegister(int addr) {
        creatermgr();
        rmgr.releaseRegister(addr);
    }

    // Return DCCppConstants.NO_REGISTER_FREE if address is not in list
    public int getRegisterNum(int addr) {
        creatermgr();
        return (rmgr.getRegisterNum(addr));
    }

    // Return DCCppConstants.REGISTER_UNALLOCATED if register is unused.
    public int getRegisterAddress(int num) {
        creatermgr();
        return (rmgr.getRegisterAddress(num));
    }

    /*
     * We need to register for logging
     */
    private final static Logger log = LoggerFactory.getLogger(DCCppCommandStation.class);

}
