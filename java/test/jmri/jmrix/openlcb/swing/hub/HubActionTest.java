package jmri.jmrix.openlcb.swing.hub;

import jmri.jmrix.can.CanSystemConnectionMemo;
import jmri.jmrix.can.TestTrafficController;
import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.DisabledIfSystemProperty;

/**
 * @author Bob Jacobsen Copyright 2013
 * @author Paul Bender Copyright(C) 2016
 */
public class HubActionTest {

    CanSystemConnectionMemo memo = null;
    jmri.jmrix.can.TrafficController tc = null;

    @Test
    @DisabledIfSystemProperty(named ="java.awt.headless", matches ="true")
    public void testCtor() {
        HubAction h = new HubAction();
        Assertions.assertNotNull(h, "Action object non-null");
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetProfileManager();

        memo  = new jmri.jmrix.openlcb.OlcbSystemConnectionMemoScaffold();
        tc = new TestTrafficController();
        memo.setTrafficController(tc);
        jmri.InstanceManager.setDefault(CanSystemConnectionMemo.class,memo);
    }

    @AfterEach
    public void tearDown() {
        memo.dispose();
        memo = null;
        tc.terminateThreads();
        tc = null;
        JUnitUtil.tearDown();

    }
}
