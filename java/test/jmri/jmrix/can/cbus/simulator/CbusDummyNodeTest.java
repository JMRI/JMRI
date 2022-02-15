package jmri.jmrix.can.cbus.simulator;

import jmri.jmrix.can.CanMessage;
import jmri.jmrix.can.CanReply;
import jmri.jmrix.can.CanSystemConnectionMemo;
import jmri.jmrix.can.TrafficControllerScaffold;
import jmri.jmrix.can.cbus.CbusConstants;

import jmri.jmrix.can.cbus.simulator.moduletypes.MergCanpan;

import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 * @author Steve Young Copyright (C) 2019
 */
public class CbusDummyNodeTest {

    private TrafficControllerScaffold tcis;
    private CanSystemConnectionMemo memo;
    
    @Test
    public void testCTor() {
        CbusDummyNode t = new CbusDummyNode(null, 1);
        Assert.assertNotNull("exists",t);
        t.dispose();
    }
    
    @Test
    public void testListeners() {
        
        Assert.assertTrue("0 listeners",tcis.numListeners()==0);
        CbusDummyNode t = new CbusDummyNode(memo, 1 );
        
        Assert.assertTrue("1 listener",tcis.numListeners()==1);
        
        Assert.assertTrue("start getDelay", ((CbusSimCanListener) t.getCanListener()).getDelay()>0);
        ((CbusSimCanListener) t.getCanListener()).setDelay(7);
        Assert.assertEquals("getSetDelay", 7,((CbusSimCanListener) t.getCanListener()).getDelay());
        
        t.dispose();
        Assert.assertTrue("0 listeners after dispose",tcis.numListeners()==0);
    }
    
