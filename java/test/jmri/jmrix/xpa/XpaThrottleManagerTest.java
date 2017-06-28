package jmri.jmrix.xpa;

import org.junit.After;
import org.junit.Before;

/**
 * XpaThrottleManagerTest.java
 *
 * Description:	tests for the jmri.jmrix.xpa.XpaThrottleManager class
 *
 * @author	Paul Bender Copyright (C) 2012,2016
 */
public class XpaThrottleManagerTest extends jmri.managers.AbstractThrottleManagerTestBase {

    // The minimal setup for log4J
    @Override
    @Before
    public void setUp() {
        apps.tests.Log4JFixture.setUp();
        jmri.util.JUnitUtil.resetInstanceManager();
        XpaSystemConnectionMemo memo = new XpaSystemConnectionMemo();
        memo.setXpaTrafficController(new XpaTrafficController());
        tm = new XpaThrottleManager(memo);
    }

    @After
    public void tearDown() {
        jmri.util.JUnitUtil.resetInstanceManager();
        apps.tests.Log4JFixture.tearDown();
    }

}
