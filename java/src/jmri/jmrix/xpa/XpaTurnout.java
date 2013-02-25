// XpaTurnout.java

package jmri.jmrix.xpa;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jmri.implementation.AbstractTurnout;
import jmri.Turnout;

/**
 * Xpa+Modem implementation of the Turnout interface.
 * <P>
 *
 * @author	Paul Bender Copyright (C) 2004
 * @version	$Revision$
 */
public class XpaTurnout extends AbstractTurnout {

    // Private data member to keep track of what turnout we control.
    int _number;

    /**
     * Xpa turnouts use any addres allowed as an accessory decoder address 
     * on the particular command station.
     */
    public XpaTurnout(int number) {
        super("PT"+number);
        _number = number;
    }

    public int getNumber() { return _number; }

    // Handle a request to change state by sending a formatted DCC packet
    protected void forwardCommandChangeToLayout(int s) {
	XpaMessage m=null;
        // sort out states
        if ( (s & Turnout.CLOSED) > 0) {
            // first look for the double case, which we can't handle
            if ( (s & Turnout.THROWN) > 0) {
                // this is the disaster case!
                log.error("Cannot command both CLOSED and THROWN "+s);
                return;
            } else {
                // send a CLOSED command
		m=XpaMessage.getSwitchNormalMsg(_number);
            }
        } else {
            // send a THROWN command
            m=XpaMessage.getSwitchReverseMsg(_number);
        }
	if(m!=null) {
	   XpaTrafficController.instance().sendXpaMessage(m, null);
	}

    }
    protected void turnoutPushbuttonLockout(boolean _pushButtonLockout){
		if (log.isDebugEnabled()) log.debug("Send command to " + (_pushButtonLockout ? "Lock" : "Unlock")+ " Pushbutton PT"+_number);
    }

    static Logger log = LoggerFactory.getLogger(XpaTurnout.class.getName());

}

/* @(#)XpaTurnout.java */
