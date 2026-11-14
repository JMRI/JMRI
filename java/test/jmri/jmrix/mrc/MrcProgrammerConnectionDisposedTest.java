package jmri.jmrix.mrc;

import static org.junit.jupiter.api.Assertions.assertEquals;

import jmri.ProgListener;
import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

/**
 * Check that an MRC programming operation still running when its connection
 * is disposed ends cleanly when it times out, and that a programmer only
 * removes the traffic listener it added, from the traffic controller it added
 * it to.
 *
 * @author Keith Bruce Copyright (C) 2026
 */
public class MrcProgrammerConnectionDisposedTest {

    private MrcSystemConnectionMemo memo;
    private MrcInterfaceScaffold tc;
    private int replyStatus;

    private final ProgListener listener = (value, status) -> replyStatus = status;

    @Test
    public void testServiceModeWriteTimesOutAfterDispose() throws Exception {
        MrcProgrammer programmer = new MrcProgrammer(memo);
        programmer.writeCV("29", 6, listener);
        assertEquals(1, tc.trafficListeners.size(), "listening for the reply");

        memo.dispose();
        programmer.timeout();

        assertEquals(ProgListener.FailedTimeout, replyStatus, "told the write timed out");
        assertEquals(0, tc.trafficListeners.size(), "no longer listening to the traffic controller");
    }

    @Test
    public void testOpsModeWriteTimesOutAfterDispose() throws Exception {
        MrcOpsModeProgrammer programmer = new MrcOpsModeProgrammer(memo, 5, false);
        programmer.writeCV("29", 6, listener);
        assertEquals(0, tc.trafficListeners.size(), "an ops mode write waits for no reply");

        memo.dispose();
        programmer.timeout();

        assertEquals(ProgListener.FailedTimeout, replyStatus, "told the write timed out");
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        memo = new MrcSystemConnectionMemo();
        tc = new MrcInterfaceScaffold();
        memo.setMrcTrafficController(tc);
        replyStatus = -1;
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }

}
