// BlockTableActionTest.java

package jmri.jmrit.beantable;

import org.apache.log4j.Logger;
import junit.framework.*;
import junit.extensions.jfcunit.*;

import jmri.Block;
import jmri.InstanceManager;
import jmri.util.*;

/**
 * Tests for the jmri.jmrit.beantable.BlockTableAction class
 * @author	Bob Jacobsen  Copyright 2004, 2007, 2008
 * @version	$Revision$
 */
public class BlockTableActionTest extends jmri.util.SwingTestCase {

    public void testCreate() {
        jmri.InstanceManager.store(jmri.managers.DefaultUserMessagePreferences.getInstance(), jmri.UserPreferencesManager.class);
        BlockTableAction ba = new BlockTableAction();
        assertNotNull("BlockTableAction is null!", ba);
        TestHelper.disposeWindow(ba.f, this);
    }

    public void testInvoke() {
        jmri.InstanceManager.store(jmri.managers.DefaultUserMessagePreferences.getInstance(), jmri.UserPreferencesManager.class);
        BlockTableAction ba = new BlockTableAction();
        ba.actionPerformed(null);
        
        // create a couple blocks, and see if they show
        InstanceManager.blockManagerInstance().createNewBlock("IB1", "block 1");
        
        Block b2 = InstanceManager.blockManagerInstance().createNewBlock("IB2", "block 2");
        b2.setDirection(jmri.Path.EAST);
        TestHelper.disposeWindow(ba.f, this);
    }


    // from here down is testing infrastructure

    public BlockTableActionTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {"-noloading", BlockTableActionTest.class.getName()};
        junit.swingui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(BlockTableActionTest.class);
        return suite;
    }

    // The minimal setup for log4J
    // The minimal setup for log4J
    protected void setUp() throws Exception { 
        apps.tests.Log4JFixture.setUp(); 

        super.setUp();
        JUnitUtil.resetInstanceManager();
        JUnitUtil.initInternalTurnoutManager();
        JUnitUtil.initInternalLightManager();
        JUnitUtil.initInternalSensorManager();
        JUnitUtil.initInternalSignalHeadManager();
    }
    protected void tearDown() throws Exception { 
        JUnitUtil.resetInstanceManager();
        super.tearDown();
        apps.tests.Log4JFixture.tearDown();
    }

    static Logger log = Logger.getLogger(BlockTableActionTest.class.getName());
}
