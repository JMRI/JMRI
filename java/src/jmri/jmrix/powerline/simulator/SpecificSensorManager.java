// SpecificSensorManager.java

package jmri.jmrix.powerline.simulator;

import org.apache.log4j.Logger;
import jmri.Sensor;
import jmri.jmrix.powerline.SerialTrafficController;
import jmri.jmrix.powerline.X10Sequence;
import jmri.jmrix.powerline.SerialReply;
import jmri.jmrix.powerline.simulator.Constants;
import jmri.util.StringUtil;

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
 * Converted to multiple connection
 * @author kcameron Copyright (C) 2011
 * @version			$Revision$
 */
public class SpecificSensorManager extends jmri.jmrix.powerline.SerialSensorManager {

    public SpecificSensorManager(SerialTrafficController tc) {
        super(tc);
        this.tc = tc;
    }
    
    SerialTrafficController tc = null;
    
    /**
     *  Process a reply to a poll of Sensors of one node
     */
    public synchronized void reply(SerialReply r) {
        // process for updates
    	processForPollReq(r);
    }
    
    private void processForPollReq(SerialReply l) {
    	if ((l.getElement(0) & 0xFF) == Constants.HEAD_STX) {
	        // process the POLL_REQ_X10 and update/create sensors as needed
		    if (((l.getElement(1)& 0xFF) == Constants.POLL_REQ_X10) && l.getNumDataElements() == 4) { 
		        // valid poll of X10 message
		    	int dat = l.getElement(2) & 0xFF;
		    	int flag = l.getElement(3) & 0xFF;
	        	String newHouseCode = X10Sequence.houseValueToText(X10Sequence.decode((dat >> 4) & 0x0F));
	        	int newCmdCode = dat & 0x0F;
	        	int newAddrCode = -1;
	        	Sensor sensor = null;
		        if ((flag & Constants.FLAG_BIT_X10_CMDUNIT) == Constants.FLAG_X10_RECV_CMD) {
		        	if ((newCmdCode == X10Sequence.FUNCTION_ALL_LIGHTS_OFF || newCmdCode == X10Sequence.FUNCTION_ALL_UNITS_OFF || newCmdCode == X10Sequence.FUNCTION_ALL_LIGHTS_ON)) {
	        			// some sort of 'global' command, process for all matching the house code
	        			List<String> sensors = getSystemNameList();
	        			for (int ii = 0; ii < sensors.size(); ii++) {
	        				String sName = sensors.get(ii);
	        				if (newHouseCode.compareTo(tc.getAdapterMemo().getSerialAddress().houseCodeFromSystemName(sName)) == 0) {
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
		            	if (newHouseCode != null && newAddrCode > 0) {
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
		        }
		    } else if (((l.getElement(1)& 0xFF) == Constants.POLL_REQ_STD) && l.getNumDataElements() == 11) {
		    	// figure how to decode an standard Insteon poll command
		    	int highAddr = l.getElement(5) & 0xFF;
		    	int middleAddr = l.getElement(6) & 0xFF;
		    	int lowAddr = l.getElement(7) & 0xFF;
		    	int cmd1 = l.getElement(9) & 0xFF;
		    	StringBuilder sysName = new StringBuilder();
        		sysName.append(getSystemPrefix());
        		sysName.append("S");
        		sysName.append(StringUtil.twoHexFromInt(highAddr));
        		sysName.append(".");
        		sysName.append(StringUtil.twoHexFromInt(middleAddr));
        		sysName.append(".");
        		sysName.append(StringUtil.twoHexFromInt(lowAddr));
        		Sensor sensor = null;
        		sensor = provideSensor(new String(sysName));
        		if (sensor != null) {
    		    	if (cmd1 == Constants.CMD_LIGHT_ON_FAST || cmd1 == Constants.CMD_LIGHT_ON_RAMP) {
        				try {				
        					sensor.setKnownState(Sensor.ACTIVE);
        				} catch (jmri.JmriException e) {
        					log.error("Exception setting " + sysName + " sensor ACTIVE: " + e);
        				}
        			}
        			if (cmd1 == Constants.CMD_LIGHT_OFF_FAST || cmd1 == Constants.CMD_LIGHT_OFF_RAMP) {
        				try {				
        					sensor.setKnownState(Sensor.INACTIVE);
        				} catch (jmri.JmriException e) {
        					log.error("Exception setting " + sysName + " sensor INACTIVE: " + e);
        				}
        			}
		    	}
		    }
    	}
    }
    
    static Logger log = Logger.getLogger(SpecificSensorManager.class.getName());
}

/* @(#)SpecificSensorManager.java */
