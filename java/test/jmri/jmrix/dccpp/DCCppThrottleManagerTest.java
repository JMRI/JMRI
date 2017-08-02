package jmri.jmrix.dccpp;

import org.junit.After;
import org.junit.Before;

/**
 * DCCppThrottleManagerTest.java
 *
 * Description:	tests for the jmri.jmrix.dccpp.DCCppThrottleManager class
 *
 * @author	Paul Bender
 * @author	Mark Underwood (C) 2015
 */
public class DCCppThrottleManagerTest extends jmri.managers.AbstractThrottleManagerTestBase {

    // The minimal setup for log4J
    @Override
    @Before
    public void setUp() {
        apps.tests.Log4JFixture.setUp();
        jmri.util.JUnitUtil.resetInstanceManager();
        DCCppInterfaceScaffold tc = new DCCppInterfaceScaffold(new DCCppCommandStation());
        tm = new DCCppThrottleManager(new DCCppSystemConnectionMemo(tc));
    }

    @After
    public void tearDown() {
        jmri.util.JUnitUtil.resetInstanceManager();
        apps.tests.Log4JFixture.tearDown();
    }

}
