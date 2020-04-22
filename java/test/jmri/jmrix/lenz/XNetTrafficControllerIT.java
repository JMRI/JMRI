package jmri.jmrix.lenz;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import jmri.InstanceManager;
import jmri.Turnout;
import jmri.jmrix.AbstractMRListener;
import jmri.jmrix.AbstractMRMessage;
import jmri.jmrix.lenz.liusb.LIUSBXNetPacketizer;
import jmri.util.JUnitAppender;
import jmri.util.JUnitUtil;
import jmri.util.ThreadingUtil;
import org.apache.log4j.Level;
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
    
    volatile XNetMessage originalMessage;
    Map<XNetMessage, AtomicInteger> retryCounters = new IdentityHashMap<>();
    
    class TestUSBPacketizer extends LIUSBXNetPacketizer implements MessageOutput {
        public TestUSBPacketizer(LenzCommandStation pCommandStation) {
            super(pCommandStation);
            output = this;
        }
        
        /**
         * Counts the number of retries after errors, does NOT count retries
         * after timeout.
s         */
        @Override
        protected synchronized void forwardToPort(AbstractMRMessage m, AbstractMRListener reply) {
            super.forwardToPort(m, reply);
            XNetMessage msg = (XNetMessage)m;
            // for each *instance* of message, track the number of attempts to send.
            // XNetMessage with the same payload are equals(), but we want to track instances,
            // to see what msg was actually re-send, and which was just replicated
            retryCounters.computeIfAbsent(msg, (k) -> new AtomicInteger()).incrementAndGet();
            originalMessage = msg;
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
    
    /**
     * If true, will clear errors at the end.
     */
    private boolean clearErrors;

    @After
    public void tearDown() throws Exception {
        XNetTrafficController ctrl = (XNetTrafficController)output; 
        ctrl.terminateThreads();
        ctrl.disconnectPort(testAdapter);
        testAdapter.dispose();
        JUnitUtil.clearShutDownManager(); // put in place because AbstractMRTrafficController implementing subclass was not terminated properly
        if (clearErrors) {
            JUnitAppender.end();
            JUnitAppender.resetUnexpectedMessageFlags(Level.ERROR);
        }
        
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

    interface AccRequestCallback {
        public XNetReply getReply(int address, int output, boolean state);
    }
    
    class BusyLenzSimulator extends XNetTestSimulator.LZV100_USB {
        volatile AccRequestCallback cb;
        Map<XNetMessage, XNetMessage> backToJMRIMessage = new IdentityHashMap<>();

        @Override
        protected XNetMessage readMessage() {
            XNetMessage received = super.readMessage();
            // the simulator deserializes messages from the stream; they are different objects
            // than the original, so let's map them back with the help of hack in TrafficCOntroller:
            backToJMRIMessage.put(received, originalMessage);
            return received;
        }
        
        @Override
        protected XNetReply generateAccRequestReply(int address, int output, boolean state) {
            if (cb != null) {
                XNetReply r = cb.getReply(address, output, state);
                if (r != null) {
                    return r;
                }
            }
            return super.generateAccRequestReply(address, output, state);
        }
    }
    
    @Test
    public void testSendBusyToOutputOff() throws Exception {
        BusyLenzSimulator simul = new BusyLenzSimulator();
        initializeLayout(simul, new TestUSBPacketizer(new LenzCommandStation()));
        
        XNetTurnout t = (XNetTurnout)xnetManager.provideTurnout("XT21");
        // excess OFF messages are sent
        Thread.sleep(1000);
        
        // send just 3 BUSY to break "error handling"
        AtomicInteger counter = new AtomicInteger(0);
        
        simul.cb = (a, o, s) -> {
            // ignore ON messages, normal processing.
            if (s) {
                return null;
            }
            // send at most 3 BUSY messages.
            if (counter.incrementAndGet() > 3) {
                assertNotNull(lnis);
                assertNotNull(simul);
                return null;
            }
            XNetReply reply = new XNetReply("61 81 E0");
            return reply;
        };
        simul.setCaptureMessages(true);
        t.setCommandedState(XNetTurnout.THROWN);
        
        // wait for the timeout time to get potentially all repetitions.
        Thread.sleep(6000);
        
        List<XNetMessage> outgoing = simul.getOutgoingMessages();
        List<XNetMessage> justOFFs = outgoing.stream().filter((m) -> 
                m.getElement(0) == 0x52 &&
                (m.getElement(2) & 0x08) == 0).collect(Collectors.toList());
        
        // show individual messages were NOT retransmitted at all, the "retransmition"
        // is just a FAKE because of OFFs replicated in advance. Counters for individual
        // object instances are increased in 
        int index = 1;
        XNetMessage last = null;
        for (XNetMessage m : justOFFs) {
            // translate to the JMRI controller instance:
            XNetMessage orig = simul.backToJMRIMessage.get(m);
            if (orig == last) {
                System.err.println("Message #" + index + ": retry previous");
            } else {
                AtomicInteger retries = retryCounters.get(orig);
                System.err.println("Message #" + index + ": " + m + ", retries avail: " + retries.get());
            }
            last = orig;
            index++;
        }
        
        // 5 retransmissions are permitted by default, so after 3 rejected OFFs, some should be yet sent:
        assertTrue("3 OFFs were rejected by station, they must be repeated !", justOFFs.size() > 3);
        
        // there were 3 Busy to OFF message. So 1st message should be sent 3 times unsuccessfully, and once successfully.
        // the other (and last) OFF should be sent without any repetition
        XNetMessage orig = simul.backToJMRIMessage.get(justOFFs.get(0));
        assertEquals(4, retryCounters.get(orig).get());
        orig = simul.backToJMRIMessage.get(justOFFs.get(justOFFs.size() - 1));
        assertEquals(1, retryCounters.get(orig).get());

        // permit cleaning errors logged by TrafficController, all other checks OK
        clearErrors = true;
    }

    @Test
    public void testSendBusyErrorIgnoredNewCommandSent() throws Exception {
        BusyLenzSimulator simul = new BusyLenzSimulator();
        initializeLayout(simul, new TestUSBPacketizer(new LenzCommandStation()));
        
        XNetTurnout t = (XNetTurnout)xnetManager.provideTurnout("XT21");
        // excess OFF messages are sent
        Thread.sleep(1000);
        
        // send just 1 BUSY in response to OFF message.
        // station status query message that will happen BEFORE the erroneously
        // multiplied OFFs:
        AtomicInteger counter = new AtomicInteger(0);
        
        CountDownLatch msgLatch = new CountDownLatch(1);
        
        List<XNetMessage> messagesUntilBusy = new ArrayList<>();
        
        simul.cb = (a, o, s) -> {
            // ignore ON messages, normal processing.
            if (s) {
                return null;
            }
            if (counter.incrementAndGet()> 1) {
                return null;
            }
            XNetReply reply = new XNetReply("61 81 E0");
            messagesUntilBusy.addAll(simul.getOutgoingMessages());
            simul.clearMesages();
            
            // at this point, queue "incidentally" another command for transmission
            // it has to be before pre-poll interval elapses.
            
            // It would be better to do right here, but Paul would probably not believe it is real,
            // so rely on the main thread will wake up & execute the sendXNetMessage in less than 100ms.
            msgLatch.countDown();
            return reply;
        };
        simul.setCaptureMessages(true);
        t.setCommandedState(XNetTurnout.THROWN);

        List<XNetMessage>   outgoingAfterError = new ArrayList<>();
        
        // wait until the error is generated.
        assertTrue(msgLatch.await(5, TimeUnit.SECONDS));
        
        CountDownLatch versionLatch = new CountDownLatch(1);
        
        // then just send a normal message at the same time OFF is rejected. 
        // The rejected OFF should be repeated, before THIS message is processed:
        lnis.sendXNetMessage(
            XNetMessage.getCSVersionRequestMessage(),
            new XNetListenerScaffold(){
                @Override
                public void message(XNetReply msg) {
                    outgoingAfterError.addAll(simul.getOutgoingMessages());
                    versionLatch.countDown();
                }
            }
        );
        // wait until the Version request is *replied*
        assertTrue(versionLatch.await(5, TimeUnit.SECONDS));
        
        // the repeated OFF should have been in the captured outgoing messages before
        // the CS version request:
        assertEquals(0x52, outgoingAfterError.get(0).getElement(0));
        
        // permit cleaning errors logged by TrafficController, all other checks OK
        clearErrors = true;
    }
}
