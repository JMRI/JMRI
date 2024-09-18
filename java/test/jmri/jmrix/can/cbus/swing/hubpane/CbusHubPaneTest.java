package jmri.jmrix.can.cbus.swing.hubpane;

import jmri.jmrix.can.*;
import jmri.util.JUnitAppender;
import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

/**
 * @author Steve Young Copyright(C) 2022
 */
@jmri.util.junit.annotations.DisabledIfHeadless
public class CbusHubPaneTest {

    private CbusHubPane hub = null;
    private CanSystemConnectionMemo memo = null;
    private TrafficController tc = null;

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

        JUnitUtil.removeMatchingThreads("openlcb-hub-output"); // Daemon thread
        JUnitAppender.suppressErrorMessageStartsWith("Hub: Interrupted in queue handling loop");

        JUnitUtil.tearDown();
    }
}
