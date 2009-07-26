// SpecificSensorManager.java

package jmri.jmrix.powerline.cm11;

import jmri.Sensor;
import jmri.InstanceManager;
import jmri.jmrix.powerline.X10Sequence;
import jmri.jmrix.powerline.SerialReply;
import jmri.jmrix.powerline.cm11.Constants;

/**
 * Manage the system-specific Sensor implementation.
 * <P>
 * System names are "PSann", where a is the unit id, nn is the unit number without padding.
 * <P>
 * Sensors are numbered from 1.
 * <P>
 * @author			Bob Jacobsen Copyright (C) 2003, 2006, 2007, 2008
 * @author			Ken Cameron, (C) 2009, sensors from poll replies
 * @version			$Revision: 1.2 $
 */
public class SpecificSensorManager extends jmri.jmrix.powerline.SerialSensorManager {

    public SpecificSensorManager() {
        super();
    }
    
    /**
     *  Process a reply to a poll of Sensors of one node
     */
    public synchronized void reply(SerialReply r) {
        // process for updates
    	processForPollReq(r);
    }
    
    private void processForPollReq(SerialReply l) {
        // process the POLL_REQ and update/create sensors as needed
	    if ((l.getElement(0)& 0xFF) == Constants.POLL_REQ ) { 
	        // must be received data
	        int last = (l.getElement(1)& 0xFF) + 1;
	        int bits = (l.getElement(2)& 0xFF);
        	String newHouseCode = null;
        	String newCmdCode = null;
        	int newAddrCode = 0;
        	Sensor sensor = null;
	        for (int i = 3; i <= last; i++) {
	            if ((bits & 0x01) != 0) {
	                newCmdCode = X10Sequence.formatCommandByte(l.getElement(i) & 0xFF);
	            } else {
	            	int b = l.getElement(i) & 0xFF;
	            	newHouseCode = X10Sequence.houseValueToText(X10Sequence.decode((b >> 4) & 0x0F));
	            	newAddrCode = X10Sequence.decode(b & 0x0f);
	            	if (newCmdCode != null && newHouseCode != null && newAddrCode != 0) {
	            		String sysName = InstanceManager.sensorManagerInstance().systemLetter() + "S" + newHouseCode + newAddrCode;
	            		sensor = InstanceManager.sensorManagerInstance().provideSensor(sysName);
	            		// see if sensor exists
	            		if (sensor == null) {
	                    	sensor = InstanceManager.sensorManagerInstance().newSensor(sysName.toUpperCase(), null); 
	            		}
	            	}
	            	newHouseCode = null;
	            	newCmdCode = null;
	            	newAddrCode = 0;
	            }
                bits = bits >> 1;  // shift over before next byte
	        }
	    }
    }
    
    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(SpecificSensorManager.class.getName());
}

/* @(#)SpecificSensorManager.java */
