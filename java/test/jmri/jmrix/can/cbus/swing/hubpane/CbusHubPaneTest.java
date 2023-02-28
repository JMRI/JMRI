package jmri.jmrix.can.cbus.swing.hubpane;

import jmri.jmrix.can.*;
import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;
import org.netbeans.jemmy.operators.JFrameOperator;

/**
 * @author Steve Young Copyright(C) 2022
 */
public class CbusHubPaneTest {

    CbusHubPane hub = null;
    CanSystemConnectionMemo memo = null;
    TrafficController tc = null;

    @Test
    public void testCtor() {
        Assertions.assertNotNull(hub, "hub pane creation");
        // hub.initComponents(memo);
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetProfileManager();

        memo  = new CanSystemConnectionMemo();
        tc = new TrafficControllerScaffold();
        memo.setTrafficController(tc);
        jmri.InstanceManager.setDefault(CanSystemConnectionMemo.class,memo);
        hub = new CbusHubPane();
    }

    @AfterEach
    public void tearDown() {
        hub.dispose();
        hub = null;
        memo.dispose();
        memo = null;
        tc.terminateThreads();
        tc = null;
        JUnitUtil.tearDown();
    }
}
