package jmri.jmrix.openlcb.swing.hub;

import jmri.jmrix.can.CanSystemConnectionMemo;
import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

import jmri.jmrix.can.TestTrafficController;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Bob Jacobsen Copyright 2013
 */
public class HubPaneTest {

    HubPane hub;
    jmri.jmrix.can.CanSystemConnectionMemo memo;
    jmri.jmrix.can.TrafficController tc;

    @Test
    public void testCtor() {
        assertThat(hub).withFailMessage("hub pane creation").isNotNull();
        // this next step takes 30 seconds of clock time, so has been commented out
        //hub.initContext(memo);
    }

    @BeforeAll
    static public void checkSeparate() {
        org.junit.Assume.assumeFalse("Ignoring intermittent test", Boolean.getBoolean("jmri.skipTestsRequiringSeparateRunning"));
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetProfileManager();

        memo  = new jmri.jmrix.openlcb.OlcbSystemConnectionMemo();
        tc = new TestTrafficController();
        memo.setTrafficController(tc);
        jmri.InstanceManager.setDefault(CanSystemConnectionMemo.class,memo);
        hub = new HubPane();
    }

    @AfterEach
    public void tearDown() {
        hub.stopHubThread();
        hub = null;
        memo.dispose();
        memo = null;
        tc.terminateThreads();
        tc = null;
        JUnitUtil.tearDown();
    }
}
