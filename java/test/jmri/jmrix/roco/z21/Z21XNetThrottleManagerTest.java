package jmri.jmrix.roco.z21;

import jmri.jmrix.lenz.LenzCommandStation;
import jmri.jmrix.lenz.XNetInterfaceScaffold;
import jmri.jmrix.lenz.XNetSystemConnectionMemo;
import jmri.jmrix.lenz.XNetThrottleManagerTest;
import org.junit.After;
import org.junit.Before;


/**
 * Tests for the jmri.jmrix.lenz.z21XNetThrottleManager class
 *
 * @author	Paul Bender Copyright (C) 2015,2016
 */
public class Z21XNetThrottleManagerTest extends XNetThrottleManagerTest {

    // The minimal setup for log4J
    @Override
    @Before
    public void setUp() {
        apps.tests.Log4JFixture.setUp();
        jmri.util.JUnitUtil.resetInstanceManager();
        XNetInterfaceScaffold tc = new XNetInterfaceScaffold(new LenzCommandStation());
        tm = new Z21XNetThrottleManager(new XNetSystemConnectionMemo(tc));
    }

    @After
    @Override
    public void tearDown() {
        jmri.util.JUnitUtil.resetInstanceManager();
        apps.tests.Log4JFixture.tearDown();
    }

}
