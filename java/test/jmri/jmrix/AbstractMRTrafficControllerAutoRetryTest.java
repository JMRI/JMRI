package jmri.jmrix;

import java.util.LinkedList;
import java.util.concurrent.atomic.AtomicInteger;

import jmri.util.JUnitAppender;
import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for the automatic retry of a message whose reply is a
 * retransmittable error (XpressNet "command station busy", for example):
 * the message is put back on the queue under the same lock that every
 * other change to the queue takes.
 *
 * @author Keith Bruce Copyright (C) 2026
 */
public class AbstractMRTrafficControllerAutoRetryTest {

    private AbstractMRTrafficController tc;
    private final AtomicInteger busyReplies = new AtomicInteger(1);
    private volatile boolean changedWithoutLock = false;

    /** A queue that notes any change made without the traffic controller's lock. */
    private class LockCheckingList<T> extends LinkedList<T> {

        @Override
        public void addFirst(T e) {
            check();
            super.addFirst(e);
        }

        @Override
        public void addLast(T e) {
            check();
            super.addLast(e);
        }

        @Override
        public T removeFirst() {
            check();
            return super.removeFirst();
        }

        private void check() {
            if (!Thread.holdsLock(tc)) {
                changedWithoutLock = true;
            }
        }
    }

    @Test
    public void testRetriedMessageQueuedUnderTheLock() throws Exception {
        AbstractSerialPortControllerScaffold port = new AbstractSerialPortControllerScaffold(
                Mockito.mock(jmri.SystemConnectionMemo.class));
        tc.connectPort(port);
        AbstractMRMessage m = new AbstractMRMessage(1) {
        };
        m.setElement(0, 0x21);
        m.setTimeout(5000);
        tc.sendMessage(m, Mockito.mock(AbstractMRListener.class));

        assertEquals(0x21, port.tostream.read(), "sent");
        assertEquals(0x0D, port.tostream.read(), "with the default trailer");
        port.tistream.write(0x01); // a reply saying "busy, send again"
        assertEquals(0x21, port.tostream.read(), "sent again");
        assertEquals(0x0D, port.tostream.read());
        port.tistream.write(0x01); // this time a plain reply
        assertTrue(JUnitUtil.waitFor(() -> tc.mCurrentState == AbstractMRTrafficController.IDLESTATE));
        JUnitAppender.assertErrorMessageStartsWith("Automatic Recovery from Error Message");

        assertFalse(changedWithoutLock, "the queues are only changed under the traffic controller's lock");
    }

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
                boolean busy = busyReplies.getAndDecrement() > 0;
                return new AbstractMRReply() {
                    @Override
                    protected int skipPrefix(int index) {
                        return index;
                    }

                    @Override
                    public boolean isRetransmittableErrorMsg() {
                        return busy;
                    }
                };
            }

            @Override
            protected boolean endOfMessage(AbstractMRReply r) {
                return true;
            }
        };
        tc.msgQueue = new LockCheckingList<>();
        tc.listenerQueue = new LockCheckingList<>();
    }

    @AfterEach
    public void tearDown() {
        tc.terminateThreads();
        tc = null;
        JUnitUtil.tearDown();
    }
}
