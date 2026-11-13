package jmri.jmrix;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

import jmri.SystemConnectionMemo;
import jmri.util.JUnitAppender;
import jmri.util.JUnitUtil;
import jmri.util.ThreadingUtil;

import org.junit.jupiter.api.*;
import org.mockito.Mockito;

/**
 * Tests that AbstractMRTrafficController records when a message is written to
 * the connection, and tells the message's sent listener.
 *
 * @author Keith Bruce Copyright (C) 2026
 */
public class AbstractMRTrafficControllerSentTimeTest {

    // The time is taken once the write has returned, not before or during
    // it; the listener is told that time, on the layout thread.
    @Timeout(10)
    @Test
    public void testStampedWhenWriteReturns() throws Exception {
        CountDownLatch writing = new CountDownLatch(1);
        CountDownLatch release = new CountDownLatch(1);
        ByteArrayOutputStream written = new ByteArrayOutputStream();
        connect(new OutputStream() {
            @Override
            public void write(int b) throws IOException {
                write(new byte[]{(byte) b}, 0, 1);
            }

            @Override
            public void write(byte[] b, int off, int len) throws IOException {
                writing.countDown();
                try {
                    release.await();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw new IOException(e);
                }
                written.write(b, off, len);
            }
        });

        AtomicLong toldTime = new AtomicLong();
        AtomicBoolean told = new AtomicBoolean(false);
        AtomicBoolean onLayoutThread = new AtomicBoolean(false);
        AbstractMRMessage m = newMessage();
        m.setSentListener((message, timeNanos) -> {
            Assertions.assertSame(m, message, "told of its own message");
            onLayoutThread.set(ThreadingUtil.isLayoutThread());
            toldTime.set(timeNanos);
            told.set(true);
        });

        tc.sendMessage(m, null);
        Assertions.assertTrue(writing.await(5, TimeUnit.SECONDS), "write started");
        Assertions.assertFalse(m.hasSentTime(), "not stamped while the write is under way");
        long released = System.nanoTime();
        release.countDown();

        JUnitUtil.waitFor(told::get, "sent listener told");
        byte[] bytes = written.toByteArray();
        Assertions.assertTrue(bytes.length >= 3 && bytes[0] == 0x11 && bytes[1] == 0x22 && bytes[2] == 0x33,
                "message written");
        Assertions.assertTrue(m.hasSentTime(), "stamped");
        Assertions.assertTrue(m.getSentTime() >= released, "stamped after the write returned");
        Assertions.assertEquals(m.getSentTime(), toldTime.get(), "listener told the same time");
        Assertions.assertTrue(onLayoutThread.get(), "listener told on the layout thread");
    }

    // A message that is not written gets no time, and its listener is not told.
    @Timeout(10)
    @Test
    public void testNotStampedWhenWriteFails() throws Exception {
        connect(new OutputStream() {
            @Override
            public void write(int b) throws IOException {
                throw new IOException("test: port gone");
            }
        });

        AtomicBoolean told = new AtomicBoolean(false);
        AbstractMRMessage m = newMessage();
        m.setSentListener((message, timeNanos) -> told.set(true));

        tc.sendMessage(m, null);
        JUnitUtil.waitFor(() -> JUnitAppender.checkForMessageStartingWith("sendMessage: Exception: In") != null,
                "write failed");
        ThreadingUtil.runOnLayout(() -> {
        }); // anything queued for the layout thread before this has run

        Assertions.assertFalse(m.hasSentTime(), "not stamped");
        Assertions.assertFalse(told.get(), "listener not told");
    }

    private void connect(OutputStream out) throws IOException {
        AbstractPortControllerScaffold port = new AbstractPortControllerScaffold(Mockito.mock(SystemConnectionMemo.class)) {
            private final DataOutputStream dout = new DataOutputStream(out);

            @Override
            public DataOutputStream getOutputStream() {
                return dout;
            }
        };
        tc.connectPort(port);
    }

    private AbstractMRMessage newMessage() {
        AbstractMRMessage m = new AbstractMRMessage(3) {
            @Override
            public boolean replyExpected() {
                return false;
            }
        };
        m.setElement(0, 0x11);
        m.setElement(1, 0x22);
        m.setElement(2, 0x33);
        return m;
    }

    private AbstractMRTrafficController tc;

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetInstanceManager();
        tc = new AbstractMRTrafficController() {

            @Override
            protected void forwardMessage(AbstractMRListener client, AbstractMRMessage m) {
            }

            @Override
            protected AbstractMRMessage pollMessage() {
                return null;
            }

            @Override
            protected AbstractMRListener pollReplyHandler() {
                return null;
            }

            @Override
            protected AbstractMRMessage enterProgMode() {
                return null;
            }

            @Override
            protected AbstractMRMessage enterNormalMode() {
                return null;
            }

            @Override
            protected void forwardReply(AbstractMRListener client, AbstractMRReply m) {
            }

            @Override
            protected AbstractMRReply newReply() {
                // the receive thread runs, waiting for input that never comes
                return new AbstractMRReply() {
                    @Override
                    protected int skipPrefix(int index) {
                        return index;
                    }
                };
            }

            @Override
            protected boolean endOfMessage(AbstractMRReply r) {
                return true;
            }
        };
    }

    @AfterEach
    public void tearDown() {
        tc.terminateThreads();
        JUnitUtil.tearDown();
    }

}
