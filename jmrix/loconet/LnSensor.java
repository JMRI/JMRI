/** 
 * LnSensor.java
 *
 * Description:		extend jmri.AbstractSensor for LocoNet layouts
 * @author			Bob Jacobsen Copyright (C) 2001
 * @version			
 */

package jmri.jmrix.loconet;

import jmri.AbstractSensor;

public class LnSensor extends AbstractSensor implements LocoNetListener {

	public LnSensor(int number) {  // a human-readable sensor number must be specified!
		_number = number;
		// At construction, register for messages
		LnTrafficController.instance().addLocoNetListener(~0, this);	
	}

	public int getNumber() { return _number; }
	
	// Handle a request to change state by sending a LocoNet command
	protected void forwardCommandChangeToLayout(int s) throws jmri.JmriException {
		// send SWREQ for close
		LocoNetMessage l = new LocoNetMessage(4);
		l.setOpCode(LnConstants.OPC_SW_REQ);
		
		// compute address fields
		int hiadr = (_number-1)/128;
		int loadr = (_number-1)-hiadr*128;
					
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
	        case LnConstants.OPC_SW_REP: {               /* page 9 of Loconet PE */
            	int sw1 = l.getElement(1);
            	int sw2 = l.getElement(2);
				if (myAddress(sw1, sw2)) {
					if (log.isDebugEnabled()) log.debug("SW_REP received with valid address");
					// see if its a sensor state report
    	        	if ((sw2 & LnConstants.OPC_SW_REP_INPUTS)==0) {
    	        		// sort out states
    	        		switch (sw2 & 
    	        			(LnConstants.OPC_SW_REP_CLOSED|LnConstants.OPC_SW_REP_THROWN)) {
    	        			
    	        			case LnConstants.OPC_SW_REP_CLOSED:	
    	        				setKnownState(ACTIVE);
    	        				break;
    	        			case LnConstants.OPC_SW_REP_THROWN:	
    	        				setKnownState(INACTIVE);
    	        				break;
    	        			case LnConstants.OPC_SW_REP_CLOSED|LnConstants.OPC_SW_REP_THROWN:	
    	        				setKnownState(ACTIVE+INACTIVE);
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
		
	// data members
	int _number;   // loconet sensor number
	
	private boolean myAddress(int a1, int a2) { 
		// the "+ 1" in the following converts to throttle-visible numbering
		return (((a2 & 0x0f) * 128) + (a1 & 0x7f) + 1) == _number; 
		}
	static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(LnSensor.class.getName());

}


/* @(#)LnSensor.java */
