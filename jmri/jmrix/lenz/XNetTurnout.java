/**
 * XNetTurnout.java
 *
 * Description:		extend jmri.AbstractTurnout for XNet layouts
 * @author			Bob Jacobsen Copyright (C) 2001
 * @version			$Revision: 1.1 $
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
	protected void forwardCommandChangeToLayout(int s) throws jmri.JmriException {
        // find the command station
        LenzCommandStation cs = XNetTrafficController.instance().getCommandStation();
        // get the right packet
        XNetMessage msg = cs.getTurnoutCommandMsg(mNumber,
                                                    (s & CLOSED)!=0,
                                                    (s & THROWN)!=0,
                                                    true );

		XNetTrafficController.instance().sendXNetMessage(msg);
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
		switch (l.getOpCode()) {
        	case jmri.jmrix.loconet.LnConstants.OPC_SW_REQ: {               /* page 9 of Loconet PE */
	            int sw2 = l.getElement(2);
                if (log.isDebugEnabled()) log.debug("SW_REQ received with valid address");
                if ((sw2 & jmri.jmrix.loconet.LnConstants.OPC_SW_REQ_DIR)!=0)
                	newCommandedState(CLOSED);
                else
                	newCommandedState(THROWN);
                break;
            }
	        case jmri.jmrix.loconet.LnConstants.OPC_SW_REP: {               /* page 9 of Loconet PE */
            	int sw2 = l.getElement(2);
                if (log.isDebugEnabled()) log.debug("SW_REP received with valid address");
                // see if its a turnout state report
                if ((sw2 & jmri.jmrix.loconet.LnConstants.OPC_SW_REP_INPUTS)==0) {
                	// sort out states
                	switch (sw2 &
                		(jmri.jmrix.loconet.LnConstants.OPC_SW_REP_CLOSED|jmri.jmrix.loconet.LnConstants.OPC_SW_REP_THROWN)) {

                        case jmri.jmrix.loconet.LnConstants.OPC_SW_REP_CLOSED:
                            setKnownState(CLOSED);
                            break;
                        case jmri.jmrix.loconet.LnConstants.OPC_SW_REP_THROWN:
                            setKnownState(THROWN);
                            break;
                        case jmri.jmrix.loconet.LnConstants.OPC_SW_REP_CLOSED|jmri.jmrix.loconet.LnConstants.OPC_SW_REP_THROWN:
                            setKnownState(CLOSED+THROWN);
                            break;
                        default:
                        	setKnownState(0);
                        	break;
                        }
                    }
				}
			default:
				return;
			}
		// reach here only in error
	}

	public void dispose() {
		XNetTrafficController.instance().removeXNetListener(~0, this);
	}

	// data members
	int mNumber;   // loconet turnout number

	static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(XNetTurnout.class.getName());

}


/* @(#)XNetTurnout.java */
