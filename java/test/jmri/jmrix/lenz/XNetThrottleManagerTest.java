package jmri.jmrix.lenz;

import org.junit.After;
import org.junit.Before;

/**
 * XNetThrottleManagerTest.java
 *
 * Description:	tests for the jmri.jmrix.lenz.XNetThrottleManager class
 *
 * @author	Paul Bender
 */
public class XNetThrottleManagerTest extends jmri.managers.AbstractThrottleManagerTestBase {

    // The minimal setup for log4J
    @Override
    @Before
    public void setUp() {
        apps.tests.Log4JFixture.setUp();
        jmri.util.JUnitUtil.resetInstanceManager();
        XNetInterfaceScaffold tc = new XNetInterfaceScaffold(new LenzCommandStation());
        tm = new XNetThrottleManager(new XNetSystemConnectionMemo(tc));
    }

    @After
    public  void tearDown() {
        jmri.util.JUnitUtil.resetInstanceManager();
        apps.tests.Log4JFixture.tearDown();
    }

}
