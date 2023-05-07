package jmri.jmrix.tmcc;

import jmri.Turnout;
import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

/**
 * Tests for the SerialTurnoutManager class.
 *
 * @author Bob Jacobsen
 */
public class SerialTurnoutManagerTest extends jmri.managers.AbstractTurnoutMgrTestBase {

    @AfterEach
    public void tearDown() {
        JUnitUtil.clearShutDownManager(); // put in place because AbstractMRTrafficController implementing subclass was not terminated properly
        JUnitUtil.tearDown();

    }

    @BeforeEach
    @Override
    public void setUp(){
        JUnitUtil.setUp();

        // create and register the manager object
        TmccSystemConnectionMemo memo = new TmccSystemConnectionMemo("T", "TMCC Test");
        l = new SerialTurnoutManager(memo);
        jmri.InstanceManager.setTurnoutManager(l);
    }

    @Override
    public String getSystemName(int n) {
        return "TT" + n;
    }

    @Test
    public void testAsAbstractFactory() {
        // ask for a Turnout, and check type
        Turnout o = l.newTurnout("TT21", "my name");

        Assertions.assertNotNull(o);
        Assertions.assertTrue( o instanceof SerialTurnout );

        // make sure loaded into tables

        Assertions.assertNotNull( l.getBySystemName("TT21"));
        Assertions.assertNotNull( l.getByUserName("my name"));

    }

    // private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(SerialTurnoutManagerTest.class);

}
