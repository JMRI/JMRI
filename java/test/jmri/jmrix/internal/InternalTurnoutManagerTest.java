package jmri.jmrix.internal;


import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Before;


/**
 * Tests for the jmri.jmrix.internal.InternalTurnoutManager class.
 *
 * @author	Bob Jacobsen Copyright 2016
 */
public class InternalTurnoutManagerTest extends jmri.managers.AbstractTurnoutMgrTestBase {

    @Override
    public String getSystemName(int i) {
        return "IT" + i;
    }

    // from here down is testing infrastructure
    // The minimal setup for log4J
    @Override
    @Before
    public void setUp() {
        apps.tests.Log4JFixture.setUp();
        // create and register the manager object
        jmri.util.JUnitUtil.resetInstanceManager();
        jmri.util.JUnitUtil.initInternalTurnoutManager();
        l = jmri.InstanceManager.turnoutManagerInstance();
    }

    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }

}
