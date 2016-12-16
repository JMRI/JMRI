package jmri.jmrix.can;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.util.ResourceBundle;
import jmri.InstanceManager;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;
import jmri.jmrix.AbstractMRListener;
import jmri.jmrix.AbstractMRMessage;
import jmri.jmrix.AbstractMRReply;

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
           protected void setInstance() {};
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

           public void sendCanMessage(CanMessage m, CanListener l) {}
           public void addCanListener(CanListener l) {}
           public void removeCanListener(CanListener l) {}

        };
    }

    @Override
    @After
    public void tearDown(){
       tc = null;
        JUnitUtil.resetInstanceManager();
        apps.tests.Log4JFixture.tearDown(); 
    }

}
