// SerialSignalHead.java

package jmri.jmrix.grapevine;

import jmri.DefaultSignalHead;
import jmri.SignalHead;

/**
 * SerialSignalHead.java
 *
 *  This object doesn't listen to the Grapevine serial communications. 
 *  It probably should, however, in case 
 *
 * Description:		extend jmri.AbstractSignalHead for grapevine serial signals
 * @author			Bob Jacobsen Copyright (C) 2003, 2006, 2007
 * @version			$Revision: 1.1 $
 */
public class SerialSignalHead extends DefaultSignalHead {

    /**
     * Create a SignalHead object, with only a system name.
     * <P>
     * 'systemName' should have been previously validated
     */
    public SerialSignalHead(String systemName) {
        super(systemName);
        // Save system Name
        tSystemName = systemName;
        // Extract the Bit from the name
        tBit = SerialAddress.getBitFromSystemName(systemName);
    }

    /**
     * Create a SignalHead object, with both system and user names.
     * <P>
     * 'systemName' should have been previously validated
     */
    public SerialSignalHead(String systemName, String userName) {
        super(systemName, userName);
        // Save system Name
        tSystemName = systemName;
        // Extract the Bit from the name
        tBit = SerialAddress.getBitFromSystemName(systemName);
    }

    /**
     * Handle a request to change state on layout
     */
    protected void updateOutput() {
        SerialNode tNode = SerialAddress.getNodeFromSystemName(tSystemName);
        if (tNode == null) {
            // node does not exist, ignore call
            log.error("Can't find node for "+tSystemName+", command ignored");
            return;
        }

        // sort out states
        int cmd;
        if (mLit) 
            switch (mAppearance) {
                    case RED:           cmd = 6; break;
                    case FLASHRED:      cmd = 7; break;
                    case YELLOW:        cmd = 2; break;
                    case FLASHYELLOW:   cmd = 3; break;
                    case GREEN:         cmd = 0; break;
                    case FLASHGREEN:    cmd = 1; break;
                    case DARK:          cmd = 4; break;
                    default:
                        log.warn("Unexpected new appearance: "+mAppearance);
                        cmd = 7; break;  // flash red for error
            }
        else 
            cmd = 4; // set dark if not lit
            
        SerialMessage m = new SerialMessage();
        m.setElement(0,tNode.getNodeAddress()|0x80);  // address 1
        m.setElement(1, (tBit<<3)| cmd);  // 
        m.setElement(2,tNode.getNodeAddress()|0x80);  // address 2
        m.setElement(3, 0<<4); // bank zero
        m.setParity();
        SerialTrafficController.instance().sendSerialMessage(m, null);
    }
        
    public void dispose() {}  // no connections need to be broken

    // flashing is done on the cards, so we don't have to
    // do it manually
    public void startFlash() {}
    public void stopFlash() {}
    
    // data members
    String tSystemName; // System Name of this turnout
    int tBit;          // bit number of turnout control in Serial node

    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(SerialSignalHead.class.getName());
}

/* @(#)SerialSignalHead.java */
