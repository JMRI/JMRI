/**
 * XNetTurnout.java
 *
 * Description:		extend jmri.AbstractTurnout for XNet layouts
 * @author			Bob Jacobsen Copyright (C) 2001
 * @version			$Revision: 1.4 $
 */

package jmri.jmrix.lenz;

import jmri.AbstractTurnout;

public class XNetTurnout extends AbstractTurnout implements XNetListener {
    
    public XNetTurnout(int pNumber) {  // a human-readable turnout number must be specified!
        mNumber = pNumber;
        // At construction, register for messages
        XNetTrafficController.instance().addXNetListener(~0, this);
    }
    
    public int getNumber() { return mNumber; }
    public String getSystemName() { return "XT"+getNumber(); }
    
    // Handle a request to change state by sending a LocoNet command
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
        if (XNetTrafficController.instance()
            .getCommandStation()
            .getTurnoutMsgAddr(l) != mNumber) return;
        // is for this object, parse message type
        log.error("message function invoked, but not yet prepared");
    }
    
    public void dispose() {
        XNetTrafficController.instance().removeXNetListener(~0, this);
    }
    
    // data members
    int mNumber;   // loconet turnout number
    
    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(XNetTurnout.class.getName());
    
}


/* @(#)XNetTurnout.java */
