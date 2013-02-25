// SerialTurnout.java

package jmri.jmrix.grapevine;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jmri.implementation.AbstractTurnout;
import jmri.Turnout;

/**
 *  Implement Turnout for Grapevine.
 * 
 *  This object doesn't listen to the Grapevine serial communications.  This is because
 *  it should be the only object that is sending messages for this turnout;
 *  more than one Turnout object pointing to a single device is not allowed.
 *
 * @author			Bob Jacobsen Copyright (C) 2003, 2006, 2007, 2008
 * @version			$Revision$
 */
public class SerialTurnout extends AbstractTurnout {

    /**
     * Create a Turnout object, with both system and user names.
     * <P>
     * 'systemName' was previously validated in SerialTurnoutManager
     */
    public SerialTurnout(String systemName, String userName) {
        super(systemName, userName);
        // Save system Name
        tSystemName = systemName;
        // Extract the Bit from the name
        int num = SerialAddress.getBitFromSystemName(systemName); // bit one is address zero
        // num is 101-124, 201-224, 301-324, 401-424
        output = (num%100)-1; // 0-23
        bank = (num/100)-1;  // 0 - 3
    }

    /**
     * Grapevine turnouts can invert their outputs
     */
    public boolean canInvert(){return true;}
    
    /**
     * Handle a request to change state by sending a turnout command
     */
    protected void forwardCommandChangeToLayout(int s) {
        // implementing classes will typically have a function/listener to get
        // updates from the layout, which will then call
        //		public void firePropertyChange(String propertyName,
        //				                Object oldValue,
        //						Object newValue)
        // _once_ if anything has changed state (or set the commanded state directly)

        // sort out states
        if ( (s & Turnout.CLOSED) > 0) {
            // first look for the double case, which we can't handle
            if ( (s & Turnout.THROWN) > 0) {
                // this is the disaster case!
                log.error("Cannot command both CLOSED and THROWN "+s);
                return;
            } else {
                // send a CLOSED command
                sendMessage(true^getInverted());
            }
        } else {
            // send a THROWN command
            sendMessage(false^getInverted());
        }
    }
    
    protected void turnoutPushbuttonLockout(boolean _pushButtonLockout){
		if (log.isDebugEnabled()) log.debug("Send command to " + (_pushButtonLockout ? "Lock" : "Unlock")+ " Pushbutton");
    }

    // data members
    String tSystemName; // System Name of this turnout
    int output;         // output connector number, 0-23
    int bank;           // bank number, 0-3

    protected void sendMessage(boolean closed) {
        SerialNode tNode = SerialAddress.getNodeFromSystemName(tSystemName);
        if (tNode == null) {
            // node does not exist, ignore call
            log.error("Can't find node for "+tSystemName+", command ignored");
            return;
        }
        boolean high = (output>=12);
        int tOut = output;
        if (high) tOut = output-12;
        if ( (bank<0)||(bank>4) ) {
            log.error("invalid bank "+bank+" for Turnout "+getSystemName());
            bank = 0;
        }

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
        m.setElement(i++, (tOut<<3)|(closed ? 0 : 6));  // closed is green, thrown is red
        m.setElement(i++,tNode.getNodeAddress()|0x80);  // address 2
        m.setElement(i++, bank<<4); // bank is most significant bits
        m.setParity(i-4);
        SerialTrafficController.instance().sendSerialMessage(m, null);
    }

    static Logger log = LoggerFactory.getLogger(SerialTurnout.class.getName());
}

/* @(#)SerialTurnout.java */