    @Test
    public void testNodeRequestNumber() {
        
        CbusDummyNode t = new MergCanpan().getNewDummyNode( memo, 1);
        ((CbusSimCanListener) t.getCanListener()).setDelay(0);
        
        Assert.assertFalse("start getProcessIn", ((CbusSimCanListener) t.getCanListener()).getProcessIn() );
        Assert.assertTrue("start getProcessOut", ((CbusSimCanListener) t.getCanListener()).getProcessOut() );
        Assert.assertTrue("start getSendIn", ((CbusSimCanListener) t.getCanListener()).getSendIn() );
        Assert.assertFalse("start getSendOut", ((CbusSimCanListener) t.getCanListener()).getSendOut() );
        Assert.assertFalse("start getsendsWRACKonNVSET", t.getnvWriteInLearnOnly());
        
        Assert.assertFalse("is a CANPAN getsendsWRACKonNVSET",t.getsendsWRACKonNVSET());
        Assert.assertEquals(" getNodeType", "CANPAN",t.getNodeStats().getNodeTypeName() );
        Assert.assertTrue("canpan getTotalNVs 0",t.getNodeNvManager().getTotalNVs()== 1 );
        
        t.flimButton();
        
        JUnitUtil.waitFor(()->{ return(!tcis.inbound.isEmpty()); }, " reply didn't arrive");
        
        Assert.assertEquals(" 0 outbound not increased", 0,(tcis.outbound.size()));
        Assert.assertEquals(" 1 inbound nodenum request ", 1,(tcis.inbound.size()));
        
        Assert.assertEquals("node request inbound", "[5f8] 50 00 01",
            tcis.inbound.elementAt(tcis.inbound.size() - 1).toString());
        // node in setup mode awaiting node allocation frame or further setup info request
        
        ((CbusSimCanListener) t.getCanListener()).setSendOut(true);
        ((CbusSimCanListener) t.getCanListener()).setSendIn(false);
        Assert.assertTrue(" getSendOut", ((CbusSimCanListener) t.getCanListener()).getSendOut() );
        Assert.assertFalse(" getSendIn", ((CbusSimCanListener) t.getCanListener()).getSendIn() );
        
        ((CbusSimCanListener) t.getCanListener()).setProcessIn(true);
        ((CbusSimCanListener) t.getCanListener()).setProcessOut(false);
        
        Assert.assertTrue("start getProcessIn", ((CbusSimCanListener) t.getCanListener()).getProcessIn() );
        Assert.assertFalse("start getProcessOut", ((CbusSimCanListener) t.getCanListener()).getProcessOut() );
        
        CanReply r = new CanReply();
        r.setHeader(tcis.getCanid());
        r.setNumDataElements(1);
        r.setElement(0, CbusConstants.CBUS_RQNP); 
        t.getCanListener().reply(r);
        
        CanMessage m = new CanMessage( tcis.getCanid() );
        m.setNumDataElements(1);
        m.setElement(0, CbusConstants.CBUS_RQNP);
        t.getCanListener().message(m); // should be ignored
        
        JUnitUtil.waitFor(()->{ return(!tcis.outbound.isEmpty()); }, " reply didn't arrive");
        
        Assert.assertEquals(" 1 outbound params request", 1,(tcis.outbound.size() ) );
        Assert.assertEquals(" 1 inbound not increased ", 1,(tcis.inbound.size() ) );
        
        Assert.assertEquals("node responds with parameters setup", "[5f8] EF A5 59 1D 80 0D 01 01",
            tcis.outbound.elementAt(tcis.outbound.size() - 1).toString());
        
        ((CbusSimCanListener) t.getCanListener()).setProcessIn(false);
        ((CbusSimCanListener) t.getCanListener()).setProcessOut(true);
        
        ((CbusSimCanListener) t.getCanListener()).setSendOut(false);
        ((CbusSimCanListener) t.getCanListener()).setSendIn(true);
        Assert.assertFalse(" getSendOut f", ((CbusSimCanListener) t.getCanListener()).getSendOut() );
        Assert.assertTrue(" getSendIn t", ((CbusSimCanListener) t.getCanListener()).getSendIn() );
        
        r = new CanReply();
        r.setHeader(tcis.getCanid());
        r.setNumDataElements(3);
        r.setElement(0, CbusConstants.CBUS_SNN);
        r.setElement(1, 0xb1); // nn
        r.setElement(2, 0x2c); // nn
        t.getCanListener().reply(r); // should be ignored
        
        m = new CanMessage( tcis.getCanid() );
        m.setNumDataElements(3);
        m.setElement(0, CbusConstants.CBUS_SNN);
        m.setElement(1, 0xb1);
        m.setElement(2, 0x2c);
        t.getCanListener().message(m);
        
        JUnitUtil.waitFor(()->{ return(tcis.inbound.size()>1); }, " inbound 2 didn't arrive");
        
        Assert.assertEquals(" 1 outbound not increased", 1,(tcis.outbound.size() ) );
        Assert.assertEquals(" 2 inbound  ", 2,(tcis.inbound.size() ) );
        
        Assert.assertEquals("node responds confirming node number", "[5f8] 52 B1 2C",
            tcis.inbound.elementAt(tcis.inbound.size() - 1).toString());
        
        m = new CanMessage( tcis.getCanid() );
        m.setNumDataElements(1);
        m.setElement(0, CbusConstants.CBUS_QNN);
        t.getCanListener().message(m);
        
        Assert.assertEquals(" 1 outbound not increased", 1,(tcis.outbound.size() ) );
        JUnitUtil.waitFor(()->{ return(tcis.inbound.size()>2); }, " inbound 3 didn't arrive");
        Assert.assertEquals(" 3 inbound  ", 3,(tcis.inbound.size() ) );
        
        Assert.assertEquals("node responds to QNN with PNN", "[5f8] B6 B1 2C A5 1D 0D",
            tcis.inbound.elementAt(tcis.inbound.size() - 1).toString());
        
        // set node to SLIM to check inbound and outbound listeners
        t.setNodeInFLiMMode(false);
        
        t.getCanListener().message(m);
        
        Assert.assertEquals(" 1 outbound not increased", 1,(tcis.outbound.size() ) );
        Assert.assertEquals(" 3 inbound  not increased", 3,(tcis.inbound.size() ) );
        
        t.flimButton();  // does nothing in node type 0
        
        Assert.assertEquals(" 1 outbound not increased", 1,(tcis.outbound.size() ) );
        Assert.assertEquals(" 3 inbound  not increased", 3,(tcis.inbound.size() ) );
        
        t.dispose();
        
    }
    
