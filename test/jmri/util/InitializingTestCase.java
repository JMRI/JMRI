package jmri.util;

import junit.framework.Assert;
import junit.framework.TestCase;

import jmri.InstanceManager;
import jmri.managers.InternalLightManager;
import jmri.managers.InternalSensorManager;
import jmri.managers.InternalTurnoutManager;


/**
 * Intermediate implementation of TestCase that
 * adds the ability to release reset the InstanceManager
 *
 * @author	Bob Jacobsen - Copyright 2009
 * @version	$Revision: 1.1 $
 */
 
public class InitializingTestCase extends TestCase {

    public InitializingTestCase(String s) { super(s); }
        
    public void resetInstanceManager() {    
        // create a new instance manager
        InstanceManager i = new InstanceManager(){
            protected void init() {
                root = null;
                super.init();
                root = this;
            }
        };
    }

    public void initInternalTurnoutManager() {
        InstanceManager.setTurnoutManager(new InternalTurnoutManager());
    }

    public void initInternalLightManager() {
        InstanceManager.setLightManager(new InternalLightManager());
    }

    public void initInternalSensorManager() {
        InstanceManager.setSensorManager(new InternalSensorManager());
    }

}
