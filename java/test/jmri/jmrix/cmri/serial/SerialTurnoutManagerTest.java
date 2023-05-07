package jmri.jmrix.cmri.serial;

import jmri.Turnout;
import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

/**
 * Tests for the jmri.jmrix.cmri.SerialTurnoutManager class
 *
 * @author Bob Jacobsen
 */
public class SerialTurnoutManagerTest extends jmri.managers.AbstractTurnoutMgrTestBase {

    private jmri.jmrix.cmri.CMRISystemConnectionMemo memo = null;
    private SerialTrafficControlScaffold stcs = null;

    @Override
    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        // replace the SerialTrafficController
        stcs = new SerialTrafficControlScaffold();
        stcs.registerNode(new SerialNode(stcs));
        memo = new jmri.jmrix.cmri.CMRISystemConnectionMemo();
        memo.setTrafficController(stcs);
        // create and register the turnout manager object
        l = new SerialTurnoutManager(memo);
        jmri.InstanceManager.setTurnoutManager(l);
    }

    @AfterEach
    public void tearDown() {
        if (stcs != null) {
            stcs.terminateThreads();
        }
        stcs = null;
        memo = null;

        JUnitUtil.clearShutDownManager(); // put in place because AbstractMRTrafficController implementing subclass was not terminated properly
        JUnitUtil.tearDown();
    }

    @Override
    public String getSystemName(int n) {
        return "CT" + n;
    }

    @Test
    public void testAsAbstractFactory() {
        // ask for a Turnout, and check type
        Turnout o = l.newTurnout("CT21", "my name");

        Assertions.assertNotNull( o);
        Assertions.assertTrue(o instanceof SerialTurnout);

        Assertions.assertNotNull( l.getBySystemName("CT21"));
        Assertions.assertNotNull( l.getByUserName("my name"));

    }

    // private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(SerialTurnoutManagerTest.class);

}
