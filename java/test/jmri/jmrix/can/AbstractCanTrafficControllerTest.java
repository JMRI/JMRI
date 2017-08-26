package jmri.jmrix.can;

import jmri.jmrix.AbstractMRListener;
import jmri.jmrix.AbstractMRMessage;
import jmri.jmrix.AbstractMRReply;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Before;

/**
 * Tests for AbstractCanTrafficController.
 * @author Paul Bender Copyright (C) 2016
 */
public class AbstractCanTrafficControllerTest extends jmri.jmrix.AbstractMRTrafficControllerTest {
    
    @Override
    @Before
    public void setUp() {
        apps.tests.Log4JFixture.setUp(); 
        JUnitUtil.resetInstanceManager();
        tc = new AbstractCanTrafficController(){
           @Override
           protected void setInstance() {}
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
           public void sendCanMessage(CanMessage m, CanListener l) {}
           @Override
           public void addCanListener(CanListener l) {}
           @Override
           public void removeCanListener(CanListener l) {}

        };
    }

    @Override
    @After
    public void tearDown(){
       tc = null;
        JUnitUtil.tearDown(); 
    }

}
