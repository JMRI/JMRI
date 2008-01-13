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
 * @version			$Revision: 1.2 $
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

        int output = (tBit-1) % 24; /// 0 to 23 range for individual bank
        boolean high = (output>=12);
        if (high) output = output-12;
        int bank = (tBit-1)/24;  
        if ( (bank<0)||(bank>4) ) {
            log.error("invalid bank "+bank+" for signal "+getSystemName());
            bank = 0;
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
            


        SerialMessage m = new SerialMessage(high?8:4);
        int i = 0;
        if (high) {
            m.setElement(i++,tNode.getNodeAddress()|0x80);  // address 1
            m.setElement(i++,122);   // shift command
            m.setElement(i++,tNode.getNodeAddress()|0x80);  // address 2
            m.setElement(i++,0x10);  // bank 1
            m.setParity(i-4);
        }
        m.setElement(i++,tNode.getNodeAddress()|0x80);  // address 1
        m.setElement(i++, (output<<3)|cmd);
        m.setElement(i++,tNode.getNodeAddress()|0x80);  // address 2
        m.setElement(i++, bank<<4); // bank is most significant bits
        m.setParity(i-4);
        SerialTrafficController.instance().sendSerialMessage(m, null);
    }
        
    public void dispose() {}  // no connections need to be broken

    // flashing is done on the cards, so we don't have to
    // do it manually
    public void startFlash() {}
    public void stopFlash() {}
    
    // data members
    String tSystemName; // System Name of this signal head
    int tBit;          // bit number of head control in Serial node

    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(SerialSignalHead.class.getName());
}

/* @(#)SerialSignalHead.java */