    @Test
    public void testNodeFetchParameters() {
        
        // dummy CANPAN
        CbusDummyNode t = new MergCanpan().getNewDummyNode(memo, 1234);
        ((CbusSimCanListener) t.getCanListener()).setDelay(0);
        t.setNodeInFLiMMode(true);
        
        // get event variables with knowledge of index
        CanMessage m = new CanMessage( tcis.getCanid() );
        m.setNumDataElements(3);
        m.setElement(0, CbusConstants.CBUS_RQNPN); 
        m.setElement(1, 0x04); // node 1234
        m.setElement(2, 0xd2); // node 1234
        m.setElement(3, 0x00); // parameter index on node, 0 is total parameters
        t.getCanListener().message(m);        
        
        JUnitUtil.waitFor(()->{ return(!tcis.inbound.isEmpty()); }, " inbound 1 didn't arrive");
        Assert.assertEquals(" 1 inbound increase", 1,(tcis.inbound.size() ) );
        Assert.assertEquals("node 1234 responds parameter index 0 val 20", "[5f8] 9B 04 D2 00 14",
            tcis.inbound.elementAt(tcis.inbound.size() - 1).toString());
        
        m.setElement(3, 0x05); // parameter index on node, 5 is total ev vars per ev
        t.getCanListener().message(m);
        
        JUnitUtil.waitFor(()->{ return(tcis.inbound.size()>1); }, " inbound 2 didn't arrive");
        Assert.assertEquals(" 2 inbound increase", 2,(tcis.inbound.size() ) );
        Assert.assertEquals("node 1234 responds parameter index 5 val 13", "[5f8] 9B 04 D2 05 0D",
            tcis.inbound.elementAt(tcis.inbound.size() - 1).toString());
        
        t.dispose();
        
    }
    
    @Test
    public void testNodeFetchTeachNV() {
        
        // dummy CANPAN
        CbusDummyNode t = new MergCanpan().getNewDummyNode(memo, 1234);
        ((CbusSimCanListener) t.getCanListener()).setDelay(0);
        t.setNodeInFLiMMode(true);
        
        // get node variable
        CanMessage m = new CanMessage( tcis.getCanid() );
        m.setNumDataElements(3);
        m.setElement(0, CbusConstants.CBUS_NVRD); 
        m.setElement(1, 0x04); // node 1234
        m.setElement(2, 0xd2); // node 1234
        m.setElement(3, 0x00); // NV index on node, 0 is total node variables
        t.getCanListener().message(m);        
        
        JUnitUtil.waitFor(()->{ return(!tcis.inbound.isEmpty()); }, " inbound 1 didn't arrive");
        Assert.assertEquals(" 1 inbound increase", 1,(tcis.inbound.size() ) );
        Assert.assertEquals("node 1234 responds nv index 0 val 1", "[5f8] 97 04 D2 00 01",
            tcis.inbound.elementAt(tcis.inbound.size() - 1).toString());
        
        m.setElement(3, 0x01); // nv index on node
        t.getCanListener().message(m);
        
        JUnitUtil.waitFor(()->{ return(tcis.inbound.size()>1); }, " inbound 2 didn't arrive");
        Assert.assertEquals(" 2 inbound increase", 2,(tcis.inbound.size() ) );
        Assert.assertEquals("node 1234 responds nv index 1 val 0", "[5f8] 97 04 D2 01 00",
            tcis.inbound.elementAt(tcis.inbound.size() - 1).toString());
        
        
        // set NV1 to 02 ( all states ON )
        m = new CanMessage( tcis.getCanid() );
        m.setNumDataElements(5);
        m.setElement(0, CbusConstants.CBUS_NVSET); 
        m.setElement(1, 0x04); // node 1234
        m.setElement(2, 0xd2); // node 1234
        m.setElement(3, 0x01); // NV index on node
        m.setElement(4, 0x02); // new NV value
        t.getCanListener().message(m); 
        
        // note CANPAN does not confirm NVSET so no response from node
        
        // re-request NV value 1 to confirm changed and no unexpected inbound
        m = new CanMessage( tcis.getCanid() );
        m.setNumDataElements(3);
        m.setElement(0, CbusConstants.CBUS_NVRD); 
        m.setElement(1, 0x04); // node 1234
        m.setElement(2, 0xd2); // node 1234
        m.setElement(3, 0x01); // NV index on node
        t.getCanListener().message(m);        
        
        JUnitUtil.waitFor(()->{ return(tcis.inbound.size()>2); }, " inbound 3 didn't arrive");
        Assert.assertEquals(" 3 inbound increase", 3,(tcis.inbound.size() ) );
        Assert.assertEquals("node 1234 responds nv index 1 val 2", "[5f8] 97 04 D2 01 02",
            tcis.inbound.elementAt(tcis.inbound.size() - 1).toString());
        
        // set NV1 to 255 ( invalid )
        m = new CanMessage( tcis.getCanid() );
        m.setNumDataElements(5);
        m.setElement(0, CbusConstants.CBUS_NVSET); 
        m.setElement(1, 0x04); // node 1234
        m.setElement(2, 0xd2); // node 1234
        m.setElement(3, 0x77); // NV index on node
        m.setElement(4, 0xff); // new NV value
        t.getCanListener().message(m); 
        
        JUnitUtil.waitFor(()->{ return(tcis.inbound.size()>3); }, " inbound 4 didn't arrive");
        Assert.assertEquals(" 4 inbound increase", 4,(tcis.inbound.size() ) );
        Assert.assertEquals("node 1234 nv index 77 val ff error 10 Invalid NV Index", "[5f8] 6F 04 D2 0A",
            tcis.inbound.elementAt(tcis.inbound.size() - 1).toString());
        
        m.setElement(3, 0x00); // NV index on node
        m.setElement(4, 0x01); // new NV value
        t.getCanListener().message(m); 
        
        JUnitUtil.waitFor(()->{ return(tcis.inbound.size()>4); }, " inbound 5 didn't arrive");
        Assert.assertEquals(" 5 inbound increase", 5,(tcis.inbound.size() ) );
        Assert.assertEquals("node 1234 nv index 0 val 01 error 10 Invalid NV Index", "[5f8] 6F 04 D2 0A",
            tcis.inbound.elementAt(tcis.inbound.size() - 1).toString());
        
        t.dispose();
        
    }
    
