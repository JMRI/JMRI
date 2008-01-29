// EliteXNetInitilizationManager.java

package jmri.jmrix.lenz.hornbyelite;
import jmri.jmrix.lenz.AbstractXNetInitilizationManager;
import jmri.jmrix.lenz.XNetTrafficController;

/**
 * This class performs Command Station dependant initilization for 
 * The Hornby Elite.  
 * It adds the appropriate Managers via the Initilization Manager
 * based on the Command Station Type.
 *
 * @author			Paul Bender  Copyright (C) 2003,2008
 * @version			$Revision: 1.1 $
 */
public class EliteXNetInitilizationManager extends AbstractXNetInitilizationManager{

    protected void init() {
	if(log.isDebugEnabled()) log.debug("Init called");
        float CSSoftwareVersion = XNetTrafficController.instance()
                                       .getCommandStation()
                                       .getCommandStationSoftwareVersion();
        int CSType = XNetTrafficController.instance()
                                          .getCommandStation()
                                          .getCommandStationType();

        /* First, we load things that should work on all systems */
        jmri.InstanceManager.setPowerManager(new jmri.jmrix.lenz.XNetPowerManager());
        jmri.InstanceManager.setThrottleManager(new jmri.jmrix.lenz.hornbyelite.EliteXNetThrottleManager());
            
	if (log.isDebugEnabled()) log.debug("Command Station is Hornby Elite (manually identified).");
        jmri.InstanceManager.setTurnoutManager(new jmri.jmrix.lenz.hornbyelite.EliteXNetTurnoutManager());
        jmri.InstanceManager.setProgrammerManager(new jmri.jmrix.lenz.XNetProgrammerManager(jmri.jmrix.lenz.hornbyelite.EliteXNetProgrammer.instance()));

	if(log.isDebugEnabled()) log.debug("XPressNet Initilization Complete");
    }

    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(EliteXNetInitilizationManager.class.getName());

}
