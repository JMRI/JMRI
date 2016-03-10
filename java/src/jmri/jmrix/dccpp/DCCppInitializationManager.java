// DCCppInitializationManager.java
package jmri.jmrix.dccpp;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class performs Command Station dependant initialization for DCC++. It
 * adds the appropriate Managers via the Initialization Manager based on the
 * Command Station Type.
 *
 * @author	Paul Bender Copyright (C) 2003-2010
 * @author	Giorgio Terdina Copyright (C) 2007
 * @author      Mark Underwood Copyright (C) 2015
 * @version	$Revision$
 *
 * Based on XNetInitializationManager by Paul Bender and Giorgio Terdina
 */
public class DCCppInitializationManager extends AbstractDCCppInitializationManager {

    public DCCppInitializationManager(DCCppSystemConnectionMemo memo) {
        super(memo);
    }

    protected void init() {
        if (log.isDebugEnabled()) {
            log.debug("Init called");
        }

	String base_station = "Unknown";
	String code_build = "Unknown";

	if (systemMemo.getDCCppTrafficController().getCommandStation() != null) {
	    base_station = systemMemo.getDCCppTrafficController().getCommandStation().getBaseStationType();
	}
	if (systemMemo.getDCCppTrafficController().getCommandStation() != null) {
	    code_build = systemMemo.getDCCppTrafficController().getCommandStation().getCodeBuildDate();
	}
	/* First, we load things that should work on all systems */
	if (systemMemo.getPowerManager() == null) {
	    log.error("Power Manager not (yet) created!");
	}
	jmri.InstanceManager.setPowerManager(systemMemo.getPowerManager());
	if (jmri.InstanceManager.powerManagerInstance() == null) {
	    log.error("Power Manager not accessible!");
	} else {
	    log.debug("Power Manager: {}", jmri.InstanceManager.powerManagerInstance());
	}
	jmri.InstanceManager.setThrottleManager(systemMemo.getThrottleManager());
	/* Next we check the command station type, and add the 
	   apropriate managers */
	/* If we still don't  know what we have, load everything */
	if (log.isDebugEnabled()) {
	    log.debug("Command Station is type {} build {}", base_station, code_build);
	}
	systemMemo.setProgrammerManager(new DCCppProgrammerManager(new DCCppProgrammer(systemMemo.getDCCppTrafficController()), systemMemo));
	jmri.InstanceManager.setProgrammerManager(systemMemo.getProgrammerManager());
	systemMemo.setCommandStation(systemMemo.getDCCppTrafficController().getCommandStation());
	jmri.InstanceManager.setCommandStation(systemMemo.getCommandStation());
	/* the consist manager has to be set up AFTER the programmer, to 
	   prevent the default consist manager from being loaded on top of it */
	//systemMemo.setConsistManager(new jmri.jmrix.dccpp.DCCppConsistManager(systemMemo));
	//jmri.InstanceManager.setConsistManager(systemMemo.getConsistManager());
	systemMemo.setTurnoutManager(new jmri.jmrix.dccpp.DCCppTurnoutManager(systemMemo.getDCCppTrafficController(), systemMemo.getSystemPrefix()));
	jmri.InstanceManager.setTurnoutManager(systemMemo.getTurnoutManager());
	systemMemo.setLightManager(new jmri.jmrix.dccpp.DCCppLightManager(systemMemo.getDCCppTrafficController(), systemMemo.getSystemPrefix()));
	jmri.InstanceManager.setLightManager(systemMemo.getLightManager());
	systemMemo.setSensorManager(new jmri.jmrix.dccpp.DCCppSensorManager(systemMemo.getDCCppTrafficController(), systemMemo.getSystemPrefix()));
	jmri.InstanceManager.setSensorManager(systemMemo.getSensorManager());
	systemMemo.setMultiMeter(new DCCppMultiMeter(systemMemo));
	jmri.InstanceManager.store(systemMemo.getMultiMeter(), jmri.MultiMeter.class);
	

        if (log.isDebugEnabled()) {
            log.debug("DCC++ Initialization Complete");
        }
    }
    
    private final static Logger log = LoggerFactory.getLogger(DCCppInitializationManager.class.getName());

}
