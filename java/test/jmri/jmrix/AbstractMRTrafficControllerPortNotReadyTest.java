package jmri.jmrix;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.util.concurrent.atomic.AtomicBoolean;

import jmri.SystemConnectionMemo;
import jmri.util.JUnitAppender;
import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;
import org.mockito.Mockito;

/**
 * Tests that AbstractMRTrafficController reports a message it drops because
 * the port was not ready to send when the message's retries ran out.
 *
 * @author Keith Bruce Copyright (C) 2026
 */
public class AbstractMRTrafficControllerPortNotReadyTest {

    // The first message dropped is a warning; more while the port stays
    // unready are not; once a message has been written, the next one
    // dropped is a warning again.
    @Timeout(20)
    @Test
    public void testDroppedMessageReported() throws Exception {
        ByteArrayOutputStream written = new ByteArrayOutputStream();
        AbstractPortControllerScaffold port = new AbstractPortControllerScaffold(Mockito.mock(SystemConnectionMemo.class)) {
            private final DataOutputStream dout = new DataOutputStream(written);

            @Override
            public DataOutputStream getOutputStream() {
                return dout;
            }
        };
        tc.connectPort(port);

        ready.set(false);
        AbstractMRMessage first = newMessage(1);
        tc.sendMessage(first, null);
        JUnitUtil.waitFor(() -> JUnitAppender.checkForMessageStartingWith("sendMessage: ") != null,
                "dropped message reported");
        Assertions.assertFalse(first.hasSentTime(), "first message not sent");
        Assertions.assertEquals(0, written.size(), "nothing written");

        AbstractMRMessage second = newMessage(0);
        tc.sendMessage(second, null);
        JUnitUtil.waitFor(() -> second.getRetries() < 0, "second message tried");
        AbstractMRMessage marker = newMessage(0);
        ready.set(true);
        tc.sendMessage(marker, null);
        JUnitUtil.waitFor(marker::hasSentTime, "next message written once the port is ready");
        Assertions.assertFalse(second.hasSentTime(), "second message not sent");
        // the second drop was not a warning: tearDown checks no WARN is left

        ready.set(false);
        AbstractMRMessage third = newMessage(0);
        tc.sendMessage(third, null);
        JUnitUtil.waitFor(() -> JUnitAppender.checkForMessageStartingWith("sendMessage: ") != null,
                "drop after a write reported again");
        Assertions.assertFalse(third.hasSentTime(), "third message not sent");
    }

    private AbstractMRMessage newMessage(int retries) {
        AbstractMRMessage m = new AbstractMRMessage(3) {
            @Override
            public boolean replyExpected() {
                return false;
            }
        };
        m.setElement(0, 0x11);
        m.setElement(1, 0x22);
        m.setElement(2, 0x33);
        m.setRetries(retries);
        m.setTimeout(20);
        return m;
    }

    private final AtomicBoolean ready = new AtomicBoolean(true);
    private AbstractMRTrafficController tc;

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetInstanceManager();
        tc = new AbstractMRTrafficController() {

            @Override
            public boolean portReadyToSend(AbstractPortController p) {
                return ready.get();
            }

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
