package jmri.jmrix.tams;

import jmri.Turnout;
import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

/**
 * Tests for the TurnoutManager class
 *
 * @author Bob Jacobsen  Copyright 2013, 2016
 */
public class TamsTurnoutManagerTest extends jmri.managers.AbstractTurnoutMgrTestBase {

    private TamsInterfaceScaffold nis = null;
    private TamsSystemConnectionMemo tm = null;

    @AfterEach
    public void tearDown() {
        JUnitUtil.clearShutDownManager(); // put in place because AbstractMRTrafficController implementing subclass was not terminated properly
        JUnitUtil.tearDown();
    }

    @Override
    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();

        jmri.util.JUnitUtil.initInternalTurnoutManager();
        // prepare an interface, register
        nis = new TamsInterfaceScaffold();
        tm = new TamsSystemConnectionMemo(nis);
        // create and register the manager object
        l = new TamsTurnoutManager(tm);
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
        Assertions.assertTrue(o instanceof TamsTurnout );

        Assertions.assertNotNull( l.getBySystemName("TT21"));
        Assertions.assertNotNull( l.getByUserName("my name"));
    }

    // private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(TamsTurnoutManagerTest.class);

}
