package jmri.jmrix.loconet;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import jmri.jmrix.loconet.Intellibox.IBLnPacketizer;
import jmri.jmrix.loconet.loconetovertcp.LnOverTcpPacketizer;
import jmri.jmrix.loconet.uhlenbrock.UhlenbrockPacketizer;
import jmri.jmrix.loconet.uhlenbrock.UhlenbrockSystemConnectionMemo;
import jmri.util.JUnitAppender;
import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

/**
 * Tests that each LocoNet receive loop records when each message was
 * received: when its receive thread has read it, not when it reaches
 * listeners on the layout thread, which may be busy.
 *
 * @author Keith Bruce Copyright (C) 2026
 */
public class LnPacketizerReceivedTimeTest {

    private static final byte[] INPUT_REP = {(byte) 0xB2, 0x15, 0x70, 0x28}; // LS44 high

    private LocoNetSystemConnectionMemo memo;
    private LnPacketizer packets;
    private PipedInputStream in;
    private PipedOutputStream toIn;

    @Test
    @Timeout(20)
    public void testLnPacketizer() throws Exception {
        assertStampedWhenRead(new LnPacketizer(memo), INPUT_REP);
    }

    @Test
    @Timeout(20)
    public void testLnPacketizerStrict() throws Exception {
        assertStampedWhenRead(new LnPacketizerStrict(memo), INPUT_REP);
    }

    @Test
    @Timeout(20)
    public void testIntellibox() throws Exception {
        assertStampedWhenRead(new IBLnPacketizer(), INPUT_REP);
    }

    @Test
    @Timeout(20)
    public void testUhlenbrock() throws Exception {
        assertStampedWhenRead(new UhlenbrockPacketizer(new UhlenbrockSystemConnectionMemo()), INPUT_REP);
    }

    @Test
    @Timeout(20)
    public void testLocoNetOverTcp() throws Exception {
        assertStampedWhenRead(new LnOverTcpPacketizer(memo),
                "RECEIVE B2 15 70 28\r\n".getBytes(StandardCharsets.US_ASCII));
    }

    private void assertStampedWhenRead(LnPacketizer p, byte[] input) throws Exception {
        packets = p;
        packets.istream = new DataInputStream(in);
        packets.ostream = new DataOutputStream(new ByteArrayOutputStream());
        List<LocoNetMessage> heard = new CopyOnWriteArrayList<>();
        packets.addLocoNetListener(~0, heard::add);
        packets.startThreads();

        // keep the layout thread busy while the message is read
        CountDownLatch busy = new CountDownLatch(1);
        CountDownLatch release = new CountDownLatch(1);
        javax.swing.SwingUtilities.invokeLater(() -> {
            busy.countDown();
            try {
                release.await();
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
            }
        });
        long before;
        long released;
        try {
            assertTrue(busy.await(5, TimeUnit.SECONDS), "layout thread busy");
            before = System.nanoTime();
            toIn.write(input);
            toIn.flush();
            JUnitUtil.waitFor(() -> available() == 0, "the receive thread has read the message");
            Thread.sleep(100); // and finished it, on the receive thread
            assertTrue(heard.isEmpty(), "not passed on while the layout thread is busy");
            released = System.nanoTime();
        } finally {
            release.countDown();
        }

        JUnitUtil.waitFor(() -> !heard.isEmpty(), "message passed on");
        LocoNetMessage m = heard.get(0);
        assertEquals(new LocoNetMessage(new int[]{0xB2, 0x15, 0x70, 0x28}), m, "the message sent");
        assertTrue(m.hasReceivedTime(), "received time recorded");
        assertTrue(m.getReceivedTime() - before >= 0, "stamped after it was sent");
        assertTrue(released - m.getReceivedTime() > 0,
                "stamped by the receive thread, before the layout thread was free");
    }

    private int available() {
        try {
            return in.available();
        } catch (IOException ex) {
            return -1;
        }
    }

    @BeforeEach
    public void setUp() throws IOException {
        JUnitUtil.setUp();
        memo = new LocoNetSystemConnectionMemo();
        in = new PipedInputStream();
        toIn = new PipedOutputStream(in);
    }

    @AfterEach
    public void tearDown() throws IOException {
        if (packets != null) {
            packets.terminateThreads();
        }
        toIn.close();
        packets = null;
        memo = null;
        JUnitAppender.suppressErrorMessage("transmitLoop interrupted");
        JUnitUtil.tearDown();
    }
}
