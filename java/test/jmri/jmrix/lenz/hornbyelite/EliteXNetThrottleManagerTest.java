package jmri.jmrix.lenz.hornbyelite;

import jmri.jmrix.lenz.XNetInterfaceScaffold;
import jmri.jmrix.lenz.XNetSystemConnectionMemo;
import org.junit.After;
import org.junit.Before;

/**
 * EliteXNetThrottleManagerTest.java
 *
 * Description:	tests for the jmri.jmrix.lenz.EliteXNetThrottleManager class
 *
 * @author	Paul Bender
 */
public class EliteXNetThrottleManagerTest extends jmri.jmrix.lenz.XNetThrottleManagerTest {

    // The minimal setup for log4J
    @Override
    @Before
    public void setUp() {
        apps.tests.Log4JFixture.setUp();
        jmri.util.JUnitUtil.resetInstanceManager();
        XNetInterfaceScaffold tc = new XNetInterfaceScaffold(new HornbyEliteCommandStation());
        tm = new EliteXNetThrottleManager(new XNetSystemConnectionMemo(tc));
    }

    @After
    @Override
    public void tearDown() {
        jmri.util.JUnitUtil.resetInstanceManager();
        apps.tests.Log4JFixture.tearDown();
    }

}
