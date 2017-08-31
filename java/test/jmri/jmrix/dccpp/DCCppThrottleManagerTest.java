package jmri.jmrix.dccpp;

import jmri.util.JUnitUtil;
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
        JUnitUtil.setUp();
        DCCppInterfaceScaffold tc = new DCCppInterfaceScaffold(new DCCppCommandStation());
        tm = new DCCppThrottleManager(new DCCppSystemConnectionMemo(tc));
    }

    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }

}
