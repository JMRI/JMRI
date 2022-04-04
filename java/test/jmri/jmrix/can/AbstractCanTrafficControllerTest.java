package jmri.jmrix.can;

import jmri.jmrix.AbstractMRListener;
import jmri.jmrix.AbstractMRMessage;
import jmri.jmrix.AbstractMRReply;
import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

/**
 * Tests for AbstractCanTrafficController.
 * @author Paul Bender Copyright (C) 2016
 */
public class AbstractCanTrafficControllerTest extends jmri.jmrix.AbstractMRTrafficControllerTest {

    @Override
    @BeforeEach
    public void setUp() {
        jmri.util.JUnitUtil.setUp();
        JUnitUtil.resetInstanceManager();
        tc = new AbstractCanTrafficController(){
           @Override
           protected void forwardMessage(AbstractMRListener client, AbstractMRMessage m){
           }
           @Override
           protected AbstractMRMessage pollMessage(){ return null; }
           @Override
           protected AbstractMRListener pollReplyHandler() { return null; }
           @Override
           protected AbstractMRMessage enterProgMode() { return null; }
           @Override
           protected AbstractMRMessage enterNormalMode() { return null; }
           @Override
           protected void forwardReply(AbstractMRListener client, AbstractMRReply m){}
           @Override
           protected AbstractMRReply newReply() { return null; }
           @Override
           protected boolean endOfMessage(AbstractMRReply r) {return true; }
           @Override
           protected AbstractMRMessage newMessage() { return null; }
           @Override
           public CanReply decodeFromHardware(AbstractMRReply m) { return null; }
           @Override
           public AbstractMRMessage encodeForHardware(CanMessage m) { return null; }

           @Override
           public void sendCanReply(CanReply r, CanListener l) {}
           @Override
           public void sendCanMessage(CanMessage m, CanListener l) {}
           @Override
           public synchronized void addCanListener(CanListener l) {}
           @Override
           public synchronized void removeCanListener(CanListener l) {}

        };
    }

    @Override
    @AfterEach
    public void tearDown(){
        tc.terminateThreads();
        tc = null;
        JUnitUtil.tearDown();
    }

}
