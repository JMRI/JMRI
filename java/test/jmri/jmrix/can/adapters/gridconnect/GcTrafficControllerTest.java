package jmri.jmrix.can.adapters.gridconnect;

import jmri.jmrix.AbstractPortControllerScaffold;
import jmri.jmrix.can.*;
import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

/**
 * Tests for GcTrafficController.
 * @author Paul Bender Copyright (C) 2016
 */
public class GcTrafficControllerTest extends jmri.jmrix.can.TrafficControllerTest {

    @Timeout(10)
    @Test
    public void testIncomingRtr() throws java.io.IOException {
        
        DummyCanListener listener = new DummyCanListener((GcTrafficController)tc);
        CanSystemConnectionMemo memo = new CanSystemConnectionMemo();
        
        AbstractPortControllerScaffold pcs = new AbstractPortControllerScaffold(memo);
        tc.connectPort(pcs);
        pcs.getRedirectedToInputStream().writeBytes(":SB0F0R;"); // RTR CanFrame SB0F0R
        
        JUnitUtil.waitFor(() -> {
            return !listener.getReplies().isEmpty();
        },"rtr reply did not happen");
        
        CanReply reply = listener.getReplies().get(0);
        Assertions.assertNotNull(reply, "CanReply generated and not null");
        Assertions.assertEquals(0,reply.getNumDataElements(),"no data elements");
        Assertions.assertTrue(reply.isRtr(),"rtr flag set");
        
        listener.dispose();
        tc.disconnectPort(pcs);
        pcs.dispose();
        
    }

    // A frame whose ';' is lost must not be merged with the following frame,
    // the partial frame is discarded and the following frame received intact.
    @Timeout(10)
    @Test
    public void testResyncOnLostTerminator() throws java.io.IOException {

        DummyCanListener listener = new DummyCanListener((GcTrafficController)tc);
        CanSystemConnectionMemo memo = new CanSystemConnectionMemo();

        AbstractPortControllerScaffold pcs = new AbstractPortControllerScaffold(memo);
        tc.connectPort(pcs);
        // first frame truncated, no ';', then a complete ACON N2 E2
        pcs.getRedirectedToInputStream().writeBytes(":SB020N91000100:SB020N9000020002;");

        JUnitUtil.waitFor(() -> {
            return !listener.getReplies().isEmpty();
        },"reply did not happen");

        CanReply reply = listener.getReplies().get(0);
        Assertions.assertEquals(5,reply.getNumDataElements(),"5 data elements from the complete frame");
        Assertions.assertEquals(0x90,reply.getElement(0),"ACON");
        Assertions.assertEquals(0x00,reply.getElement(1),"NN hi");
        Assertions.assertEquals(0x02,reply.getElement(2),"NN lo");
        Assertions.assertEquals(0x00,reply.getElement(3),"EN hi");
        Assertions.assertEquals(0x02,reply.getElement(4),"EN lo");
        Assertions.assertEquals(1,listener.getReplies().size(),"only the complete frame passed on");
        jmri.util.JUnitAppender.assertWarnMessageStartsWith("Incomplete frame discarded");

        listener.dispose();
        tc.disconnectPort(pcs);
        pcs.dispose();
    }

    // A frame is stamped when the receive thread has read it, not when it
    // reaches listeners on the layout thread, which may be busy.
    @Timeout(10)
    @Test
    public void testIncomingFrameStampedWhenRead() throws Exception {

        DummyCanListener listener = new DummyCanListener((GcTrafficController)tc);
        CanSystemConnectionMemo memo = new CanSystemConnectionMemo();
        AbstractPortControllerScaffold pcs = new AbstractPortControllerScaffold(memo);
        tc.connectPort(pcs);

        java.util.concurrent.CountDownLatch busy = new java.util.concurrent.CountDownLatch(1);
        java.util.concurrent.CountDownLatch release = new java.util.concurrent.CountDownLatch(1);
        javax.swing.SwingUtilities.invokeLater(() -> {
            busy.countDown();
            try {
                release.await();
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
            }
        });
        long released;
        long before;
        try {
            Assertions.assertTrue(busy.await(5, java.util.concurrent.TimeUnit.SECONDS), "layout thread busy");
            before = System.nanoTime();
            pcs.getRedirectedToInputStream().writeBytes(":SB020N9000020002;"); // ACON N2 E2
            pcs.getRedirectedToInputStream().flush(); // wake the receive thread, as a real port would
            Thread.sleep(500); // the receive thread reads it meanwhile
            Assertions.assertTrue(listener.getReplies().isEmpty(), "not passed on while the layout thread is busy");
            released = System.nanoTime();
        } finally {
            release.countDown();
        }

        JUnitUtil.waitFor(() -> {
            return !listener.getReplies().isEmpty();
        },"reply did not happen");

        CanReply reply = listener.getReplies().get(0);
        Assertions.assertTrue(reply.hasReceivedTime(), "received time recorded");
        Assertions.assertTrue(reply.getReceivedTime() >= before, "stamped after it was sent");
        Assertions.assertTrue(reply.getReceivedTime() < released,
                "stamped by the receive thread, before the layout thread was free");
        Assertions.assertEquals(reply.getReceivedTime(), new CanReply(reply).getReceivedTime(), "kept by a copy");

        listener.dispose();
        tc.disconnectPort(pcs);
        pcs.dispose();
    }

