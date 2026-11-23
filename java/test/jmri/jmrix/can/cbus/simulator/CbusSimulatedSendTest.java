package jmri.jmrix.can.cbus.simulator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import jmri.jmrix.can.CanReply;
import jmri.jmrix.can.CanSystemConnectionMemo;
import jmri.jmrix.can.TrafficControllerScaffold;
import jmri.jmrix.can.cbus.CbusConstants;
import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

/**
 * Tests for CbusSimulatedSend.
 *
 * @author Keith Bruce Copyright (C) 2026
 */
public class CbusSimulatedSendTest {

    private TrafficControllerScaffold tcis;
    private CanSystemConnectionMemo memo;
    private CbusSimulatedSend send;

    @Test
    public void testReceiveAtGivesTheTimeAndTakesItsPlace() {
        long now = System.nanoTime();
        // a frame with a delay of 40 ms, then frames received 20 ms ago, now, and 30 ms from now
        send.sendWithDelay(new CanReply(new int[]{CbusConstants.CBUS_ACON, 0x00, 0x01, 0x00, 40}), true, false, 40);
        send.receiveAt(new CanReply(new int[]{CbusConstants.CBUS_ACON, 0x00, 0x01, 0x00, 1}), now - 20_000_000L);
        send.receiveAt(new CanReply(new int[]{CbusConstants.CBUS_ACOF, 0x00, 0x01, 0x00, 2}), now);
        send.receiveAt(new CanReply(new int[]{CbusConstants.CBUS_ACON, 0x00, 0x01, 0x00, 30}), now + 30_000_000L);
        JUnitUtil.waitFor(() -> tcis.inbound.size() == 4, "all frames delivered");
        int[] expected = {1, 2, 30, 40};
        for (int i = 0; i < expected.length; i++) {
            assertEquals(expected[i], tcis.inbound.elementAt(i).getElement(4), "position " + i);
        }
        assertEquals(now - 20_000_000L, tcis.inbound.elementAt(0).getReceivedTime(), "the time given");
        assertEquals(now, tcis.inbound.elementAt(1).getReceivedTime());
        assertEquals(now + 30_000_000L, tcis.inbound.elementAt(2).getReceivedTime());
        assertTrue(tcis.outbound.isEmpty(), "only incoming");
        for (int i = 1; i < tcis.inbound.size(); i++) {
            assertTrue(tcis.inbound.elementAt(i).getReceivedTime() - tcis.inbound.elementAt(i - 1).getReceivedTime() >= 0,
                "received times in delivery order, at " + i);
        }
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        memo = new CanSystemConnectionMemo();
        tcis = new TrafficControllerScaffold();
        memo.setTrafficController(tcis);
        send = new CbusSimulatedSend(memo);
    }

    @AfterEach
    public void tearDown() {
        send = null;
        memo.dispose();
        tcis.terminateThreads();
        tcis = null;
        memo = null;
        JUnitUtil.tearDown();
    }
}
