/** 
 * LnSensor.java
 *
 * Description:		extend jmri.AbstractSensor for LocoNet layouts
 * @author			Bob Jacobsen Copyright (C) 2001
 * @version			
 */

package jmri.jmrix.loconet;

import jmri.AbstractSensor;
import jmri.Sensor;

public class LnSensor extends AbstractSensor implements LocoNetListener {

	private LnSensorAddress a;
	
	public LnSensor(String id) {  // a human-readable sensor number must be specified!
		super(id);

		// store address forms
		a = new LnSensorAddress(id);

		// At construction, register for messages
		LnTrafficController.instance().addLocoNetListener(~0, this);	
	}

	public int getNumber() { return _number; }
	
	// request an update on status by sending a loconet message
	public void requestUpdateFromLayout() {
		// the only known way to do this from LocoNet is to request the
		// status of _all_ devices, which is here considered too 
		// heavyweight.  Perhaps this is telling us we need
		// a "update all" in the SensorManager (and/or TurnoutManager)
		// interface?
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
	        case LnConstants.OPC_INPUT_REP: {               /* page 9 of Loconet PE */
            	int sw1 = l.getElement(1);
            	int sw2 = l.getElement(2);
				if (a.matchAddress(sw1, sw2)) {
					if (log.isDebugEnabled()) log.debug("INPUT_REP received with valid address");
					// save the state
					int state = sw2 & 0x20;
					if ( state !=0 && getKnownState() != Sensor.ACTIVE) {
						setKnownState(Sensor.ACTIVE);
					} else if ( state ==0 && getKnownState() != Sensor.INACTIVE) {
						setKnownState(Sensor.INACTIVE);
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
			
	static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(LnSensor.class.getName());

}


/* @(#)LnSensor.java */
