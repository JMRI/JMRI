/** 
 * LnTurnout.java
 *
 * Description:		extend jmri.AbstractTurnout for LocoNet layouts
 * @author			Bob Jacobsen Copyright (C) 2001
 * @version			
 */

package jmri.jmrix.loconet;

import jmri.AbstractTurnout;

public class LnTurnout extends AbstractTurnout implements LocoNetListener {

	public LnTurnout(int number) {  // a human-readable turnout number must be specified!
		_number = number;
		// At construction, register for messages
		LnTrafficController.instance().addLocoNetListener(~0, this);	
	}

	public int getNumber() { return _number; }
	public String getSystemName() { return "LT"+getNumber(); }
	
	// Handle a request to change state by sending a LocoNet command
	protected void forwardCommandChangeToLayout(int s) throws jmri.JmriException {
		// send SWREQ for close
		LocoNetMessage l = new LocoNetMessage(4);
		l.setOpCode(LnConstants.OPC_SW_REQ);
		
		// compute address fields
		int hiadr = (_number-1)/128;
		int loadr = (_number-1)-hiadr*128;
		
		// set closed (note that this can't handle both!  Not sure how to
		// say that in LocoNet.
		if ((s & CLOSED) != 0) {
			hiadr |= 0x20;
			// thrown exception if also THROWN
			if ((s & THROWN) != 0)
				throw new jmri.JmriException("LocoNet turnout logic can't handle both THROWN and CLOSED yet");
			}
			
		// load On/Off with on
		hiadr |= 0x10;
		
		// store and send
		l.setElement(1,loadr);
		l.setElement(2,hiadr);
		LnTrafficController.instance().sendLocoNetMessage(l);
	}
	
	// implementing classes will typically have a function/listener to get
	// updates from the layout, which will then call 
	//		public void firePropertyChange(String propertyName,
	//										Object oldValue,
	//										Object newValue)	 
	// _once_ if anything has changed state (or set the commanded state directly)
	public void message(LocoNetMessage l) {
		// parse message type
		switch (l.getOpCode()) {
        	case LnConstants.OPC_SW_REQ: {               /* page 9 of Loconet PE */
	            int sw1 = l.getElement(1);
	            int sw2 = l.getElement(2);
				if (myAddress(sw1, sw2)) {
					if (log.isDebugEnabled()) log.debug("SW_REQ received with valid address");
					if ((sw2 & LnConstants.OPC_SW_REQ_DIR)!=0)
						newCommandedState(CLOSED);
					else
						newCommandedState(THROWN);
				}
				break;
				}
	        case LnConstants.OPC_SW_REP: {               /* page 9 of Loconet PE */
            	int sw1 = l.getElement(1);
            	int sw2 = l.getElement(2);
				if (myAddress(sw1, sw2)) {
					if (log.isDebugEnabled()) log.debug("SW_REP received with valid address");
					// see if its a turnout state report
    	        	if ((sw2 & LnConstants.OPC_SW_REP_INPUTS)==0) {
    	        		// sort out states
    	        		switch (sw2 & 
    	        			(LnConstants.OPC_SW_REP_CLOSED|LnConstants.OPC_SW_REP_THROWN)) {
    	        			
    	        			case LnConstants.OPC_SW_REP_CLOSED:	
    	        				setKnownState(CLOSED);
    	        				break;
    	        			case LnConstants.OPC_SW_REP_THROWN:	
    	        				setKnownState(THROWN);
    	        				break;
    	        			case LnConstants.OPC_SW_REP_CLOSED|LnConstants.OPC_SW_REP_THROWN:	
    	        				setKnownState(CLOSED+THROWN);
    	        				break;
    	        			default:	
    	        				setKnownState(0);
    	        				break;
							}
    	        		}
					}
				}
			default:
				return;
			}
		// reach here only in error
	}
	
	public void dispose() {
		LnTrafficController.instance().removeLocoNetListener(~0, this);
	}
		
	// data members
	int _number;   // loconet turnout number
	
	private boolean myAddress(int a1, int a2) { 
		// the "+ 1" in the following converts to throttle-visible numbering
		return (((a2 & 0x0f) * 128) + (a1 & 0x7f) + 1) == _number; 
		}
	static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(LnTurnout.class.getName());

}


/* @(#)LnTurnout.java */
