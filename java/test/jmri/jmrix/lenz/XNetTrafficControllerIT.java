package jmri.jmrix.lenz;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import jmri.InstanceManager;
import jmri.Turnout;
import jmri.jmrix.AbstractMRListener;
import jmri.jmrix.AbstractMRMessage;
import jmri.jmrix.AbstractMRTrafficController;
import jmri.jmrix.lenz.liusb.LIUSBXNetPacketizer;
import jmri.util.JUnitUtil;
import jmri.util.ThreadingUtil;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Integration tests for XNetTrafficController
 * @author svatopluk.dedic@gmail.com
 */
public class XNetTrafficControllerIT {
    XNetTestSimulator testAdapter;
    XNetPacketizer lnis;
    
    volatile boolean blockMessageQueue;
    final Semaphore messageQueuePermits = new Semaphore(0);
    final BlockingQueue<Runnable> injectMessageQueue = new LinkedBlockingQueue<>();
    volatile boolean timeoutOccured;
    
    XNetTurnoutManager xnetManager;
    
    MessageOutput output;
    
    private void injectMessages() {
        List<Runnable> r = new ArrayList<>();
        injectMessageQueue.drainTo(r);
        r.stream().forEach(Runnable::run);
    }
        
    @Before
    public void setUp() {
        JUnitUtil.setUp();

        // prepare an interface
        jmri.util.JUnitUtil.resetInstanceManager();
        jmri.util.JUnitUtil.initInternalSensorManager();
        jmri.util.JUnitUtil.initInternalTurnoutManager();
        jmri.InstanceManager.store(new jmri.NamedBeanHandleManager(), jmri.NamedBeanHandleManager.class);
    }
    
    public interface MessageOutput {
        public void sendMessage(AbstractMRMessage m, AbstractMRListener reply);
    }
    
    class TestUSBPacketizer extends LIUSBXNetPacketizer implements MessageOutput {

        public TestUSBPacketizer(LenzCommandStation pCommandStation) {
            super(pCommandStation);
            output = this;
        }
        
        @Override
        protected AbstractMRMessage takeMessageToTransmit(AbstractMRListener[] ll) {
            AbstractMRMessage mrm = super.takeMessageToTransmit(ll);
            if (blockMessageQueue) {
                try {
                    messageQueuePermits.acquire();
                    injectMessages();
                } catch (InterruptedException ex) {

                }
            }
            return mrm;
        }

        @Override
        protected AbstractMRMessage pollMessage() {
            injectMessages();
            return super.pollMessage(); 
        }
        
        @Override
        protected void handleTimeout(AbstractMRMessage msg, AbstractMRListener l) {
            if (!threadStopRequest) {
                super.handleTimeout(msg, l);
                timeoutOccured = true;
            }
        }

        // Just a trampoline
        @Override
        public synchronized void sendMessage(AbstractMRMessage m, AbstractMRListener reply) {
            super.sendMessage(m, reply);
        }
    }
    
    class TestPacketizer extends XNetPacketizer implements MessageOutput {

        public TestPacketizer(LenzCommandStation pCommandStation) {
            super(pCommandStation);
            output = this;
        }

        @Override
        protected AbstractMRMessage pollMessage() {
            injectMessages();
            return super.pollMessage(); 
        }
        
        @Override
        protected AbstractMRMessage takeMessageToTransmit(AbstractMRListener[] ll) {
            AbstractMRMessage mrm = super.takeMessageToTransmit(ll);
            if (blockMessageQueue) {
                try {
                    messageQueuePermits.acquire();
                    injectMessages();
                } catch (InterruptedException ex) {

                }
            }
            return mrm;
        }

        @Override
        protected void handleTimeout(AbstractMRMessage msg, AbstractMRListener l) {
            super.handleTimeout(msg, l);
            timeoutOccured = true;
        }

        // Just a trampoline
        @Override
        public synchronized void sendMessage(AbstractMRMessage m, AbstractMRListener reply) {
            super.sendMessage(m, reply);
        }
    }
    
    private void initializeLayout(XNetTestSimulator adapter) throws Exception {
        initializeLayout(adapter, new TestPacketizer(new LenzCommandStation()));
    }
    
    private void initializeLayout(XNetTestSimulator adapter, XNetPacketizer packetizer) throws Exception {
        testAdapter = adapter;
        lnis = packetizer;
        testAdapter.configure(lnis);
        xnetManager = (XNetTurnoutManager)InstanceManager.getDefault().getInstance(XNetSystemConnectionMemo.class).getTurnoutManager();
        
        // queue a null message, simulator will signal when it is transmitted -
        // all startup messages are already procesed.
        testAdapter.drainPackets(true);
    }

