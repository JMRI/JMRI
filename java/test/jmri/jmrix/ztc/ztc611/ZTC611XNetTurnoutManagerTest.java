package jmri.jmrix.ztc.ztc611;

import jmri.Turnout;
import jmri.TurnoutManager;
import jmri.jmrix.lenz.LenzCommandStation;
import jmri.jmrix.lenz.XNetInterfaceScaffold;
import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;

/**
 * Tests for the jmri.jmrix.ztc.ztc611.ZTC611XNetTurnoutManager class.
 *
 * @author Bob Jacobsen Copyright 2004
 */
public class ZTC611XNetTurnoutManagerTest extends jmri.managers.AbstractTurnoutMgrTestBase {

    @Override
    public String getSystemName(int i) {
        return "XT" + i;
    }

    XNetInterfaceScaffold lnis;

    @Test
    public void testAsAbstractFactory() {

        // ask for a Turnout, and check type
        TurnoutManager t = jmri.InstanceManager.getDefault(TurnoutManager.class);
        Assertions.assertTrue(t instanceof ZTC611XNetTurnoutManager );

        Turnout o = t.newTurnout("XT21", "my name");

        Assertions.assertNotNull( o );
        Assertions.assertTrue( o instanceof ZTC611XNetTurnout );

        // make sure loaded into tables
        Assertions.assertNotNull( t.getBySystemName("XT21"));
        Assertions.assertNotNull( t.getByUserName("my name"));

    }

    @Test
    @Override
    public void testThrownText(){
         Assert.assertEquals("thrown text","Thrown (+)",l.getThrownText());
    }

    @Test
    @Override
    public void testClosedText(){
         Assert.assertEquals("closed text","Closed (-)",l.getClosedText());
    }

    

    @Override
    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetInstanceManager();
        // prepare an interface, register
        lnis = new XNetInterfaceScaffold(new LenzCommandStation());
        // create and register the manager object
        l = new ZTC611XNetTurnoutManager(lnis.getSystemConnectionMemo());
        jmri.InstanceManager.setDefault(TurnoutManager.class,l);
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.clearShutDownManager(); // put in place because AbstractMRTrafficController implementing subclass was not terminated properly
        JUnitUtil.tearDown();
    }

    // private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ZTC611XNetTurnoutManagerTest.class);

}
