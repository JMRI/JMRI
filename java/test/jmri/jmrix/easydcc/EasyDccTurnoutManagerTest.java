package jmri.jmrix.easydcc;

import jmri.Turnout;
import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

/**
 * Tests for the jmri.jmrix.easydcc.EasyDccTurnoutManager class
 *
 * @author Bob Jacobsen
 */
public class EasyDccTurnoutManagerTest extends jmri.managers.AbstractTurnoutMgrTestBase {

    @AfterEach
    public void tearDown() {
        JUnitUtil.clearShutDownManager(); // put in place because AbstractMRTrafficController implementing subclass was not terminated properly
        JUnitUtil.tearDown();

    }

    @BeforeEach
    @Override
    public void setUp() {
        JUnitUtil.setUp();

        // create and register the manager object
        EasyDccSystemConnectionMemo memo = new EasyDccSystemConnectionMemo("E", "EasyDCC Test");
        l = new EasyDccTurnoutManager(memo);
        jmri.InstanceManager.setTurnoutManager(l);
    }

    @Override
    public String getSystemName(int n) {
        return "ET" + n;
    }

    @Test
    public void testAsAbstractFactory() {
        // ask for a Turnout, and check type
        Turnout o = l.newTurnout("ET21", "my name");
        Assertions.assertNotNull( o);
        Assertions.assertTrue( o instanceof EasyDccTurnout );

        // make sure loaded into tables
        Assertions.assertNotNull( l.getBySystemName("ET21"));
        Assertions.assertNotNull( l.getByUserName("my name"));

    }

    // private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(EasyDccTurnoutManagerTest.class);

}