    @Test
    public void testNodeLearnEvent() {
        
        CbusDummyNode t = new MergCanpan().getNewDummyNode(memo, 1234);
        ((CbusSimCanListener) t.getCanListener()).setDelay(0);
        t.setNodeInFLiMMode(true);
        
        Assert.assertEquals(" 0 outbound not increased", 0,(tcis.outbound.size() ) );
        Assert.assertEquals(" 0 inbound  not increased", 0,(tcis.inbound.size() ) );
        
        // frame to set node into learn
        CanMessage m = new CanMessage( tcis.getCanid() );
        m.setNumDataElements(3);
        m.setElement(0, CbusConstants.CBUS_NNLRN); 
        m.setElement(1, 0x04);
        m.setElement(2, 0xd2);
        t.getCanListener().message(m);
        Assert.assertTrue("message enter learn mode ",t.getNodeInLearnMode() );
        
        
        Assert.assertEquals("Initial FLiM getTotalNodeEvents ",-1,t.getNodeEventManager().getTotalNodeEvents() );
        
        // teach the node an event ( CANPAN has 13 event variables hence the loop )
        m = new CanMessage( tcis.getCanid() );
        m.setNumDataElements(7);
        m.setElement(0, CbusConstants.CBUS_EVLRN); 
        m.setElement(1, 0x00); // node 4
        m.setElement(2, 0x04); // node 4
        m.setElement(3, 0x00); // event 1
        m.setElement(4, 0x01); // event 1
        m.setElement(5, 0x01); // event variable index - NOT event index on node
        m.setElement(6, 0x04); // event variable value
        t.getCanListener().message(m);
        
        JUnitUtil.waitFor(()->{ return(!tcis.inbound.isEmpty()); }, " inbound 1 didn't arrive");
        Assert.assertEquals(" 1 inbound increased", 1,(tcis.inbound.size() ) );
        
        Assert.assertTrue("1 getTotalNodeEvents ",t.getNodeEventManager().getTotalNodeEvents()== 1 );
        Assert.assertEquals("node responds to learn event with write acknowledge", "[5f8] 59 04 D2",
            tcis.inbound.elementAt(tcis.inbound.size() - 1).toString());
        
        Assert.assertTrue("after new ev getOutstandingEvVars 1",t.getNodeEventManager().getOutstandingEvVars()== 12 );
        
        m.setElement(5, 0x02); // event variable index - NOT event index on node
        m.setElement(6, 0x02); // event variable value
        t.getCanListener().message(m);
        
        m.setElement(5, 0x03); // event variable index - NOT event index on node
        m.setElement(6, 0x03); // event variable value
        t.getCanListener().message(m);
        
        m.setElement(5, 0x04); // event variable index - NOT event index on node
        m.setElement(6, 0xff); // event variable value
        t.getCanListener().message(m);
        
        m.setElement(5, 0x05); // event variable index - NOT event index on node
        m.setElement(6, 0x05); // event variable value
        t.getCanListener().message(m);
        
        m.setElement(5, 0x06); // event variable index - NOT event index on node
        m.setElement(6, 0x06); // event variable value
        t.getCanListener().message(m);
        
        m.setElement(5, 0x07); // event variable index - NOT event index on node
        m.setElement(6, 0x07); // event variable value
        t.getCanListener().message(m);
        
        m.setElement(5, 0x08); // event variable index - NOT event index on node
        m.setElement(6, 0x08); // event variable value
        t.getCanListener().message(m);
        
        m.setElement(5, 0x09); // event variable index - NOT event index on node
        m.setElement(6, 0x09); // event variable value
        t.getCanListener().message(m);
        
        m.setElement(5, 0x0a); // event variable index - NOT event index on node
        m.setElement(6, 0x0a); // event variable value
        t.getCanListener().message(m);
        
        m.setElement(5, 0x0b); // event variable index - NOT event index on node
        m.setElement(6, 0x0b); // event variable value
        t.getCanListener().message(m);
        
        m.setElement(5, 0x0c); // event variable index - NOT event index on node
        m.setElement(6, 0x0c); // event variable value
        t.getCanListener().message(m);
        
        m.setElement(5, 0x0d); // event variable index - NOT event index on node
        m.setElement(6, 0x0d); // event variable value
        t.getCanListener().message(m);
        
        JUnitUtil.waitFor(()->{ return(tcis.inbound.size()>12); }, " inbound 13 didn't arrive");
        // all acknowledgments received from node, assume all WRACKS
        Assert.assertEquals(" 13 inbound increased", 13,(tcis.inbound.size() ) );
        
        Assert.assertTrue("still 1 getTotalNodeEvents ",t.getNodeEventManager().getTotalNodeEvents()== 1 );
        
        Assert.assertTrue("after new ev getOutstandingEvVars 0",t.getNodeEventManager().getOutstandingEvVars()== 0 );
        
        
        // exit learn mode
        m = new CanMessage( tcis.getCanid() );
        m.setNumDataElements(3);
        m.setElement(0, CbusConstants.CBUS_NNULN); 
        m.setElement(1, 0x04);
        m.setElement(2, 0xd2);
        t.getCanListener().message(m);
        Assert.assertFalse("message exit learn mode ",t.getNodeInLearnMode() );
        
        // ask node how many events it has
        m = new CanMessage( tcis.getCanid() );
        m.setNumDataElements(3);
        m.setElement(0, CbusConstants.CBUS_RQEVN); 
        m.setElement(1, 0x04); // node 1234
        m.setElement(2, 0xd2); // node 1234
        t.getCanListener().message(m);
        
        JUnitUtil.waitFor(()->{ return(tcis.inbound.size()>13); }, " inbound 14 didn't arrive");
        Assert.assertEquals(" 14 inbound increased", 14,(tcis.inbound.size() ) );
        Assert.assertEquals("node responds to request number of events", "[5f8] 74 04 D2 01",
            tcis.inbound.elementAt(tcis.inbound.size() - 1).toString());
        
        // get events stored on node
        m = new CanMessage( tcis.getCanid() );
        m.setNumDataElements(3);
        m.setElement(0, CbusConstants.CBUS_NERD); 
        m.setElement(1, 0x04); // node 1234
        m.setElement(2, 0xd2); // node 1234
        t.getCanListener().message(m);
        
        JUnitUtil.waitFor(()->{ return(tcis.inbound.size()>14); }, " inbound 15 didn't arrive");
        Assert.assertEquals(" 15 inbound increased", 15,(tcis.inbound.size() ) );
        Assert.assertEquals("node responds to all event index fetch request", "[5f8] F2 04 D2 00 04 00 01 01",
            tcis.inbound.elementAt(tcis.inbound.size() - 1).toString());
        
        // get event variables with knowledge of index
        m = new CanMessage( tcis.getCanid() );
        m.setNumDataElements(5);
        m.setElement(0, CbusConstants.CBUS_REVAL); 
        m.setElement(1, 0x04); // node 1234
        m.setElement(2, 0xd2); // node 1234
        m.setElement(3, 0x01); // event index on node
        m.setElement(4, 0x01); // event variable index on event
        t.getCanListener().message(m);
        
        JUnitUtil.waitFor(()->{ return(tcis.inbound.size()>15); }, " inbound 16 didn't arrive");
        Assert.assertEquals(" 16 inbound increased", 16,(tcis.inbound.size() ) );
        Assert.assertEquals("node 1234 responds event index 1 var 1", "[5f8] B5 04 D2 01 01 04",
            tcis.inbound.elementAt(tcis.inbound.size() - 1).toString());
        
        m.setElement(4, 0x04); // event variable index on event
        t.getCanListener().message(m);        
        
        JUnitUtil.waitFor(()->{ return(tcis.inbound.size()>16); }, " inbound 17 didn't arrive");
        Assert.assertEquals(" 17 inbound increased", 17,(tcis.inbound.size() ) );
        Assert.assertEquals("node 1234 responds event index 1 var 1", "[5f8] B5 04 D2 01 04 FF",
            tcis.inbound.elementAt(tcis.inbound.size() - 1).toString());
        
        // put node into learn mode
        m = new CanMessage( tcis.getCanid() );
        m.setNumDataElements(3);
        m.setElement(0, CbusConstants.CBUS_NNLRN); 
        m.setElement(1, 0x04);
        m.setElement(2, 0xd2);
        t.getCanListener().message(m);
        
        // delete event on node
        m = new CanMessage( tcis.getCanid() );
        m.setNumDataElements(5);
        m.setElement(0, CbusConstants.CBUS_EVULN); 
        m.setElement(1, 0x00); // node 4
        m.setElement(2, 0x04); // node 4
        m.setElement(3, 0x00); // event 1
        m.setElement(4, 0x01); // event 1
        t.getCanListener().message(m);
        
        // exit learn mode
        m = new CanMessage( tcis.getCanid() );
        m.setNumDataElements(3);
        m.setElement(0, CbusConstants.CBUS_NNULN); 
        m.setElement(1, 0x04);
        m.setElement(2, 0xd2);
        t.getCanListener().message(m);
        
        // ask node how many events it has
        m = new CanMessage( tcis.getCanid() );
        m.setNumDataElements(3);
        m.setElement(0, CbusConstants.CBUS_RQEVN); 
        m.setElement(1, 0x04); // node 1234
        m.setElement(2, 0xd2); // node 1234
        t.getCanListener().message(m);
        
        JUnitUtil.waitFor(()->{ return(tcis.inbound.size()>17); }, " inbound 18 didn't arrive");
        Assert.assertEquals(" 18 inbound increased", 18,(tcis.inbound.size() ) );
        Assert.assertEquals("node responds to request number of events", "[5f8] 74 04 D2 00",
            tcis.inbound.elementAt(tcis.inbound.size() - 1).toString());
        
        t.dispose();
        
    }
    
