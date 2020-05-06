package jmri.jmrix.lenz.hornbyelite;

import jmri.jmrix.lenz.XNetInterfaceScaffold;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Before;

/**
 * EliteXNetThrottleManagerTest.java
 * <p>
 * Test for the jmri.jmrix.lenz.EliteXNetThrottleManager class
 *
 * @author Paul Bender
 */
public class EliteXNetThrottleManagerTest extends jmri.jmrix.lenz.XNetThrottleManagerTest {

    @Override
    @Before
    public void setUp() {
        JUnitUtil.setUp();
        XNetInterfaceScaffold tc = new XNetInterfaceScaffold(new HornbyEliteCommandStation());
        tm = new EliteXNetThrottleManager(new EliteXNetSystemConnectionMemo(tc));
    }

    @After
    @Override
    public void tearDown() {
        JUnitUtil.clearShutDownManager(); // put in place because AbstractMRTrafficController implementing subclass was not terminated properly
        JUnitUtil.tearDown();
    }

}
