/*
 * Mx1CommandStation.java
 */
package jmri.jmrix.zimo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Defines standard operations for Dcc command stations.
 *
 * @author	Bob Jacobsen Copyright (C) 2001
 * @version	$Revision$
 *
 * Adapted by Sip Bosch for use with Zimo Mx-1
 *
 */
public class Mx1CommandStation implements jmri.jmrix.DccCommandStation {

    /**
     * Zimo does use a service mode
     */
    public boolean getHasServiceMode() {
        return true;
    }

    /**
     * If this command station has a service mode, is the command station
     * currently in that mode?
     */
    public boolean getInServiceMode() {
        return mInServiceMode;
    }

    /**
     * Provides the version string returned during the initial check. This
     * function is not yet implemented...
     *
     */
    public String getVersionString() {
        return "<unknown>";
    }

    /**
     * Remember whether or not in service mode
     *
     */
    boolean mInServiceMode = false;

    /**
     * Generate a message to change turnout state
     */
    public Mx1Message getTurnoutCommandMsg(int pNumber, boolean pClose,
            boolean pThrow, boolean pOn) {
        Mx1Message l = new Mx1Message(6);
        l.setElement(0, 0x4E);

        // compute module address
        int modAdress = ((pNumber - 1) / 32) + 1;
        // break down into two bytes
        int modHigh = (modAdress & 0xF0) / 16;
        int modLow = modAdress & 0x0F;

        // compose the command-byte
        int number = pNumber - 1;
        //if (!pOn) number = (number | 0xC0);
        if (pThrow) {
            number = (number | 0xC0);
        }
        if (pClose) {
            number = (number | 0x80);
        }
        // break output number down into two bytes
        int numHigh = (number & 0xF0) / 16;
        int numLow = number & 0x0F;

        // we don't know how to command both states right now!
        if (pClose & pThrow) {
            log.error("Zimo turnout logic can't handle both THROWN and CLOSED yet");
        }

        // built and send message
        l.setElement(1, bcdToAsc(modHigh));
        l.setElement(2, bcdToAsc(modLow));
        l.setElement(3, bcdToAsc(numHigh));
        l.setElement(4, bcdToAsc(numLow));
        return l;
    }

    /**
     * If this is a turnout-type message, return address. Otherwise return -1.
     * Note we only identify the command now; the reponse to a request for
     * status is not yet seen here.
     */
    public int getTurnoutMsgAddr(Mx1Message pMsg) {
        if (isTurnoutCommand(pMsg)) {
            javax.swing.JOptionPane.showMessageDialog(null, "A-Programma komt tot hier!");
            int a1 = pMsg.getElement(1);
            int a2 = pMsg.getElement(2);
            return (((a1 & 0xff) * 4) + (a2 & 0x6) / 2 + 1);
        } else {
            return -1;
        }
    }

    /**
     * Is this a command to change turnout state?
     */
    public boolean isTurnoutCommand(Mx1Message pMsg) {
        return pMsg.getElement(0) == 0x4E;
    }

    public Mx1Message resetModeMsg() {
        Mx1Message m = new Mx1Message(3);
        m.setElement(0, 0x53);
        m.setElement(1, 0x45);
        return m;
    }

    public Mx1Message getReadPagedCVMsg(int cv) {
        Mx1Message m = new Mx1Message(4);
        // break down into two bytes
        int cvhigh = (cv & 0xF0) / 16;
        int cvlow = cv & 0x0F;
        // built message
        m.setElement(0, 0x51);
        m.setElement(1, bcdToAsc(cvhigh));
        m.setElement(2, bcdToAsc(cvlow));
        return m;
    }

    public Mx1Message getWritePagedCVMsg(int cv, int val) {
        Mx1Message m = new Mx1Message(7);
        // break down into two bytes
        int cvHigh = (cv & 0xF0) / 16;
        int cvLow = cv & 0x0F;
        int valHigh = (val & 0xF0) / 16;
        int valLow = val & 0x0F;
        // built message
        m.setElement(0, 0x52);
        m.setElement(1, 0x4E);
        m.setElement(2, bcdToAsc(cvHigh));
        m.setElement(3, bcdToAsc(cvLow));
        m.setElement(4, bcdToAsc(valHigh));
        m.setElement(5, bcdToAsc(valLow));
        return m;
    }

    public int bcdToAsc(int hex) {
        switch (hex) {
            case 0x0F:
                return 0x46;
            case 0x0E:
                return 0x65;
            case 0x0D:
                return 0x44;
            case 0x0C:
                return 0x43;
            case 0x0B:
                return 0x42;
            case 0x0A:
                return 0x41;
            case 0x09:
                return 0x39;
            case 0x08:
                return 0x38;
            case 0x07:
                return 0x37;
            case 0x06:
                return 0x36;
            case 0x05:
                return 0x35;
            case 0x04:
                return 0x34;
            case 0x03:
                return 0x33;
            case 0x02:
                return 0x32;
            case 0x01:
                return 0x31;
            default:
                return 0x30;
        }
    }

    private final static Logger log = LoggerFactory.getLogger(Mx1CommandStation.class.getName());

}


/* @(#)Mx1CommandStation.java */
