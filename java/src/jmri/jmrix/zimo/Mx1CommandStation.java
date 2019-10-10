package jmri.jmrix.zimo;

import javax.annotation.Nonnull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Defines standard operations for Dcc command stations.
 *
 * @author	Bob Jacobsen Copyright (C) 2001
 *
 * Adapted by Sip Bosch for use with Zimo Mx-1
 *
 */
public class Mx1CommandStation implements jmri.CommandStation {

    public Mx1CommandStation(String systemName, String userName) {
        this.systemName = systemName;
        this.userName = userName;
    }
 
     public Mx1CommandStation(String systemName) {
        this(systemName, "MX-1");
    }
   
     // not multi-connection safe
     public Mx1CommandStation() {
        this("Z", "MX-1");
    }
   
    String systemName;
    String userName;
    
    /**
     * {@inheritDoc}
     * <p>
     * This implementation always returns false, as sending
     * a packet isn't implemented for the Zimo command stations
     */
    @Override
    public boolean sendPacket(@Nonnull byte[] packet, int repeats) { return false; }

    @Override
    public String getUserName() {return userName;}

    @Override
    public String getSystemPrefix() {return systemName;}

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

    // private final static Logger log = LoggerFactory.getLogger(Mx1CommandStation.class);
}
