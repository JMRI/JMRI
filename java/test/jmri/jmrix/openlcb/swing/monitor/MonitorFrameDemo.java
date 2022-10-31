package jmri.jmrix.openlcb.swing.monitor;

import java.awt.GraphicsEnvironment;

import jmri.jmrix.can.CanListener;
import jmri.jmrix.can.CanMessage;
import jmri.jmrix.can.CanReply;
import jmri.jmrix.can.CanSystemConnectionMemo;
import jmri.jmrix.can.TrafficControllerScaffold;
import jmri.util.JUnitUtil;

import org.junit.Assume;
import org.junit.jupiter.api.*;
import org.openlcb.can.AliasMap;

/**
 * Tests for the jmri.jmrix.can.swing.monitor.MonitorFrame class
 *
 * @author Bob Jacobsen Copyright 2010
 */
public class MonitorFrameDemo {

    private CanSystemConnectionMemo memo = null;

    private static class OurScaffold extends TrafficControllerScaffold {

        /*
         * Forward CanMessage to object under test
         */
        private void testMessage(CanMessage f) {
            // FIXME: must clone, iterator is not threadsafe.
            for (jmri.jmrix.AbstractMRListener c : cmdListeners) {
                ((CanListener) c).message(f);
            }
        }

        private void testReply(CanReply f) {
            // FIXME: must clone, iterator is not threadsafe.
            for (jmri.jmrix.AbstractMRListener c : cmdListeners) {
                ((CanListener) c).reply(f);
            }
        }
    }

    private OurScaffold tcs = null;

    @Test
    public void testFireViaAction() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());

        new MonitorAction().actionPerformed(null);

        jmri.jmrix.can.CanMessage msg
                = new jmri.jmrix.can.CanMessage(
                        new int[]{1, 2}, 0x12345678);
        msg.setExtended(true);

        tcs.testMessage(msg);

        jmri.jmrix.can.CanReply reply
                = new jmri.jmrix.can.CanReply(
                        new int[]{1, 2});
        reply.setExtended(true);
        reply.setHeader(0x12345678);

        tcs.testReply(reply);

    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();

        jmri.util.JUnitUtil.initDefaultUserMessagePreferences();
        memo = new CanSystemConnectionMemo();
        tcs = new OurScaffold();

        memo.setTrafficController(tcs);
        memo.store(new AliasMap(), org.openlcb.can.AliasMap.class);
        jmri.InstanceManager.setDefault(CanSystemConnectionMemo.class, memo);
    }

    @AfterEach
    public void tearDown() {
        memo.dispose();
        memo = null;
        tcs.terminateThreads();
        tcs = null;
        jmri.util.JUnitUtil.resetWindows(false, false);
        JUnitUtil.clearShutDownManager();
        JUnitUtil.tearDown();

    }
}
