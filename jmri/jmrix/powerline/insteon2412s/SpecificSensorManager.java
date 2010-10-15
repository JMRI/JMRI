// SpecificSensorManager.java

package jmri.jmrix.powerline.insteon2412s;

import jmri.Sensor;
import jmri.jmrix.powerline.X10Sequence;
import jmri.jmrix.powerline.SerialReply;
import jmri.jmrix.powerline.SerialAddress;
import jmri.jmrix.powerline.insteon2412s.Constants;
import java.util.List;

/**
 * Manage the system-specific Sensor implementation.
 * <P>
 * System names are "PSann", where a is the unit id, nn is the unit number without padding.
 * <P>
 * Sensors are numbered from 1.
 * <P>
 * @author			Bob Jacobsen Copyright (C) 2003, 2006, 2007, 2008, 2009
 * @author			Ken Cameron, (C) 2009, 2010 sensors from poll replies
 * @version			$Revision: 1.3 $
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
	    if ((l.getElement(0)& 0xFF) == Constants.POLL_REQ_X10 ) { 
	        // must be received data
	        int last = (l.getElement(1)& 0xFF) + 1;
	        int bits = (l.getElement(2)& 0xFF);
        	String newHouseCode = null;
        	int newCmdCode = -1;
        	int newAddrCode = -1;
        	Sensor sensor = null;
	        for (int i = 3; i <= last; i++) {
	        	int dat = l.getElement(i) & 0xFF;
	            if ((bits & 0x01) != 0) {
	            	newHouseCode = X10Sequence.houseValueToText(X10Sequence.decode((dat >> 4) & 0x0F));
	            	newCmdCode = dat & 0x0f;
            		if (newHouseCode != null && (newCmdCode == X10Sequence.FUNCTION_ALL_LIGHTS_OFF || newCmdCode == X10Sequence.FUNCTION_ALL_UNITS_OFF || newCmdCode == X10Sequence.FUNCTION_ALL_LIGHTS_ON)) {
            			// some sort of 'global' command, process for all matching the house code
            			List<String> sensors = getSystemNameList();
            			for (int ii = 0; ii < sensors.size(); ii++) {
            				String sName = sensors.get(ii);
            				if (newHouseCode.compareTo(SerialAddress.houseCodeFromSystemName(sName)) == 0) {
                				sensor = provideSensor(sName);
            					if (sensor != null) {
            						try {
            							if (newCmdCode == X10Sequence.FUNCTION_ALL_LIGHTS_OFF || newCmdCode == X10Sequence.FUNCTION_ALL_UNITS_OFF) {
            								sensor.setKnownState(Sensor.INACTIVE);
            							} else {
            								sensor.setKnownState(Sensor.ACTIVE);
            							}
            						} catch (jmri.JmriException e) {
            							if (newCmdCode == X10Sequence.FUNCTION_ALL_LIGHTS_OFF || newCmdCode == X10Sequence.FUNCTION_ALL_UNITS_OFF) {
            								log.error("Exception setting " + sName + " sensor INACTIVE: " + e);
            							} else {
            								log.error("Exception setting " + sName + " sensor ACTIVE: " + e);
            							}
		            				}
            					}
            				}
            			}
            		} else {
    	            	if (newCmdCode != -1 && newHouseCode != null && newAddrCode > 0) {
		            		String sysName = getSystemPrefix() + "S" + newHouseCode + newAddrCode;
		            		sensor = provideSensor(sysName);
		            		if (sensor != null) {
		            			if (newCmdCode == X10Sequence.FUNCTION_ON || newCmdCode == X10Sequence.FUNCTION_BRIGHT || newCmdCode == X10Sequence.FUNCTION_STATUS_ON) {
		            				try {				
		            					sensor.setKnownState(Sensor.ACTIVE);
		            				} catch (jmri.JmriException e) {
		            					log.error("Exception setting " + sysName + " sensor ACTIVE: " + e);
		            				}
		            			}
		            			if (newCmdCode == X10Sequence.FUNCTION_OFF || newCmdCode == X10Sequence.FUNCTION_DIM || newCmdCode == X10Sequence.FUNCTION_STATUS_OFF) {
		            				try {				
		            					sensor.setKnownState(Sensor.INACTIVE);
		            				} catch (jmri.JmriException e) {
		            					log.error("Exception setting " + sysName + " sensor INACTIVE: " + e);
		            				}
		            			}
		            		}
	            		}
	            	}
	            	newHouseCode = null;
	            	newCmdCode = -1;
	            	newAddrCode = -1;
	            } else {
	            	newHouseCode = X10Sequence.houseValueToText(X10Sequence.decode((dat >> 4) & 0x0F));
	            	newAddrCode = X10Sequence.decode(dat & 0x0f);
	            }
                bits = bits >> 1;  // shift over before next byte
	        }
	    }
    }
    
    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(SpecificSensorManager.class.getName());
}

/* @(#)SpecificSensorManager.java */