    @After
    public void tearDown() throws Exception {
        XNetTrafficController ctrl = (XNetTrafficController)output; 
        ctrl.terminateThreads();
        ctrl.disconnectPort(testAdapter);
        testAdapter.dispose();
        JUnitUtil.clearShutDownManager(); // put in place because AbstractMRTrafficController implementing subclass was not terminated properly
        JUnitUtil.tearDown();
    }

    /**
     * Check that normal messages will arrive in
     * the same order as they were posted.
     * PENDING: this should be tested for all subclasses of XNetTrafficController.
     */
    @Test
    public void testNormalMessages() throws Exception {
        XNetTestSimulator simul = new XNetTestSimulator.LZV100();
        initializeLayout(simul);
        
        simul.setCaptureMessages(true);
        
        simul.limitReplies = true;
        XNetMessage m = XNetMessage.getCSVersionRequestMessage();        
        XNetMessage m2 = XNetMessage.getCSStatusRequestMessage();
        XNetMessage m3 = XNetMessage.getLocomotiveInfoRequestMsg(1);
        
        CountDownLatch l = new CountDownLatch(3);
        XNetListener callback = new XNetListenerScaffold() {
            @Override
            public void message(XNetReply m) {
                l.countDown();
            }
        };
        output.sendMessage(m, callback);
        output.sendMessage(m2, callback);
        output.sendMessage(m3, callback);
        
        simul.repliesAllowed.release(100);
        
        l.await(300, TimeUnit.MILLISECONDS);
        
        List<XNetMessage> msgs = simul.getOutgoingMessages();
        assertEquals(Arrays.asList(m, m2, m3), msgs);
    }
    
    /**
     * Checks that a priority message will preempt existing messages
     * in the queue, and also new messages that should be yet sent.
     * PENDING: this should be tested for all subclasses of XNetTrafficController.
     */
    @Test
    public void testSendPriorityMessages() throws Exception {
        XNetTestSimulator simul = new XNetTestSimulator.LZV100();
        initializeLayout(simul);
        
        simul.setCaptureMessages(true);
        
        XNetMessage m = XNetMessage.getCSVersionRequestMessage();       
        XNetMessage m2 = XNetMessage.getCSStatusRequestMessage();
        XNetMessage m3 = XNetMessage.getLocomotiveInfoRequestMsg(1);
        XNetMessage m4 = XNetMessage.getLocomotiveFunctionStatusMsg(1);
        XNetMessage m5 = XNetMessage.getEmergencyOffMsg();
        
        CountDownLatch l = new CountDownLatch(5);
        XNetListener callback = new XNetListenerScaffold() {
            @Override
            public void message(XNetReply m) {
                l.countDown();
            }
        };
        
        // better sync: wait for 1st m message to be taken from the queue + put the rest in the queue:
        blockMessageQueue = true;

        synchronized (output) {
            output.sendMessage(m, callback);    // xmit thread will stop after dequeing this
            output.sendMessage(m2, callback);   // will sit already in the transmit queue after the xmit thread goes to fetch
        }

        injectMessageQueue.add(() -> {
            // will execute at a time before m and m2
            List<XNetMessage> msgs = simul.getOutgoingMessages();
            assertTrue(msgs.isEmpty());
            
            // the following will insert two high-priority plus one normal before m and m2 is processed.
            // the effect should be that m is processed first (it is already out), m2 is preempted by m3, m4
            // and m5 queues normally at the end.
            lnis.sendHighPriorityXNetMessage(m3, callback);
            lnis.sendHighPriorityXNetMessage(m4, callback);
            output.sendMessage(m5, callback);
        });
        
        // all is set up, release the xmit thread.
        messageQueuePermits.release(100);
        
        l.await(300, TimeUnit.MILLISECONDS);
        
        List<XNetMessage> msgs = simul.getOutgoingMessages();
        assertEquals(Arrays.asList(m, m3, m4, m2, m5), msgs);
    }
    
    private <T> T initOnLayout(Callable<T> c) throws Exception {
        AtomicReference<T> res = new AtomicReference<>();
        AtomicReference<Exception> exe = new AtomicReference<>();
        ThreadingUtil.runOnLayout(() -> {
            try {
                res.set(c.call());
            } catch (Exception ex) {
                exe.set(ex);
            }
        });
        if (exe.get() != null) {
            throw exe.get();
        }
        return res.get();
    }
    