    @Test
    public void testNodeLearnTwoEventsThenDelete() {
    
        CbusDummyNode t = new MergCanpan().getNewDummyNode(memo, 1234);
        ((CbusSimCanListener) t.getCanListener()).setDelay(0);
        t.setNodeInFLiMMode(true);
        
        // frame to set node into learn
        CanMessage m = new CanMessage( tcis.getCanid() );
        m.setNumDataElements(3);
        m.setElement(0, CbusConstants.CBUS_NNLRN); 
        m.setElement(1, 0x04);
        m.setElement(2, 0xd2);
        t.getCanListener().message(m);
        
        // teach the node an event
        m = new CanMessage( tcis.getCanid() );
        m.setNumDataElements(7);
        m.setElement(0, CbusConstants.CBUS_EVLRN); 
        m.setElement(1, 0x00); // node 4
        m.setElement(2, 0x04); // node 4
        m.setElement(3, 0x00); // event 1
        m.setElement(4, 0x01); // event 1
        m.setElement(5, 0x01); // event variable index - NOT event index on node
        m.setElement(6, 0x04); // event variable value
        t.getCanListener().message(m);
    
        // teach the node a different event
        m = new CanMessage( tcis.getCanid() );
        m.setNumDataElements(7);
        m.setElement(0, CbusConstants.CBUS_EVLRN); 
        m.setElement(1, 0x00); // node 4
        m.setElement(2, 0x04); // node 4
        m.setElement(3, 0x00); // event 1
        m.setElement(4, 0x02); // event 2
        m.setElement(5, 0x01); // event variable index - NOT event index on node
        m.setElement(6, 0x04); // event variable value
        t.getCanListener().message(m);
        
        // exit learn mode
        m = new CanMessage( tcis.getCanid() );
        m.setNumDataElements(3);
        m.setElement(0, CbusConstants.CBUS_NNULN); 
        m.setElement(1, 0x04);
        m.setElement(2, 0xd2);
        t.getCanListener().message(m);
        
        // ask node how many events it has
        m = new CanMessage( tcis.getCanid() );
        m.setNumDataElements(3);
        m.setElement(0, CbusConstants.CBUS_RQEVN); 
        m.setElement(1, 0x04); // node 1234
        m.setElement(2, 0xd2); // node 1234
        t.getCanListener().message(m);
        
        JUnitUtil.waitFor(()->{ return(tcis.inbound.size()>2); }, " inbound 3 didn't arrive");
        Assert.assertEquals(" 3 inbound increased", 3,(tcis.inbound.size() ) );
        Assert.assertEquals("node responds to request number of events 2", "[5f8] 74 04 D2 02",
            tcis.inbound.elementAt(tcis.inbound.size() - 1).toString());
    
        // frame to set node into learn
        m = new CanMessage( tcis.getCanid() );
        m.setNumDataElements(3);
        m.setElement(0, CbusConstants.CBUS_NNLRN); 
        m.setElement(1, 0x04);
        m.setElement(2, 0xd2);
        t.getCanListener().message(m);
        
        // clear all events
        m = new CanMessage( tcis.getCanid() );
        m.setNumDataElements(3);
        m.setElement(0, CbusConstants.CBUS_NNCLR); 
        m.setElement(1, 0x04);
        m.setElement(2, 0xd2);
        t.getCanListener().message(m);
        
        // exit learn mode
        m = new CanMessage( tcis.getCanid() );
        m.setNumDataElements(3);
        m.setElement(0, CbusConstants.CBUS_NNULN); 
        m.setElement(1, 0x04);
        m.setElement(2, 0xd2);
        t.getCanListener().message(m);
        
        // ask node how many events it has
        m = new CanMessage( tcis.getCanid() );
        m.setNumDataElements(3);
        m.setElement(0, CbusConstants.CBUS_RQEVN); 
        m.setElement(1, 0x04); // node 1234
        m.setElement(2, 0xd2); // node 1234
        t.getCanListener().message(m);
        
        JUnitUtil.waitFor(()->{ return(tcis.inbound.size()>3); }, " inbound 4 didn't arrive");
        Assert.assertEquals(" 4 inbound increased", 4,(tcis.inbound.size() ) );
        Assert.assertEquals("node responds 0 events following CBUS_NNCLR", "[5f8] 74 04 D2 00",
            tcis.inbound.elementAt(tcis.inbound.size() - 1).toString());
    
        t.dispose();
    
    }
    
