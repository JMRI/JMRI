package jmri.jmrix.oaktree;

import jmri.Turnout;
import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

/**
 * JUnit tests for the SerialTurnoutManager class
 *
 * @author Bob Jacobsen
 */
public class SerialTurnoutManagerTest extends jmri.managers.AbstractTurnoutMgrTestBase {

    private OakTreeSystemConnectionMemo memo = null;

    @Override
    public String getSystemName(int n) {
        return "OT" + n;
    }

    @Test
    public void testCtor() {
        // create and register the manager object
        SerialTurnoutManager atm = new SerialTurnoutManager(memo);
        Assertions.assertNotNull( atm, "Oak Tree Turnout Manager creation" );
    }

    @Test
    public void testConstructor() {
        // create and register the manager object
        SerialTurnoutManager atm = new SerialTurnoutManager(new OakTreeSystemConnectionMemo());
        Assertions.assertNotNull(atm, "Oak Tree Turnout Manager creation with memo" );
    }

    @Test
    public void testAsAbstractFactory() {
        // ask for a Turnout, and check type
        Turnout o = l.newTurnout("OT21", "my name");

        Assertions.assertNotNull( o );
        Assertions.assertTrue(o instanceof SerialTurnout );

        // make sure loaded into tables
        Assertions.assertNotNull( l.getBySystemName("OT21"));
        Assertions.assertNotNull( l.getByUserName("my name"));
    }

    @BeforeEach
    @Override
    public void setUp() {
        JUnitUtil.setUp();
        memo = new OakTreeSystemConnectionMemo("O", "Oak Tree");
        SerialTrafficController t = new SerialTrafficControlScaffold(memo);
        memo.setTrafficController(t); // important for successful getTrafficController()
        t.registerNode(new SerialNode(0, SerialNode.IO48, memo));
        // create and register the manager object
        l = new SerialTurnoutManager(memo);
        jmri.InstanceManager.setTurnoutManager(l);
    }

    @AfterEach
    public void tearDown() {
        l.dispose();
        memo.dispose();
        JUnitUtil.clearShutDownManager(); // put in place because AbstractMRTrafficController implementing subclass was not terminated properly
        JUnitUtil.tearDown();

    }

    // private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(SerialTurnoutManagerTest.class);

}
