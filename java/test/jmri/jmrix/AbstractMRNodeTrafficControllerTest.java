package jmri.jmrix;

import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;

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

    private static AbstractNode node(int address) {
        AbstractNode n = new AbstractNode() {
            @Override
            protected boolean checkNodeAddress(int a) {
                return true;
            }

            @Override
            public AbstractMRMessage createInitPacket() {
                return null;
            }

            @Override
            public AbstractMRMessage createOutPacket() {
                return null;
            }

            @Override
            public boolean getSensorsActive() {
                return false;
            }

            @Override
            public boolean handleTimeout(AbstractMRMessage m, AbstractMRListener l) {
                return false;
            }

            @Override
            public void resetTimeout(AbstractMRMessage m) {
            }
        };
        n.setNodeAddress(address);
        return n;
    }

    @Test
    public void testDeleteNodeKeepsEachNodesInitFlag() {
        AbstractMRNodeTrafficController ntc = newController();
        ntc.init(0, 10);
        ntc.registerNode(node(1));
        ntc.registerNode(node(2));
        ntc.registerNode(node(3));
        ntc.setMustInit(0, false);
        ntc.setMustInit(1, false); // node 2 initialised
        ntc.setMustInit(2, true);  // node 3 still waiting to be initialised
        ntc.deleteNode(2);
        Assert.assertEquals(2, ntc.getNumNodes());
        Assert.assertEquals(3, ntc.getNode(1).getNodeAddress());
        Assert.assertTrue("node 3 still to be initialised", ntc.getMustInit(1));
        Assert.assertFalse("node 1 unchanged", ntc.getMustInit(0));
        ntc.terminateThreads();
    }

    @Test
    public void testDeleteUnknownNodeDeletesNothing() {
        AbstractMRNodeTrafficController ntc = newController();
        ntc.init(0, 10);
        ntc.registerNode(node(1));
        ntc.registerNode(node(2));
        ntc.deleteNode(7);
        jmri.util.JUnitAppender.assertWarnMessage("No node with address 7 to delete");
        Assert.assertEquals("nothing deleted", 2, ntc.getNumNodes());
        Assert.assertEquals(1, ntc.getNode(0).getNodeAddress());
        ntc.terminateThreads();
    }

    @BeforeEach
    @Override
    public void setUp() {
        JUnitUtil.setUp(); 
        JUnitUtil.resetInstanceManager();
        tc = newController();
    }

    /**
     * A bare controller for the node tests. Subclass tests replace tc with
     * their own controller, which may only accept its own node type, so the
     * node tests use this one directly.
     */
    private static AbstractMRNodeTrafficController newController() {
        return new AbstractMRNodeTrafficController(){
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

    @AfterEach
    @Override
    public void tearDown(){
       tc = null;
        JUnitUtil.clearShutDownManager(); // put in place because AbstractMRTrafficController implementing subclass was not terminated properly
        JUnitUtil.tearDown();
 
    }

}
