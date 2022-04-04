package jmri.jmrix.secsi;

import jmri.InstanceManager;
import jmri.Turnout;
import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * SerialTurnoutManagerTest.java
 *
 * Test for the SerialTurnoutManager class
 *
 * @author Bob Jacobsen Copyright 2004, 2008
 */
public class SerialTurnoutManagerTest extends jmri.managers.AbstractTurnoutMgrTestBase {

    private SecsiSystemConnectionMemo memo;
    private SerialTrafficController t;

    @Override
    public String getSystemName(int n) {
        return "VT" + n;
    }

    @Test
    public void testAsAbstractFactory() {
        // ask for a Turnout, and check type
        Turnout o = l.newTurnout("VT21", "my name");

        if (log.isDebugEnabled()) {
            log.debug("received turnout value {}", o);
        }
        Assertions.assertNotNull( o);
        Assertions.assertTrue(o instanceof SerialTurnout);

        // make sure loaded into tables
        if (log.isDebugEnabled()) {
            log.debug("by system name: {}", l.getBySystemName("VT21"));
        }
        if (log.isDebugEnabled()) {
            log.debug("by user name:   {}", l.getByUserName("my name"));
        }

        Assertions.assertNotNull( l.getBySystemName("VT21"));
        Assertions.assertNotNull( l.getByUserName("my name"));

    }

    @Override
    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        memo = new SecsiSystemConnectionMemo();
        t = new SerialTrafficControlScaffold(memo);
        memo.setTrafficController(t);
        t.registerNode(new SerialNode(0, SerialNode.DAUGHTER,t));
        // create and register the manager object
        l = new SerialTurnoutManager(memo);
        InstanceManager.setTurnoutManager(l);
    }

    @AfterEach
    public void tearDown() {
        if ( l != null ){
            l.dispose();
        }
        l = null;
        t.terminateThreads();
        memo.dispose();
        t = null;
        memo = null;
        JUnitUtil.tearDown();
    }

    private final static Logger log = LoggerFactory.getLogger(SerialTurnoutManagerTest.class);

}
