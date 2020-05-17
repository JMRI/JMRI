package jmri.jmrix.lenz;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Before;

/**
 * XNetThrottleManagerTest.java
 *
 * Test for the jmri.jmrix.lenz.XNetThrottleManager class
 *
 * @author Paul Bender
 */
public class XNetThrottleManagerTest extends jmri.managers.AbstractThrottleManagerTestBase {

    @Override
    @Before
    public void setUp() {
        JUnitUtil.setUp();
        XNetInterfaceScaffold tc = new XNetInterfaceScaffold(new LenzCommandStation());
        tm = new XNetThrottleManager(new XNetSystemConnectionMemo(tc));
    }

    @After
    public  void tearDown() {
        JUnitUtil.clearShutDownManager(); // put in place because AbstractMRTrafficController implementing subclass was not terminated properly
        JUnitUtil.tearDown();
    }

}
