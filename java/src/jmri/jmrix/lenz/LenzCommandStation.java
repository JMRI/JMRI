/*
 * LenzCommandStation.java
 */
package jmri.jmrix.lenz;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Defines the standard/common routines used in multiple classes related to the
 * a Lenz Command Station, on an XPressNet network.
 *
 * @author	Bob Jacobsen Copyright (C) 2001 Portions by Paul Bender Copyright (C)
 * 2003
 */
public class LenzCommandStation implements jmri.jmrix.DccCommandStation, jmri.CommandStation {

    /* The First group of routines is for obtaining the Software and
     hardware version of the Command station */
    /**
     * We need to add a few data members for saving the version information we
     * get from the layout.
     *
     */
    private int cmdStationType = -1;
    private float cmdStationSoftwareVersion = -1;
    private int cmdStationSoftwareVersionBCD = -1;

    /**
     * return the CS Type
     *
     */
    public int getCommandStationType() {
        return cmdStationType;
    }

    /**
     * set the CS Type
     *
     */
    public void setCommandStationType(int t) {
        cmdStationType = t;
    }

    /**
     * Set the CS type based on an XPressNet Message
     *
     */
    public void setCommandStationType(XNetReply l) {
        if (l.getElement(0) == XNetConstants.CS_SERVICE_MODE_RESPONSE) {
            // This is the Command Station Software Version Response
            if (l.getElement(1) == XNetConstants.CS_SOFTWARE_VERSION) {
                cmdStationType = l.getElement(3);
            }
        }
    }

    /**
     * return the CS Software Version
     *
     */
    public float getCommandStationSoftwareVersion() {
        return cmdStationSoftwareVersion;
    }

    /**
     * return the CS Software Version in BCD (for use in comparisons)
     *
     */
    public float getCommandStationSoftwareVersionBCD() {
        return cmdStationSoftwareVersionBCD;
    }

    /**
     * set the CS Software Version
     *
     */
    public void setCommandStationSoftwareVersion(float v) {
        cmdStationSoftwareVersion = v;
    }

    /**
     * Set the CS Software Version based on an XPressNet Message
     *
     */
    public void setCommandStationSoftwareVersion(XNetReply l) {
        if (l.getElement(0) == XNetConstants.CS_SERVICE_MODE_RESPONSE) {
            // This is the Command Station Software Version Response
            if (l.getElement(1) == XNetConstants.CS_SOFTWARE_VERSION) {
                try {
                    cmdStationSoftwareVersion = (l.getElementBCD(2).floatValue()) / 10;
                } catch (java.lang.NumberFormatException nfe) {
                    // the number was not in BCD format as expected.
                    // the upper nibble is the major version and the lower 
                    // nibble is the minor version.
                    cmdStationSoftwareVersion = ((l.getElement(2) & 0xf0) >> 4) + (l.getElement(2) & 0x0f) / 100.0f;
                }
                cmdStationSoftwareVersionBCD = l.getElement(2);
            }
        }
    }

    /**
     * Provides the version string returned during the initial check.
     */
    @Override
    public String getVersionString() {
        return "hardware type: " + getCommandStationType() 
                + " software version: " + getCommandStationSoftwareVersionBCD();
    }

    /* 
     * The next group of messages has to do with determining if the
     * command station has, and is currently in service mode 
     */
    /**
     * Lenz does use a service mode
     */
    @Override
    public boolean getHasServiceMode() {
        return true;
    }

    /**
     * If this command station has a service mode, is the command station
     * currently in that mode?
     */
    @Override
    public boolean getInServiceMode() {
        return mInServiceMode;
    }

    /**
     * Remember whether or not in service mode
     *
     */
    boolean mInServiceMode = false;

    /**
     * XPressNet command station does provide Ops Mode We should make this
     * return false based on what command station we're using but for now, we'll
     * return true
     */
    public boolean isOpsModePossible() {
        if (cmdStationType == 0x01 || cmdStationType == 0x02) {
            return false;
        } else {
            return true;
        }
    }

    // A few utility functions
    /**
     * Get the Lower byte of a locomotive address from the decimal locomotive
     * address
     */
    public static int getDCCAddressLow(int address) {
        /* For addresses below 100, we just return the address, otherwise,
         we need to return the upper byte of the address after we add the
         offset 0xC000. The first address used for addresses over 99 is 0xC064*/
        if (address < 100) {
            return (address);
        } else {
            int temp = address + 0xC000;
            temp = temp & 0x00FF;
            return temp;
        }
    }

    /**
     * Get the Upper byte of a locomotive address from the decimal locomotive
     * address
     */
    public static int getDCCAddressHigh(int address) {
        /* this isn't actually the high byte, For addresses below 100, we
         just return 0, otherwise, we need to return the upper byte of the
         address after we add the offset 0xC000 The first address used for
         addresses over 99 is 0xC064*/
        if (address < 100) {
            return (0x00);
        } else {
            int temp = address + 0xC000;
            temp = temp & 0xFF00;
            temp = temp / 256;
            return temp;
        }

    }

    /* To Implement the CommandStation Interface, we have to define the 
     sendPacket function */
    /**
     * Send a specific packet to the rails.
     * <p>
     * @param packet  Byte array representing the packet, including the
     *                error-correction byte. Must not be null.
     * @param repeats Number of times to repeat the transmission.
     */
    @Override
    public void sendPacket(byte[] packet, int repeats) {

        if (_tc == null) {
            log.error("Send Packet Called without setting traffic controller");
            return;
        }

        XNetMessage msg = XNetMessage.getNMRAXNetMsg(packet);
        for (int i = 0; i < repeats; i++) {
            _tc.sendXNetMessage(msg, null);
        }
    }

    /*
     * For the command station interface, we need to set the traffic 
     * controller.
     */
    public void setTrafficController(XNetTrafficController tc) {
        _tc = tc;
    }

    private XNetTrafficController _tc = null;

    public void setSystemConnectionMemo(XNetSystemConnectionMemo memo) {
        adaptermemo = memo;
    }

    XNetSystemConnectionMemo adaptermemo;

    @Override
    public String getUserName() {
        if (adaptermemo == null) {
            return "XPressnet";
        }
        return adaptermemo.getUserName();
    }

    @Override
    public String getSystemPrefix() {
        if (adaptermemo == null) {
            return "X";
        }
        return adaptermemo.getSystemPrefix();
    }

    /*
     * We need to register for logging
     */
    private final static Logger log = LoggerFactory.getLogger(LenzCommandStation.class.getName());

}


/* @(#)LenzCommandStation.java */
