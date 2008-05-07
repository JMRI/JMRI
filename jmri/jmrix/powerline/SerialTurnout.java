// SerialTurnout.java

package jmri.jmrix.powerline;

import jmri.AbstractTurnout;
import jmri.Turnout;

/**
 *  Turnout implementation for X10.
 *  <p>
 *  This object doesn't listen to the serial communications.  It should
 *  eventually, so it can track changes outside the program.
 * <p>
 *  Within JMRI, only one Turnout object should besending messages to a turnout address;
 *  more than one Turnout object pointing to a single device is not allowed.
 *
 * Description:		extend jmri.AbstractTurnout for powerline serial layouts
 * @author			Bob Jacobsen Copyright (C) 2003, 2006, 2007, 2008
 * @version			$Revision: 1.7 $
 */
public class SerialTurnout extends AbstractTurnout {

    /**
     * Create a Turnout object, with both system and user names.
     * <P>
     * 'systemName' was previously validated in SerialTurnoutManager
     */
    public SerialTurnout(String systemName, String userName) {
        super(systemName, userName);
        // Extract the default 1-256 address from the name
        int tBit = SerialAddress.getBitFromSystemName(systemName);
        // Convert to the two-part X10 address
        housecode = ((tBit-1)/16)+1;
        devicecode = ((tBit-1)%16)+1;
    }

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

    // data members holding the X10 address
    int housecode = -1;
    int devicecode = -1;
    
    protected void sendMessage(boolean closed) {
        log.debug("set closed "+closed+" house "+housecode+" device "+devicecode);
        // create output sequence of address, then function
        X10Sequence out = new X10Sequence();
        out.addAddress(housecode, devicecode);
        out.addFunction(housecode, (closed ? X10Sequence.FUNCTION_OFF : X10Sequence.FUNCTION_ON), 0);
        // send
        SerialTrafficController.instance().sendX10Sequence(out, null);
    }

    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(SerialTurnout.class.getName());
}

/* @(#)SerialTurnout.java */
