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
 * @version			$Revision: 1.8 $
 */
public class EliteXNetInitilizationManager extends AbstractXNetInitilizationManager{

    public EliteXNetInitilizationManager(XNetSystemConnectionMemo memo){
      super(memo);
    }


    protected void init() {
	if(log.isDebugEnabled()) log.debug("Init called");
        @SuppressWarnings("unused")
		float CSSoftwareVersion = systemMemo.getXNetTrafficController()
                                       .getCommandStation()
                                       .getCommandStationSoftwareVersion();
        @SuppressWarnings("unused")
		int CSType = systemMemo.getXNetTrafficController()
                                          .getCommandStation()
                                          .getCommandStationType();

        /* First, we load things that should work on all systems */
        jmri.InstanceManager.setPowerManager(new jmri.jmrix.lenz.XNetPowerManager(systemMemo));
        jmri.InstanceManager.setThrottleManager(new jmri.jmrix.lenz.hornbyelite.EliteXNetThrottleManager(systemMemo));
            
	if (log.isDebugEnabled()) log.debug("Command Station is Hornby Elite (manually identified).");
        jmri.InstanceManager.setTurnoutManager(new jmri.jmrix.lenz.hornbyelite.EliteXNetTurnoutManager(systemMemo.getXNetTrafficController(),systemMemo.getSystemPrefix()));
        jmri.InstanceManager.setLightManager(new jmri.jmrix.lenz.XNetLightManager(systemMemo.getXNetTrafficController(),systemMemo.getSystemPrefix()));
        jmri.InstanceManager.setProgrammerManager(new jmri.jmrix.lenz.XNetProgrammerManager(new jmri.jmrix.lenz.hornbyelite.EliteXNetProgrammer(systemMemo.getXNetTrafficController()),systemMemo));

	if(log.isDebugEnabled()) log.debug("XPressNet Initilization Complete");
    }

    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(EliteXNetInitilizationManager.class.getName());

}
