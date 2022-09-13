package jmri.jmrix.maple;

import jmri.Turnout;
import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

/**
 * JUnit tests for the jmri.jmrix.maple.SerialTurnoutManager class.
 *
 * @author Bob Jacobsen
 */
public class SerialTurnoutManagerTest extends jmri.managers.AbstractTurnoutMgrTestBase {

    private MapleSystemConnectionMemo memo = null;

    @Override
    public String getSystemName(int n) {
        return "KT" + n;
    }

    @Test
    public void testConstructor() {
        // create and register the manager object
        SerialTurnoutManager atm = new SerialTurnoutManager(new MapleSystemConnectionMemo());
        Assertions.assertNotNull( atm, "Maple Turnout Manager creation with memo" );
    }

    @Test
    public void testAsAbstractFactory() {
        // ask for a Turnout, and check type
        Turnout o = l.newTurnout("KT21", "my name");

        Assertions.assertNotNull(o);
        Assertions.assertTrue( o instanceof SerialTurnout );

        // make sure loaded into tables

        Assertions.assertNotNull( l.getBySystemName("KT21"));
        Assertions.assertNotNull( l.getByUserName("my name"));

    }

    @Override
    @BeforeEach
    public void setUp(){
        jmri.util.JUnitUtil.setUp();
        // replace the SerialTrafficController
        SerialTrafficController t = new SerialTrafficController() {
            SerialTrafficController test() {
                return this;
            }
        }.test();
        t.registerNode(new SerialNode(t));
        memo = new MapleSystemConnectionMemo("K", "Maple");
        // create and register the turnout manager object
        l = new SerialTurnoutManager(memo);
        jmri.InstanceManager.setTurnoutManager(l);
    }

    @AfterEach
    public void tearDown() {
        memo.dispose();
        JUnitUtil.clearShutDownManager(); // put in place because AbstractMRTrafficController implementing subclass was not terminated properly
        JUnitUtil.tearDown();

    }

    // private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(SerialTurnoutManagerTest.class);

}