    /**
     * Checks that a sole feedback response to Turnout command
     * is sufficient to acknowledge the command.
     */
    @Test
    public void testFeedbackOnlyAccepted() throws Exception {
        XNetTestSimulator simul = new XNetTestSimulator.NanoXGenLi();
        initializeLayout(simul);

        Turnout t = initOnLayout(() -> {
            Turnout x = xnetManager.provideTurnout("XT5");
            lnis.sendXNetMessage(new XNetMessage("00 00 00"), null);
            return x;
        });
        
        testAdapter.drainPackets(true);

        simul.setCaptureMessages(true);
        ThreadingUtil.runOnLayout(() -> {
            t.setCommandedState(XNetTurnout.THROWN);
        });

        // wait > 5sec to capture a timeout
        Thread.sleep(6000);
        List<XNetReply> replies = simul.getIncomingReplies();
        
        assertFalse("Must not time out", timeoutOccured);
        assertEquals("Expected one feedback and 3 OKs for OFF messages", 3, replies.size());
        assertEquals("Feedback reply expected", 0x42, replies.get(0).getElement(0));
    }
    
    /**
     * Checks that a sole OK response to a Turnout command is sufficient
     * to acknowledge the command message.
     */
    @Test
    public void testInterfaceOKOnlyAccepted() throws Exception {
        XNetTestSimulator simul = new XNetTestSimulator.LZV100();
        initializeLayout(simul);
        
        Turnout t = initOnLayout(() -> {
                Turnout x = xnetManager.provideTurnout("XT5");
                x.setCommandedState(XNetTurnout.CLOSED);
                return x;
        });
        
        // delayed OFF messages are sent
        Thread.sleep(500);

        testAdapter.drainPackets(true);
        simul.setCaptureMessages(true);
        
        ThreadingUtil.runOnLayout(() -> {
            t.setCommandedState(XNetTurnout.CLOSED);
        });

        // wait > 5sec to capture a timeout
        Thread.sleep(6000);
        List<XNetReply> replies = simul.getIncomingReplies();
        
        assertFalse("Must not time out", timeoutOccured);
        // FIXME: there is a LOT of OK messages
        assertTrue(replies.size() > 1);
        for (XNetReply r : replies) {
            assertTrue("Only OKs are expected", r.isOkMessage());
        }
    }
    
    /**
     * Check that feedback+ok is processed before the command is acknowledged.
     */
    @Test
    public void testFeedbackAndOKProcessedBeforeNextCommand() throws Exception {
        
        XNetTestSimulator simul = new XNetTestSimulator.LZV100_USB();
        CountDownLatch l = new CountDownLatch(1);
        CountDownLatch l2 = new CountDownLatch(1);
        
        AtomicReference<AbstractMRMessage> marker = new AtomicReference<>();
        
        initializeLayout(simul, new TestUSBPacketizer(new LenzCommandStation()) {
            boolean count2 = false;
            
            @Override
            protected void forwardToPort(AbstractMRMessage m, AbstractMRListener reply) {
                
                synchronized (XNetTrafficControllerIT.this) {
                    super.forwardToPort(m, reply); 
                    if (count2) {
                        l2.countDown();
                    }
                    if (m == marker.get()) {
                        l.countDown();
                        count2 = true;
                    }
                }
            }
        });
        
        Turnout t = initOnLayout(() -> {
            Turnout x = xnetManager.provideTurnout("XT21");
            return x;
        });
        // there's a query packet after turnout creation
        testAdapter.drainPackets(true);
        
        ThreadingUtil.runOnLayout(() -> {
            t.setCommandedState(XNetTurnout.CLOSED);
        });
        
        // delayed OFF messages are sent
        Thread.sleep(500);
        testAdapter.drainPackets(true);
        simul.setCaptureMessages(true);
        
        ThreadingUtil.runOnLayout(() -> {
            // block property changes for a while
            synchronized (t) {
                t.setCommandedState(XNetTurnout.THROWN);
                XNetMessage msg = new XNetMessage("21 24 05");
                lnis.sendXNetMessage(msg, null);
                marker.set(msg);
            }
        });
        assertTrue(l.await(300, TimeUnit.MILLISECONDS));

        // the turnout must be already IDLE
        assertEquals(XNetTurnout.IDLE, ((XNetTurnout)t).internalState);
        
        // sleep for some more time, to get the final reply:
        
        // there should be 4 replies from the 'command station':
        // accessory request --> feedback + OK
        // 1st OFF           --> OK
        // 2nd OFF           --> OK, but may be delayed
        assertTrue(l2.await(500, TimeUnit.MILLISECONDS));
        Thread.sleep(100);
        List<XNetReply> replies = simul.getIncomingReplies();
        assertTrue(replies.size() >= 3);
        // feedback reply to the turnout command
        assertEquals(0x42, replies.get(0).getElement(0));
        // OK 2nd reply to the turnout command
        assertEquals(0x01, replies.get(1).getElement(0));
        // OK reply to the non-delayed OFF message
        assertEquals(0x01, replies.get(2).getElement(0));
        
        // the message at index #3 and #4 are eithr response to the marker,
        // or the OK-to-second OFF.
    }

}
