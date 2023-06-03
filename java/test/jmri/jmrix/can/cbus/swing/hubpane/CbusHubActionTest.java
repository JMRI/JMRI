package jmri.jmrix.can.cbus.swing.hubpane;

import jmri.jmrix.can.*;
import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.DisabledIfSystemProperty;

/**
 * @author Steve Young Copyright(C) 2022
 */
public class CbusHubActionTest {

    CanSystemConnectionMemo memo = null;
    TrafficController tc = null;

    @Test
    @DisabledIfSystemProperty(named ="java.awt.headless", matches ="true")
    public void testCtor() {
        CbusHubAction h = new CbusHubAction();
        Assertions.assertNotNull(h, "Action object non-null");
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetProfileManager();

        memo  = new CanSystemConnectionMemo();
        tc = new TrafficControllerScaffold();
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
