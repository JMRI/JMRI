// EliteXNetInitilizationManager.java

package jmri.jmrix.lenz.hornbyelite;
import jmri.jmrix.lenz.AbstractXNetInitilizationManager;
import jmri.jmrix.lenz.XNetSystemConnectionMemo;

/**
 * This class performs Command Station dependant initilization for 
 * The Hornby Elite.  
 * It adds the appropriate Managers via the Initilization Manager
 * based on the Command Station Type.
 *
 * @author			Paul Bender  Copyright (C) 2003,2008
 * @version			$Revision$
 */
public class EliteXNetInitilizationManager extends AbstractXNetInitilizationManager{

    public EliteXNetInitilizationManager(XNetSystemConnectionMemo memo){
      super(memo);
    }


    protected void init() {
	if(log.isDebugEnabled()) log.debug("Init called");
        // float CSSoftwareVersion
		systemMemo.getXNetTrafficController()
                                       .getCommandStation()
                                       .getCommandStationSoftwareVersion();
        // int
		systemMemo.getXNetTrafficController()
                                          .getCommandStation()
                                          .getCommandStationType();

        /* First, we load things that should work on all systems */
        jmri.InstanceManager.setPowerManager(systemMemo.getPowerManager());
        systemMemo.setThrottleManager(new jmri.jmrix.lenz.hornbyelite.EliteXNetThrottleManager(systemMemo));
        jmri.InstanceManager.setThrottleManager(systemMemo.getThrottleManager());
            
	if (log.isDebugEnabled()) log.debug("Command Station is Hornby Elite (manually identified).");
        systemMemo.setTurnoutManager(new jmri.jmrix.lenz.hornbyelite.EliteXNetTurnoutManager(systemMemo.getXNetTrafficController(),systemMemo.getSystemPrefix()));
        jmri.InstanceManager.setTurnoutManager(systemMemo.getTurnoutManager());
        
        systemMemo.setCommandStation(systemMemo.getXNetTrafficController().getCommandStation());
        jmri.InstanceManager.setCommandStation(systemMemo.getCommandStation());
        
        systemMemo.setLightManager(new jmri.jmrix.lenz.XNetLightManager(systemMemo.getXNetTrafficController(),systemMemo.getSystemPrefix()));
        jmri.InstanceManager.setLightManager(systemMemo.getLightManager());
        systemMemo.setProgrammerManager(new jmri.jmrix.lenz.XNetProgrammerManager(new jmri.jmrix.lenz.hornbyelite.EliteXNetProgrammer(systemMemo.getXNetTrafficController()),systemMemo));
        jmri.InstanceManager.setProgrammerManager(systemMemo.getProgrammerManager());

	if(log.isDebugEnabled()) log.debug("XPressNet Initilization Complete");
    }

    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(EliteXNetInitilizationManager.class.getName());

}
