package jmri.jmrix;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for connecting a traffic controller to its port again after the
 * connection failed, as connection recovery does: only one transmit thread
 * runs afterwards.
 *
 * @author Keith Bruce Copyright (C) 2026
 */
public class AbstractMRTrafficControllerReconnectTest {

    private AbstractMRTrafficController tc;
    private AbstractMRMessage progModeMessage = null;
    private AbstractMRMessage normalModeMessage = null;
    private AbstractMRMessage pollMessage = null;

    /** A one-byte message whose reply would take a long time, as a request/reply system's can (NCE: 10 s). */
    private static AbstractMRMessage slowMessage() {
        AbstractMRMessage m = new AbstractMRMessage(1) {
        };
        m.setElement(0, 0x55);
        m.setTimeout(30_000);
        return m;
    }

    /**
     * @param state a transmit state
     * @return true once the transmit thread is in that state and waiting
     *         (it sets the state just before it sends)
     */
    private boolean waitingIn(int state) {
        Thread t = tc.xmtThread;
        return JUnitUtil.waitFor(() -> tc.mCurrentState == state && t.getState() == Thread.State.TIMED_WAITING);
    }

    private static AbstractSerialPortControllerScaffold port() throws Exception {
        return new AbstractSerialPortControllerScaffold(Mockito.mock(jmri.SystemConnectionMemo.class));
    }

    /**
     * A port that, asked to recover, connects the traffic controller to a
     * new port, as an adapter's recovery does once its port reopens.
     */
    private class RecoveringPort extends AbstractSerialPortControllerScaffold {

        RecoveringPort() throws Exception {
            super(Mockito.mock(jmri.SystemConnectionMemo.class));
        }

        @Override
        public void recover() {
            try {
                tc.connectPort(port());
            } catch (Exception e) {
                fail(e);
            }
        }
    }

    /**
     * End the receive loop, as a failed connection does; the receive thread
     * then recovers, connecting again. The old transmit thread must have
     * ended by then.
     */
    private void reconnectAndCheck() throws Exception {
        Thread oldTransmit = tc.xmtThread;
        Thread oldReceive = tc.rcvThread;
        oldReceive.interrupt(); // its read ends, as when the connection fails
        assertTrue(JUnitUtil.waitFor(() -> tc.xmtThread != oldTransmit), "connected again");
        oldReceive.join(5000);
        assertTrue(tc.xmtThread.isAlive());
        assertFalse(oldTransmit.isAlive(), "the old transmit thread has ended: only one takes from the queue");
    }

    @Test
    public void testOldTransmitThreadEndsWhenReconnected() throws Exception {
        AbstractSerialPortControllerScaffold first = new RecoveringPort();
        tc.connectPort(first);
        tc.sendMessage(slowMessage(), Mockito.mock(AbstractMRListener.class));
        assertTrue(waitingIn(AbstractMRTrafficController.WAITMSGREPLYSTATE),
                "the transmit thread is waiting for the reply");
        reconnectAndCheck();
    }

    @Test
    public void testWaitingToEnterProgrammingMode() throws Exception {
        progModeMessage = slowMessage();
        AbstractSerialPortControllerScaffold first = new RecoveringPort();
        tc.connectPort(first);
        AbstractMRMessage m = slowMessage();
        m.setNeededMode(AbstractMRTrafficController.PROGRAMINGMODE);
        tc.sendMessage(m, Mockito.mock(AbstractMRListener.class));
        assertTrue(waitingIn(AbstractMRTrafficController.WAITREPLYINPROGMODESTATE));
        reconnectAndCheck();
    }

    @Test
    public void testWaitingToEnterNormalMode() throws Exception {
        normalModeMessage = slowMessage();
        AbstractSerialPortControllerScaffold first = new RecoveringPort();
        tc.mCurrentMode = AbstractMRTrafficController.PROGRAMINGMODE;
        tc.mWaitBeforePoll = 60_000; // stays in programming mode until the message
        tc.connectPort(first);
        tc.sendMessage(slowMessage(), Mockito.mock(AbstractMRListener.class));
        assertTrue(waitingIn(AbstractMRTrafficController.WAITREPLYINNORMMODESTATE));
        reconnectAndCheck();
    }

    @Test
    public void testWaitingToLeaveProgrammingMode() throws Exception {
        normalModeMessage = slowMessage();
        AbstractSerialPortControllerScaffold first = new RecoveringPort();
        tc.mCurrentMode = AbstractMRTrafficController.PROGRAMINGMODE;
        tc.mWaitBeforePoll = 10; // idle in programming mode: leaves it
        tc.connectPort(first);
        assertTrue(waitingIn(AbstractMRTrafficController.WAITREPLYINNORMMODESTATE));
        reconnectAndCheck();
    }

    @Test
    public void testWaitingForPollReply() throws Exception {
        pollMessage = slowMessage();
        AbstractSerialPortControllerScaffold first = new RecoveringPort();
        tc.mWaitBeforePoll = 10;
        tc.connectPort(first);
        assertTrue(waitingIn(AbstractMRTrafficController.WAITMSGREPLYSTATE));
        reconnectAndCheck();
    }

    @Test
    public void testWaitingToRetrySend() throws Exception {
        AbstractSerialPortControllerScaffold first = new RecoveringPort();
        tc.connectPort(first);
        Thread oldTransmit = tc.xmtThread;
        tc.xmtException = true; // the port can't take the message: the transmit thread waits to try again
        AbstractMRMessage m = slowMessage();
        m.setRetries(5);
        tc.sendMessage(m, Mockito.mock(AbstractMRListener.class));
        assertTrue(JUnitUtil.waitFor(() -> oldTransmit.getState() == Thread.State.TIMED_WAITING
                && tc.mCurrentState == AbstractMRTrafficController.WAITMSGREPLYSTATE));
        reconnectAndCheck();
        assertEquals(0, first.tostream.available(), "the message was not sent on the failed port");
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
                return pollMessage;
            }

            @Override
            protected AbstractMRListener pollReplyHandler() {
                return null;
            }

            @Override
            protected AbstractMRMessage enterProgMode() {
                return progModeMessage;
            }

            @Override
            protected AbstractMRMessage enterNormalMode() {
                return normalModeMessage;
            }

            @Override
            protected void forwardReply(AbstractMRListener client, AbstractMRReply m) {
            }

            @Override
            protected AbstractMRReply newReply() {
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
        tc = null;
        JUnitUtil.tearDown();
    }
}