    // The sender's frame, not its GridConnect encoding, is stamped once the
    // write has returned, and its sent listener is told on the layout thread.
    @Timeout(10)
    @Test
    public void testOutgoingFrameStampedWhenWritten() throws Exception {

        DummyCanListener monitor = new DummyCanListener((GcTrafficController)tc);
        java.util.concurrent.CountDownLatch writing = new java.util.concurrent.CountDownLatch(1);
        java.util.concurrent.CountDownLatch release = new java.util.concurrent.CountDownLatch(1);
        java.io.ByteArrayOutputStream written = new java.io.ByteArrayOutputStream();
        java.io.OutputStream gate = new java.io.OutputStream() {
            @Override
            public void write(int b) throws java.io.IOException {
                write(new byte[]{(byte) b}, 0, 1);
            }

            @Override
            public void write(byte[] b, int off, int len) throws java.io.IOException {
                writing.countDown();
                try {
                    release.await();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw new java.io.IOException(e);
                }
                written.write(b, off, len);
            }
        };
        CanSystemConnectionMemo memo = new CanSystemConnectionMemo();
        AbstractPortControllerScaffold pcs = new AbstractPortControllerScaffold(memo) {
            private final java.io.DataOutputStream out = new java.io.DataOutputStream(gate);

            @Override
            public java.io.DataOutputStream getOutputStream() {
                return out;
            }
        };
        tc.connectPort(pcs);

        java.util.concurrent.atomic.AtomicLong toldTime = new java.util.concurrent.atomic.AtomicLong();
        java.util.concurrent.atomic.AtomicBoolean told = new java.util.concurrent.atomic.AtomicBoolean(false);
        java.util.concurrent.atomic.AtomicBoolean onLayoutThread = new java.util.concurrent.atomic.AtomicBoolean(false);
        CanMessage m = new CanMessage(new int[]{0x0D}, 0x7A); // QNN
        m.setSentListener((message, timeNanos) -> {
            Assertions.assertSame(m, message, "told of the sender's frame");
            onLayoutThread.set(jmri.util.ThreadingUtil.isLayoutThread());
            toldTime.set(timeNanos);
            told.set(true);
        });

        long released;
        try {
            ((GcTrafficController)tc).sendCanMessage(m, null);
            Assertions.assertTrue(writing.await(5, java.util.concurrent.TimeUnit.SECONDS), "write started");
            Assertions.assertFalse(m.hasSentTime(), "not stamped while the write is under way");
            released = System.nanoTime();
        } finally {
            release.countDown();
        }

        JUnitUtil.waitFor(told::get, "sent listener told");
        String frame = written.toString(java.nio.charset.StandardCharsets.US_ASCII);
        Assertions.assertTrue(frame.startsWith(":S") && frame.endsWith("N0D;"), "QNN written: " + frame);
        Assertions.assertTrue(m.hasSentTime(), "sender's frame stamped");
        Assertions.assertTrue(m.getSentTime() >= released, "stamped after the write returned");
        Assertions.assertEquals(m.getSentTime(), toldTime.get(), "listener told the same time");
        Assertions.assertTrue(onLayoutThread.get(), "listener told on the layout thread");
        Assertions.assertSame(m, monitor.getMessages().get(0), "monitors were given the same frame");

        monitor.dispose();
        tc.disconnectPort(pcs);
        pcs.dispose();
        gate.close();
    }

    @Override
    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp(); 
        JUnitUtil.resetInstanceManager();
        tc = new GcTrafficController();
    }

    @Override
    @AfterEach
    public void tearDown(){
        tc.terminateThreads();
        tc = null;
        JUnitUtil.tearDown();
 
    }

    // A listener which sent the last frame and was then removed must not
    // receive incoming frames. Only registered listeners get them.
    @Timeout(10)
    @Test
    public void testRemovedSenderReceivesNothing() throws java.io.IOException {

        DummyCanListener monitor = new DummyCanListener((GcTrafficController)tc);
        DummyCanListener sender = new DummyCanListener((GcTrafficController)tc);
        CanSystemConnectionMemo memo = new CanSystemConnectionMemo();

        AbstractPortControllerScaffold pcs = new AbstractPortControllerScaffold(memo);
        tc.connectPort(pcs);

        // sender sends a frame, which makes it the last sender, then goes away
        CanMessage m = new CanMessage(new int[]{0x0D}, 0x7A); // QNN
        ((GcTrafficController)tc).sendCanMessage(m, sender);
        JUnitUtil.waitFor(() -> {
            return !monitor.getMessages().isEmpty();
        },"frame not sent");
        sender.dispose();

        pcs.getRedirectedToInputStream().writeBytes(":SB020N9000020002;"); // ACON N2 E2

        JUnitUtil.waitFor(() -> {
            return !monitor.getReplies().isEmpty();
        },"reply did not reach the registered listener");
        JUnitUtil.waitFor(50); // let any delivery to the removed sender happen
        Assertions.assertEquals(1, monitor.getReplies().size(), "registered listener receives the frame");
        Assertions.assertTrue(sender.getReplies().isEmpty(), "removed sender receives nothing");

        monitor.dispose();
        tc.disconnectPort(pcs);
        pcs.dispose();
    }

}
