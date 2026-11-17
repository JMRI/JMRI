package jmri.jmrix.can.adapters.gridconnect;

import java.util.concurrent.atomic.AtomicBoolean;

import jmri.jmrix.AbstractPortController;
import jmri.jmrix.AbstractPortControllerScaffold;
import jmri.jmrix.can.CanMessage;
import jmri.jmrix.can.CanSystemConnectionMemo;
import jmri.util.JUnitAppender;
import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

/**
 * Tests that a CAN traffic controller reports a frame it drops because the
 * port was not ready to send when the frame's retries ran out.
 *
 * @author Keith Bruce Copyright (C) 2026
 */
public class GcTrafficControllerPortNotReadyTest {

    @Timeout(20)
    @Test
    public void testDroppedFrameReported() throws Exception {
        CanSystemConnectionMemo memo = new CanSystemConnectionMemo();
        AbstractPortControllerScaffold pcs = new AbstractPortControllerScaffold(memo);
        tc.connectPort(pcs);

        ready.set(false);
        CanMessage m = new CanMessage(new int[]{0x0D}, 0x7A); // QNN
        tc.sendCanMessage(m, null);
        JUnitUtil.waitFor(() -> JUnitAppender.checkForMessageStartingWith("sendMessage: ") != null,
                "dropped frame reported");
        Assertions.assertFalse(m.hasSentTime(), "frame not sent");

        ready.set(true);
        CanMessage next = new CanMessage(new int[]{0x0D}, 0x7A);
        tc.sendCanMessage(next, null);
        JUnitUtil.waitFor(next::hasSentTime, "next frame written once the port is ready");

        tc.disconnectPort(pcs);
        pcs.dispose();
    }

    private final AtomicBoolean ready = new AtomicBoolean(true);
    private GcTrafficController tc;

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetInstanceManager();
        tc = new GcTrafficController() {
            @Override
            public boolean portReadyToSend(AbstractPortController p) {
                return ready.get();
            }
        };
    }

    @AfterEach
    public void tearDown() {
        tc.terminateThreads();
        JUnitUtil.tearDown();
    }

}
