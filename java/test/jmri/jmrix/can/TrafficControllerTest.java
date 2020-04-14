package jmri.jmrix.can;

import jmri.jmrix.AbstractMRListener;
import jmri.jmrix.AbstractMRMessage;
import jmri.jmrix.AbstractMRReply;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests for TrafficController.
 * @author Paul Bender Copyright (C) 2016
 */
public class TrafficControllerTest extends AbstractCanTrafficControllerTest {
   
    @Test
    public void testGetCanid(){
        Assert.assertEquals("default canid value",120,((TrafficController)tc).getCanid());
    }

    @Test
    public void testSetCanid(){
        ((TrafficController)tc).setCanId(240);
        Assert.assertEquals("canid value after set",240,((TrafficController)tc).getCanid());
    }
 
    @Override
    @Before
    public void setUp() {
        jmri.util.JUnitUtil.setUp(); 
        JUnitUtil.resetInstanceManager();
        tc = new TrafficController(){
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
           public void addCanListener(CanListener l) {}
           @Override
           public void removeCanListener(CanListener l) {}

        };
    }

    @Override
    @After
    public void tearDown(){
       tc = null;
        JUnitUtil.clearShutDownManager(); // put in place because AbstractMRTrafficController implementing subclass was not terminated properly
        JUnitUtil.tearDown();
 
    }

}
