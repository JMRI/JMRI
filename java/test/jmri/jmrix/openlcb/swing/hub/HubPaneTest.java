package jmri.jmrix.openlcb.swing.hub;

import jmri.jmrix.can.CanSystemConnectionMemo;
import jmri.jmrix.can.TestTrafficController;
import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.DisabledIfSystemProperty;

/**
 * @author Bob Jacobsen Copyright 2013
 */
@DisabledIfSystemProperty(named = "java.awt.headless", matches = "true")
public class HubPaneTest {

    HubPane hub = null;
    CanSystemConnectionMemo memo = null;
    jmri.jmrix.can.TrafficController tc = null;

    @Test
    public void testCtor() {
        Assertions.assertNotNull(hub, "hub pane creation");
        // this next step takes 30 seconds of clock time, so has been commented out
        //hub.initContext(memo);
    }

    @BeforeAll
    static public void checkSeparate() {
       // this test is run separately because it leaves a lot of threads behind
        Assumptions.assumeFalse( Boolean.getBoolean("jmri.skipTestsRequiringSeparateRunning"),
            "Ignoring intermittent test");
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetProfileManager();

        memo  = new jmri.jmrix.openlcb.OlcbSystemConnectionMemoScaffold();
        tc = new TestTrafficController();
        memo.setTrafficController(tc);
        jmri.InstanceManager.setDefault(CanSystemConnectionMemo.class,memo);
        hub = new HubPane();
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
