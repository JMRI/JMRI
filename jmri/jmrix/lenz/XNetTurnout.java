/**
 * XNetTurnout.java
 *
 * Description:		extend jmri.AbstractTurnout for XNet layouts
 * @author			Bob Jacobsen Copyright (C) 2001
 * @version			$Revision: 1.6 $
 */

package jmri.jmrix.lenz;

import jmri.AbstractTurnout;

public class XNetTurnout extends AbstractTurnout implements XNetListener {

    public XNetTurnout(int pNumber) {  // a human-readable turnout number must be specified!
        super("XT"+pNumber);
        mNumber = pNumber;
        // At construction, register for messages
        XNetTrafficController.instance().addXNetListener(~0, this);
    }

    public int getNumber() { return mNumber; }

    // Handle a request to change state by sending an XPressNet command
    protected void forwardCommandChangeToLayout(int s) {
        // find the command station
        LenzCommandStation cs = XNetTrafficController.instance().getCommandStation();
        // get the right packet
        XNetMessage msg = cs.getTurnoutCommandMsg(mNumber,
                                                  (s & CLOSED)!=0,
                                                  (s & THROWN)!=0,
                                                  true );

        XNetTrafficController.instance().sendXNetMessage(msg, this);
    }

    // implementing classes will typically have a function/listener to get
    // updates from the layout, which will then call
    //		public void firePropertyChange(String propertyName,
    //										Object oldValue,
    //										Object newValue)
    // _once_ if anything has changed state (or set the commanded state directly)
    public void message(XNetMessage l) {
        // check validity & addressing
        // if this is an ODD numbered turnout, then we always get the 
        // right response from .getTurnoutMsgAddr.  If this is an even 
        // numbered turnout, we need to check the messages for the odd 
        // numbered turnout in the nibble as well.
        if ( XNetTrafficController.instance()
            .getCommandStation()
            .getTurnoutMsgAddr(l) == mNumber) {
            // is for this object, parse message type
            if (log.isDebugEnabled()) log.debug("Message for turnout" + mNumber);
            int turnout_type=l.getElement(2) & 0x60;
            if(turnout_type==0x00 || turnout_type==0x20) {
		/* this is a turnout response */
                if((l.getElement(2) & 0x03)!=0) {
                    /* this is for the lower half of the nibble */
                    int state=l.getElement(2) & 0x03;
                    XNetMessage msg =  XNetTrafficController.instance()
                                                .getCommandStation()
						.getTurnoutCommandMsg(mNumber,
                                                  (state & CLOSED)!=0,
                                                  (state & THROWN)!=0,
                                                  false );                    
                // XNetTrafficController.instance().sendXNetMessage(msg, this);
                }
             }    
            } else if ( ((mNumber%2)==0) && 
                        (XNetTrafficController.instance()
                                   .getCommandStation()
                                   .getTurnoutMsgAddr(l) == mNumber-1)) {
            // is for this object, parse message type
            if (log.isDebugEnabled()) log.debug("Message for turnout" + mNumber);
            int turnout_type=l.getElement(2) & 0x60;
            if(turnout_type==0x00 || turnout_type==0x20)
            {
		/* this is a turnout response */
                if((l.getElement(2) & 0x03)!=0) {
                    /* this is for the upper half of the nibble */
                int state=l.getElement(2) & 0x0C;
                XNetMessage msg =  XNetTrafficController.instance()
                                                .getCommandStation()
						.getTurnoutCommandMsg(mNumber,
                                                  (state & CLOSED)!=0,
                                                  (state & THROWN)!=0,
                                                  false );
                //XNetTrafficController.instance().sendXNetMessage(msg, this);
                }                 
            }
	} else { return; }
    }

    public void dispose() {
        XNetTrafficController.instance().removeXNetListener(~0, this);
    }

    // data members
    int mNumber;   // loconet turnout number

    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(XNetTurnout.class.getName());

}


/* @(#)XNetTurnout.java */
