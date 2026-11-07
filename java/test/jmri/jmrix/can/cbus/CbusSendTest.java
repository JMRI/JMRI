package jmri.jmrix.can.cbus;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import jmri.jmrix.can.CanReply;
import jmri.jmrix.can.CanSystemConnectionMemo;
import jmri.jmrix.can.TrafficControllerScaffold;
import jmri.util.JUnitUtil;
import jmri.util.ThreadingUtil;
import jmri.util.swing.TextAreaFIFO;

import org.junit.jupiter.api.*;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 * @author Steve Young Copyright (C) 2019
 */
public class CbusSendTest {

    private TrafficControllerScaffold tcis;
    private CanSystemConnectionMemo memo;
    private CbusSend send;
    private TextAreaFIFO ta;

    @Test
    public void testCTor() {
        assertNotNull( send, "exists");
    }

    @Test
    public void testnodeExitLearnEvMode() {
        send.nodeExitLearnEvMode(54321);
        checknodeExitLearnEvMode();
    }

    @Test
    public void testnodeEnterLearnEvMode() {
        send.nodeEnterLearnEvMode(54321);
        checknodeEnterLearnEvMode();
    }

    @Test
    public void testnodeSetNodeNumber() {
        send.nodeSetNodeNumber(12345);
        checknodeSetNodeNumber();
    }
    
    @Test
    public void testnodeRequestParamSetup() {
        send.nodeRequestParamSetup();
        checknodeRequestParamSetup();
    }    

    @Test
    public void testnodeTeachEventLearnMode() {
        send.nodeTeachEventLearnMode(54321,12345,8,255);
        checknodeTeachEventLearnMode();
    }   

    @Test
    public void testnodenodeUnlearnEvent() {
        send.nodeUnlearnEvent(0XFD,0xee);
        checknodeUnlearnEvent();
    }       

    @Test
    public void testrEVAL() {
        send.rEVAL(987,123,6541);
        checkrEVAL();
    }  

    @Test
    public void testrQNPN() {
        send.rQNPN(555,777);
        checkrQNPN();
    }  

    @Test
    public void testsearchForNodes() {
        send.searchForNodes();
        checksearchForNodes();
    }  
    
    @Test
    public void testnVRD() {
        send.nVRD(4321,123);
        checknVRD();
    }

    @Test
    public void testnVSET() {
        send.nVSET(65432,123,1);
        checknVSET();
    }

    @Test
    public void testrQEVN() {
        send.rQEVN(12345);
        checkrQEVN();
    }

    @Test
    public void testnERD() {
        send.nERD(44444);
        checknERD();
    }

    @Test
    public void testaRST() {
        send.aRST();
        checkaRST();
    }
    
    @Test
    public void testCbusEnumerationSent() {
        send.eNUM(1234);
        checkeNUM();
    }
    
    @Test
    public void testcANID() {
        send.cANID(6543,77);
        checkcANID();
    }

    @Test
    public void testnNCLR() {
        send.nNCLR(4321);
        checknNCLR();
    }

    @Test
    public void testrQmn() {
        send.rQmn();
        checkrQmn();
    }
    
    @Test
    public void testTextAreaAdd2() {
        ta = new TextAreaFIFO(9);
        CbusSend tasend = new CbusSend(memo,ta);
        assertNotNull( tasend, "exists");
        tasend.nodeExitLearnEvMode(22222);
        assertTrue( (ta.getText()).contains("2 exit learn mode."),
            "textarea is updated 2");
        
    }


    @Test
    public void testTextAreaAdd3() {
        ta = new TextAreaFIFO(9);
        CbusSend tasend = new CbusSend(memo,ta);
        assertNotNull( tasend, "exists");
        tasend.nodeEnterLearnEvMode(33333);
        assertTrue( (ta.getText()).contains("3 enter learn mode."), "textarea is updated 3");
    }

