package jmri.jmrix;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests for AbstractMRNodeTrafficController.
 * @author Paul Bender Copyright (C) 2016
 */
public class AbstractMRNodeTrafficControllerTest extends AbstractMRTrafficControllerTest {
    
    @Test
    @Override
    public void testCtor() {
        Assert.assertNotNull(tc);
    }

    @Test
    public void testGetNumNodesZero(){
        Assert.assertEquals("NumNodes at start",0,((AbstractMRNodeTrafficController)tc).getNumNodes());
    }

    @Before
    @Override
    public void setUp() {
        JUnitUtil.setUp(); 
        JUnitUtil.resetInstanceManager();
        tc = new AbstractMRNodeTrafficController(){
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
        };
    }

    @After
    @Override
    public void tearDown(){
       tc = null;
        JUnitUtil.clearShutDownManager(); // put in place because AbstractMRTrafficController implementing subclass was not terminated properly
        JUnitUtil.tearDown();
 
    }

}