    @Test
    public void testResponseToNameRequest() {
    
        CbusDummyNode t = new MergCanpan().getNewDummyNode(memo, 1234);
        ((CbusSimCanListener) t.getCanListener()).setDelay(0);
        t.setNodeInFLiMMode(true);
        t.setNodeInSetupMode(true);
        
        CanMessage m = new CanMessage( tcis.getCanid() );
        m.setNumDataElements(1);
        m.setElement(0, CbusConstants.CBUS_RQMN);
        t.getCanListener().message(m);
        
        
        JUnitUtil.waitFor(()->{ return(!tcis.inbound.isEmpty()); }, " inbound 1 didn't arrive");
        Assert.assertEquals(" 1 inbound increased", 1,(tcis.inbound.size() ) );
        Assert.assertEquals("node responds to RQMN 0", "[5f8] E2 50 41 4E 20 20 20 20",
            tcis.inbound.elementAt(tcis.inbound.size() - 1).toString());
        
        t.dispose();
        
    }
    
    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        
        memo = new CanSystemConnectionMemo();
        tcis = new TrafficControllerScaffold();
        memo.setTrafficController(tcis);
        
    }

    @AfterEach
    public void tearDown() {
        
        tcis.terminateThreads();
        memo.dispose();
        tcis = null;
        memo = null;
        
        JUnitUtil.tearDown();

    }

    // private final static Logger log = LoggerFactory.getLogger(CbusDummyNodeTest.class);

}