    @Test
    public void testSendWithDelayGivesReplyItsReceivedTime() throws InterruptedException {
        CountDownLatch stamped = new CountDownLatch(1);
        AtomicLong stampTime = new AtomicLong();
        CanReply r = new CanReply(new int[]{CbusConstants.CBUS_ACON, 0x00, 0x01, 0x00, 0x02}) {
            @Override
            public void setReceivedTime(long timeNanos) {
                super.setReceivedTime(timeNanos);
                stampTime.set(timeNanos);
                stamped.countDown();
            }
        };

        // hold the layout thread, so the reply can only be stamped before it reaches it
        CountDownLatch layoutThreadHeld = new CountDownLatch(1);
        CountDownLatch releaseLayoutThread = new CountDownLatch(1);
        ThreadingUtil.runOnLayoutEventually(() -> {
            layoutThreadHeld.countDown();
            try {
                releaseLayoutThread.await();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });
        long releasedAt;
        try {
            assertTrue(layoutThreadHeld.await(10, TimeUnit.SECONDS), "layout thread held");
            long before = System.nanoTime();
            send.sendWithDelay(r, true, false, 5);
            // the timeout only stops a failing test hanging; it is not a measurement
            assertTrue(stamped.await(10, TimeUnit.SECONDS),
                "stamped while the layout thread is busy");
            assertTrue(tcis.inbound.isEmpty(), "not yet delivered");
            assertTrue(stampTime.get() >= before, "stamped after it was sent");
        } finally {
            releasedAt = System.nanoTime();
            releaseLayoutThread.countDown();
        }

        JUnitUtil.waitFor(() -> tcis.inbound.size() == 1, "reply delivered");
        CanReply delivered = tcis.inbound.elementAt(0);
        assertTrue(delivered.hasReceivedTime(), "delivered with a received time");
        assertEquals(stampTime.get(), delivered.getReceivedTime(), "the time it was stamped with");
        assertTrue(delivered.getReceivedTime() < releasedAt,
            "received before it waited for the layout thread");
    }

    @Test
    public void testSendWithDelaySameDelayArrivesInOrderSent() {
        int count = 50;
        for (int i = 0; i < count; i++) {
            send.sendWithDelay(new CanReply(new int[]{CbusConstants.CBUS_ACON, 0x00, 0x01, 0x00, i}), true, false, 5);
        }
        JUnitUtil.waitFor(() -> tcis.inbound.size() == count, "all frames delivered");
        for (int i = 0; i < count; i++) {
            assertEquals(i, tcis.inbound.elementAt(i).getElement(4), "frame " + i + " in the order sent");
        }
        assertReceivedTimesInOrder();
    }

    @Test
    public void testSendWithDelayArrivesInOrderDue() {
        // Sent with delays of 20, 10, 10 and 0 ms. Each frame is due its delay after it was sent,
        // and the times each send began and ended bound that. A busy machine may pause between
        // the sends, so the order of two frames is checked where those bounds settle it, and
        // frames with the same delay must keep the order they were sent in.
        int[] delays = {20, 10, 10, 0};
        long[] began = new long[delays.length];
        long[] ended = new long[delays.length];
        for (int i = 0; i < delays.length; i++) {
            began[i] = System.nanoTime();
            send.sendWithDelay(new CanReply(new int[]{CbusConstants.CBUS_ACON, 0x00, 0x01, 0x00, i}), true, false,
                delays[i]);
            ended[i] = System.nanoTime();
        }
        JUnitUtil.waitFor(() -> tcis.inbound.size() == delays.length, "all frames delivered");
        int[] position = new int[delays.length];
        for (int p = 0; p < delays.length; p++) {
            position[tcis.inbound.elementAt(p).getElement(4)] = p;
        }
        int settled = 0;
        for (int i = 0; i < delays.length; i++) {
            for (int j = i + 1; j < delays.length; j++) {
                long latestI = ended[i] + TimeUnit.MILLISECONDS.toNanos(delays[i]);
                long earliestI = began[i] + TimeUnit.MILLISECONDS.toNanos(delays[i]);
                long latestJ = ended[j] + TimeUnit.MILLISECONDS.toNanos(delays[j]);
                long earliestJ = began[j] + TimeUnit.MILLISECONDS.toNanos(delays[j]);
                if (delays[i] == delays[j] || latestI - earliestJ < 0) {
                    assertTrue(position[i] < position[j], "frame " + i + " due before frame " + j);
                    settled++;
                } else if (latestJ - earliestI < 0) {
                    assertTrue(position[j] < position[i], "frame " + j + " due before frame " + i);
                    settled++;
                }
            }
        }
        assertTrue(settled > 0, "at least the frames with the same delay are in order");
        assertReceivedTimesInOrder();
    }

    @Test
    public void testSendAtGivesTheTimeAndTakesItsPlace() {
        long now = System.nanoTime();
        // a frame with a delay of 40 ms, then frames received 20 ms ago, now, and 30 ms from now
        send.sendWithDelay(new CanReply(new int[]{CbusConstants.CBUS_ACON, 0x00, 0x01, 0x00, 40}), true, false, 40);
        send.sendAt(new CanReply(new int[]{CbusConstants.CBUS_ACON, 0x00, 0x01, 0x00, 1}), now - 20_000_000L);
        send.sendAt(new CanReply(new int[]{CbusConstants.CBUS_ACOF, 0x00, 0x01, 0x00, 2}), now);
        send.sendAt(new CanReply(new int[]{CbusConstants.CBUS_ACON, 0x00, 0x01, 0x00, 30}), now + 30_000_000L);
        JUnitUtil.waitFor(() -> tcis.inbound.size() == 4, "all frames delivered");
        int[] expected = {1, 2, 30, 40};
        for (int i = 0; i < expected.length; i++) {
            assertEquals(expected[i], tcis.inbound.elementAt(i).getElement(4), "position " + i);
        }
        assertEquals(now - 20_000_000L, tcis.inbound.elementAt(0).getReceivedTime(), "the time given");
        assertEquals(now, tcis.inbound.elementAt(1).getReceivedTime());
        assertEquals(now + 30_000_000L, tcis.inbound.elementAt(2).getReceivedTime());
        assertTrue(tcis.outbound.isEmpty(), "only incoming");
        assertReceivedTimesInOrder();
    }

    private void assertReceivedTimesInOrder() {
        for (int i = 1; i < tcis.inbound.size(); i++) {
            assertTrue(tcis.inbound.elementAt(i).getReceivedTime() - tcis.inbound.elementAt(i - 1).getReceivedTime() >= 0,
                "received times in delivery order, at " + i);
        }
    }

    @Test
    public void testSendWithDelayOutgoingOnlyIsNotStamped() {
        CanReply r = new CanReply(new int[]{CbusConstants.CBUS_ACOF, 0x00, 0x01, 0x00, 0x02});
        send.sendWithDelay(r, false, true, 5);
        JUnitUtil.waitFor(() -> tcis.outbound.size() == 1, "message sent");
        assertTrue(tcis.inbound.isEmpty(), "not sent as incoming");
        assertFalse(r.hasReceivedTime(), "an outgoing frame is not received");
    }

    private void checknodeExitLearnEvMode() {
        assertEquals( "[5f8] 54 D4 31",
        tcis.outbound.elementAt(tcis.outbound.size() - 1).toString(),
        "Node exit learn sent");
    }

    private void checknodeEnterLearnEvMode() {
        assertEquals( "[5f8] 53 D4 31",
        tcis.outbound.elementAt(tcis.outbound.size() - 1).toString(),
        "Node enter learn sent");
    }

    private void checknodeSetNodeNumber() {
        assertEquals( "[5f8] 42 30 39",
        tcis.outbound.elementAt(tcis.outbound.size() - 1).toString(),
        "Node set nn sent");
    }
    
    private void checknodeRequestParamSetup() {
        assertEquals( "[5f8] 10",
            tcis.outbound.elementAt(tcis.outbound.size() - 1).toString(),
            "Node request param setup sent");
    }

    private void checknodeTeachEventLearnMode() {
        assertEquals( "[5f8] D2 D4 31 30 39 08 FF",
            tcis.outbound.elementAt(tcis.outbound.size() - 1).toString(),
            "Node Teach Event LearnMode sent");
    }

    private void checknodeUnlearnEvent() {
        assertEquals( "[5f8] 95 00 FD 00 EE",
            tcis.outbound.elementAt(tcis.outbound.size() - 1).toString(),
            "Node Unlearn Event sent");
    }

    private void checkrEVAL() {
        assertEquals( "[5f8] 9C 03 DB 7B 8D",
            tcis.outbound.elementAt(tcis.outbound.size() - 1).toString(),
            "rEVAL Event sent");
    }

    private void checkrQNPN() {
        assertEquals( "[5f8] 73 02 2B 09",
            tcis.outbound.elementAt(tcis.outbound.size() - 1).toString(),
            "rQNPN Event sent");
    }

    private void checksearchForNodes() {
        assertEquals( "[5f8] 0D",
            tcis.outbound.elementAt(tcis.outbound.size() - 1).toString(),
            "searchForNodes sent");
    }

    private void checknVRD() {
        assertEquals( "[5f8] 71 10 E1 7B",
            tcis.outbound.elementAt(tcis.outbound.size() - 1).toString(),
            "nVRD sent");
    }

    private void checknVSET() {
        assertEquals( "[5f8] 96 FF 98 7B 01",
            tcis.outbound.elementAt(tcis.outbound.size() - 1).toString(),
            "nVSET sent");
    }

    private void checkrQEVN() {
        assertEquals( "[5f8] 58 30 39",
            tcis.outbound.elementAt(tcis.outbound.size() - 1).toString(),
            "rQEVN sent");
    }

    private void checknERD() {
        assertEquals( "[5f8] 57 AD 9C",
            tcis.outbound.elementAt(tcis.outbound.size() - 1).toString(),
            "nERD sent");
    }

    private void checkaRST() {
        assertEquals( "[5f8] 07",
            tcis.outbound.elementAt(tcis.outbound.size() - 1).toString(),
            "aRST sent");
    }

    private void checkeNUM() {
        assertEquals( "[5f8] 5D 04 D2",
            tcis.outbound.elementAt(tcis.outbound.size() - 1).toString(),
            "eNUM sent");
    }

    private void checkcANID() {
        assertEquals( "[5f8] 75 19 8F 4D",
            tcis.outbound.elementAt(tcis.outbound.size() - 1).toString(),
            "cANID sent");
    }

    private void checknNCLR() {
        assertEquals( "[5f8] 55 10 E1",
            tcis.outbound.elementAt(tcis.outbound.size() - 1).toString(),
            "nNCLR sent");
    }

    private void checkrQmn() {
        assertEquals( "[5f8] 11",
            tcis.outbound.elementAt(tcis.outbound.size() - 1).toString(),
            "rQmn sent");
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        memo = new CanSystemConnectionMemo();
        tcis = new TrafficControllerScaffold();
        memo.setTrafficController(tcis);
        send = new CbusSend(memo,null);
    }

    @AfterEach
    public void tearDown() {
        send = null;
        memo.dispose();
        tcis.terminateThreads();
        tcis = null;
        memo = null;
        ta = null;
        JUnitUtil.tearDown();

    }

    // private static final Logger log = LoggerFactory.getLogger(CbusSendTest.class);
}
