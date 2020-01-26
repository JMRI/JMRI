package jmri.jmrix.can.cbus.simulator;

import jmri.jmrix.can.CanMessage;
import jmri.jmrix.can.CanReply;
import jmri.jmrix.can.CanSystemConnectionMemo;
import jmri.jmrix.can.cbus.CbusConstants;
import jmri.jmrix.can.TrafficControllerScaffold;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 * @author Steve Young Copyright (C) 2019
 */
public class CbusDummyNodeTest {

    TrafficControllerScaffold tcis;

    @Test
    public void testCTor() {
        CbusDummyNode t = new CbusDummyNode(1,2,3,4,null);
        Assert.assertNotNull("exists",t);
        t.dispose();
    }
    
    @Test
    public void testListeners() {
        
        CanSystemConnectionMemo memo = new CanSystemConnectionMemo();
        TrafficControllerScaffold tcis = new TrafficControllerScaffold();
        memo.setTrafficController(tcis);
        Assert.assertTrue("0 listeners",tcis.numListeners()==0);
        CbusDummyNode t = new CbusDummyNode(1,2,3,4,memo);
        
        Assert.assertTrue("1 listener",tcis.numListeners()==1);
        
        Assert.assertTrue("start getDelay",t.getDelay()>0);
        t.setDelay(7);
        Assert.assertEquals("getSetDelay", 7,t.getDelay());
        
        t.dispose();
        t = null;
        Assert.assertTrue("0 listeners after dispose",tcis.numListeners()==0);
        tcis=null;
        memo = null;
    }
    
    @Test
    public void testNodeRequestNumber() {
        
        CanSystemConnectionMemo memo = new CanSystemConnectionMemo();
        tcis = new TrafficControllerScaffold();
        memo.setTrafficController(tcis);
        
        CbusDummyNode t = new CbusDummyNode(1,2,3,4,memo); // nn, manufacturer, type, canid, memo
        t.setDelay(0);
        
        Assert.assertEquals("start getProcessIn", false,t.getProcessIn() );
        Assert.assertEquals("start getProcessOut", true,t.getProcessOut() );
        Assert.assertEquals("start getSendIn", true,t.getSendIn() );
        Assert.assertEquals("start getSendOut", false,t.getSendOut() );
        Assert.assertTrue("start getsendsWRACKonNVSET",t.getsendsWRACKonNVSET()==true);
        Assert.assertEquals(" getNodeType SLIM", "SLIM",t.getNodeTypeName() );
        
        // set node to CANPAN from SLIM
        t.setDummyType(165,29);
        
        Assert.assertTrue("is a CANPAN getsendsWRACKonNVSET",t.getsendsWRACKonNVSET()==false);
        Assert.assertEquals(" getNodeType", "CANPAN",t.getNodeTypeName() );
        Assert.assertTrue("canpan getTotalNVs 0",t.getTotalNVs()== 1 );
        
        t.flimButton();
        
        JUnitUtil.waitFor(()->{ return(tcis.inbound.size()>0); }, " reply didn't arrive");
        
        Assert.assertEquals(" 0 outbound not increased", 0,(tcis.outbound.size()));
        Assert.assertEquals(" 1 inbound nodenum request ", 1,(tcis.inbound.size()));
        
        Assert.assertEquals("node request inbound", "[5f8] 50 00 01",
            tcis.inbound.elementAt(tcis.inbound.size() - 1).toString());
        // node in setup mode awaiting node allocation frame or further setup info request
        
        t.setSendOut(true);
        t.setSendIn(false);
        Assert.assertEquals(" getSendOut", true,t.getSendOut() );
        Assert.assertEquals(" getSendIn", false,t.getSendIn() );
        
        t.setProcessIn(true);
        t.setProcessOut(false);
        
        Assert.assertEquals("start getProcessIn", true,t.getProcessIn() );
        Assert.assertEquals("start getProcessOut", false,t.getProcessOut() );
        
        CanReply r = new CanReply();
        r.setHeader(tcis.getCanid());
        r.setNumDataElements(1);
        r.setElement(0, CbusConstants.CBUS_RQNP); 
        t.reply(r);
        
        CanMessage m = new CanMessage( tcis.getCanid() );
        m.setNumDataElements(1);
        m.setElement(0, CbusConstants.CBUS_RQNP);
        t.message(m); // should be ignored
        
        JUnitUtil.waitFor(()->{ return(tcis.outbound.size()>0); }, " reply didn't arrive");
        
        Assert.assertEquals(" 1 outbound params request", 1,(tcis.outbound.size() ) );
        Assert.assertEquals(" 1 inbound not increased ", 1,(tcis.inbound.size() ) );
        
        Assert.assertEquals("node responds with parameters setup", "[5f8] EF A5 59 1D 80 0D 01 01",
            tcis.outbound.elementAt(tcis.outbound.size() - 1).toString());
        
        t.setProcessIn(false);
        t.setProcessOut(true);
        
        t.setSendOut(false);
        t.setSendIn(true);
        Assert.assertEquals(" getSendOut f", false,t.getSendOut() );
        Assert.assertEquals(" getSendIn t", true,t.getSendIn() );
        
        r = new CanReply();
        r.setHeader(tcis.getCanid());
        r.setNumDataElements(3);
        r.setElement(0, CbusConstants.CBUS_SNN);
        r.setElement(1, 0xb1); // nn
        r.setElement(2, 0x2c); // nn
        t.reply(r); // should be ignored
        
        m = new CanMessage( tcis.getCanid() );
        m.setNumDataElements(3);
        m.setElement(0, CbusConstants.CBUS_SNN);
        m.setElement(1, 0xb1);
        m.setElement(2, 0x2c);
        t.message(m);
        
        JUnitUtil.waitFor(()->{ return(tcis.inbound.size()>1); }, " inbound 2 didn't arrive");
        
        Assert.assertEquals(" 1 outbound not increased", 1,(tcis.outbound.size() ) );
        Assert.assertEquals(" 2 inbound  ", 2,(tcis.inbound.size() ) );
        
        Assert.assertEquals("node responds confirming node number", "[5f8] 52 B1 2C",
            tcis.inbound.elementAt(tcis.inbound.size() - 1).toString());
        
        m = new CanMessage( tcis.getCanid() );
        m.setNumDataElements(1);
        m.setElement(0, CbusConstants.CBUS_QNN);
        t.message(m);
        
        Assert.assertEquals(" 1 outbound not increased", 1,(tcis.outbound.size() ) );
        JUnitUtil.waitFor(()->{ return(tcis.inbound.size()>2); }, " inbound 3 didn't arrive");
        Assert.assertEquals(" 3 inbound  ", 3,(tcis.inbound.size() ) );
        
        Assert.assertEquals("node responds to QNN with PNN", "[5f8] B6 B1 2C A5 1D 0D",
            tcis.inbound.elementAt(tcis.inbound.size() - 1).toString());
        
        // set node to type 0 SLIM inbound and outbound listeners
        t.setDummyType(165,0);
        t.message(m);
        
        Assert.assertEquals(" 1 outbound not increased", 1,(tcis.outbound.size() ) );
        Assert.assertEquals(" 3 inbound  not increased", 3,(tcis.inbound.size() ) );
        
        t.flimButton();  // does nothing in node type 0
        
        Assert.assertEquals(" 1 outbound not increased", 1,(tcis.outbound.size() ) );
        Assert.assertEquals(" 3 inbound  not increased", 3,(tcis.inbound.size() ) );
        
        t.dispose();
        t = null;
        tcis=null;
        memo = null;
        
    }
    
    @Test
    public void testNodeFetchParameters() {
        
        CanSystemConnectionMemo memo = new CanSystemConnectionMemo();
        tcis = new TrafficControllerScaffold();
        memo.setTrafficController(tcis);
        
        // dummy CANPAN
        CbusDummyNode t = new CbusDummyNode(1234,165,29,4,memo); // nn, manufacturer, type, canid, memo
        t.setDelay(0);
        t.setNodeInFLiMMode(true);
        
        // get event variables with knowledge of index
        CanMessage m = new CanMessage( tcis.getCanid() );
        m.setNumDataElements(3);
        m.setElement(0, CbusConstants.CBUS_RQNPN); 
        m.setElement(1, 0x04); // node 1234
        m.setElement(2, 0xd2); // node 1234
        m.setElement(3, 0x00); // parameter index on node, 0 is total parameters
        t.message(m);        
        
        JUnitUtil.waitFor(()->{ return(tcis.inbound.size()>0); }, " inbound 1 didn't arrive");
        Assert.assertEquals(" 1 inbound increase", 1,(tcis.inbound.size() ) );
        Assert.assertEquals("node 1234 responds parameter index 0 val 20", "[5f8] 9B 04 D2 00 14",
            tcis.inbound.elementAt(tcis.inbound.size() - 1).toString());
        
        m.setElement(3, 0x05); // parameter index on node, 5 is total ev vars per ev
        t.message(m);
        
        JUnitUtil.waitFor(()->{ return(tcis.inbound.size()>1); }, " inbound 2 didn't arrive");
        Assert.assertEquals(" 2 inbound increase", 2,(tcis.inbound.size() ) );
        Assert.assertEquals("node 1234 responds parameter index 5 val 13", "[5f8] 9B 04 D2 05 0D",
            tcis.inbound.elementAt(tcis.inbound.size() - 1).toString());
        
        m = null;
        t.dispose();
        t = null;
        tcis=null;
        memo = null;
        
    }
    
    @Test
    public void testNodeFetchTeachNV() {
        
        CanSystemConnectionMemo memo = new CanSystemConnectionMemo();
        tcis = new TrafficControllerScaffold();
        memo.setTrafficController(tcis);
        
        // dummy CANPAN
        CbusDummyNode t = new CbusDummyNode(1234,165,29,4,memo); // nn, manufacturer, type, canid, memo
        t.setDelay(0);
        t.setNodeInFLiMMode(true);
        
        // get node variable
        CanMessage m = new CanMessage( tcis.getCanid() );
        m.setNumDataElements(3);
        m.setElement(0, CbusConstants.CBUS_NVRD); 
        m.setElement(1, 0x04); // node 1234
        m.setElement(2, 0xd2); // node 1234
        m.setElement(3, 0x00); // NV index on node, 0 is total node variables
        t.message(m);        
        
        JUnitUtil.waitFor(()->{ return(tcis.inbound.size()>0); }, " inbound 1 didn't arrive");
        Assert.assertEquals(" 1 inbound increase", 1,(tcis.inbound.size() ) );
        Assert.assertEquals("node 1234 responds nv index 0 val 1", "[5f8] 97 04 D2 00 01",
            tcis.inbound.elementAt(tcis.inbound.size() - 1).toString());
        
        m.setElement(3, 0x01); // nv index on node
        t.message(m);
        
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
        t.message(m); 
        
        // note CANPAN does not confirm NVSET so no response from node
        
        // re-request NV value 1 to confirm changed and no unexpected inbound
        m = new CanMessage( tcis.getCanid() );
        m.setNumDataElements(3);
        m.setElement(0, CbusConstants.CBUS_NVRD); 
        m.setElement(1, 0x04); // node 1234
        m.setElement(2, 0xd2); // node 1234
        m.setElement(3, 0x01); // NV index on node
        t.message(m);        
        
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
        t.message(m); 
        
        JUnitUtil.waitFor(()->{ return(tcis.inbound.size()>3); }, " inbound 4 didn't arrive");
        Assert.assertEquals(" 4 inbound increase", 4,(tcis.inbound.size() ) );
        Assert.assertEquals("node 1234 nv index 77 val ff error 10 Invalid NV Index", "[5f8] 6F 04 D2 0A",
            tcis.inbound.elementAt(tcis.inbound.size() - 1).toString());
        
        m.setElement(3, 0x00); // NV index on node
        m.setElement(4, 0x01); // new NV value
        t.message(m); 
        
        JUnitUtil.waitFor(()->{ return(tcis.inbound.size()>4); }, " inbound 5 didn't arrive");
        Assert.assertEquals(" 5 inbound increase", 5,(tcis.inbound.size() ) );
        Assert.assertEquals("node 1234 nv index 0 val 01 error 10 Invalid NV Index", "[5f8] 6F 04 D2 0A",
            tcis.inbound.elementAt(tcis.inbound.size() - 1).toString());
        
        m = null;
        t.dispose();
        t = null;
        tcis=null;
        memo = null;
        
    }
    
    @Test
    public void testNodeLearnEvent() {
        
        CanSystemConnectionMemo memo = new CanSystemConnectionMemo();
        tcis = new TrafficControllerScaffold();
        memo.setTrafficController(tcis);
        
        CbusDummyNode t = new CbusDummyNode(1234,165,29,4,memo); // nn, manufacturer, type, canid, memo
        t.setDelay(0);
        t.setNodeInFLiMMode(true);
        
        Assert.assertEquals(" 0 outbound not increased", 0,(tcis.outbound.size() ) );
        Assert.assertEquals(" 0 inbound  not increased", 0,(tcis.inbound.size() ) );
        
        // frame to set node into learn
        CanMessage m = new CanMessage( tcis.getCanid() );
        m.setNumDataElements(3);
        m.setElement(0, CbusConstants.CBUS_NNLRN); 
        m.setElement(1, 0x04);
        m.setElement(2, 0xd2);
        t.message(m);
        Assert.assertTrue("message enter learn mode ",t.getNodeInLearnMode() );
        
        
        Assert.assertTrue("start getTotalNodeEvents ",t.getTotalNodeEvents()== 0 );
        
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
        t.message(m);
        
        JUnitUtil.waitFor(()->{ return(tcis.inbound.size()>0); }, " inbound 1 didn't arrive");
        Assert.assertEquals(" 1 inbound increased", 1,(tcis.inbound.size() ) );
        
        Assert.assertTrue("1 getTotalNodeEvents ",t.getTotalNodeEvents()== 1 );
        Assert.assertEquals("node responds to learn event with write acknowledge", "[5f8] 59 04 D2",
            tcis.inbound.elementAt(tcis.inbound.size() - 1).toString());
        
        Assert.assertTrue("after new ev getOutstandingEvVars 1",t.getOutstandingEvVars()== 12 );
        
        m.setElement(5, 0x02); // event variable index - NOT event index on node
        m.setElement(6, 0x02); // event variable value
        t.message(m);
        
        m.setElement(5, 0x03); // event variable index - NOT event index on node
        m.setElement(6, 0x03); // event variable value
        t.message(m);
        
        m.setElement(5, 0x04); // event variable index - NOT event index on node
        m.setElement(6, 0xff); // event variable value
        t.message(m);
        
        m.setElement(5, 0x05); // event variable index - NOT event index on node
        m.setElement(6, 0x05); // event variable value
        t.message(m);
        
        m.setElement(5, 0x06); // event variable index - NOT event index on node
        m.setElement(6, 0x06); // event variable value
        t.message(m);
        
        m.setElement(5, 0x07); // event variable index - NOT event index on node
        m.setElement(6, 0x07); // event variable value
        t.message(m);
        
        m.setElement(5, 0x08); // event variable index - NOT event index on node
        m.setElement(6, 0x08); // event variable value
        t.message(m);
        
        m.setElement(5, 0x09); // event variable index - NOT event index on node
        m.setElement(6, 0x09); // event variable value
        t.message(m);
        
        m.setElement(5, 0x0a); // event variable index - NOT event index on node
        m.setElement(6, 0x0a); // event variable value
        t.message(m);
        
        m.setElement(5, 0x0b); // event variable index - NOT event index on node
        m.setElement(6, 0x0b); // event variable value
        t.message(m);
        
        m.setElement(5, 0x0c); // event variable index - NOT event index on node
        m.setElement(6, 0x0c); // event variable value
        t.message(m);
        
        m.setElement(5, 0x0d); // event variable index - NOT event index on node
        m.setElement(6, 0x0d); // event variable value
        t.message(m);
        
        JUnitUtil.waitFor(()->{ return(tcis.inbound.size()>12); }, " inbound 13 didn't arrive");
        // all acknowledgments received from node, assume all WRACKS
        Assert.assertEquals(" 13 inbound increased", 13,(tcis.inbound.size() ) );
        
        Assert.assertTrue("still 1 getTotalNodeEvents ",t.getTotalNodeEvents()== 1 );
        
        Assert.assertTrue("after new ev getOutstandingEvVars 0",t.getOutstandingEvVars()== 0 );
        
        
        // exit learn mode
        m = new CanMessage( tcis.getCanid() );
        m.setNumDataElements(3);
        m.setElement(0, CbusConstants.CBUS_NNULN); 
        m.setElement(1, 0x04);
        m.setElement(2, 0xd2);
        t.message(m);
        Assert.assertFalse("message exit learn mode ",t.getNodeInLearnMode() );
        
        // ask node how many events it has
        m = new CanMessage( tcis.getCanid() );
        m.setNumDataElements(3);
        m.setElement(0, CbusConstants.CBUS_RQEVN); 
        m.setElement(1, 0x04); // node 1234
        m.setElement(2, 0xd2); // node 1234
        t.message(m);
        
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
        t.message(m);
        
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
        t.message(m);
        
        JUnitUtil.waitFor(()->{ return(tcis.inbound.size()>15); }, " inbound 16 didn't arrive");
        Assert.assertEquals(" 16 inbound increased", 16,(tcis.inbound.size() ) );
        Assert.assertEquals("node 1234 responds event index 1 var 1", "[5f8] B5 04 D2 01 01 04",
            tcis.inbound.elementAt(tcis.inbound.size() - 1).toString());
        
        m.setElement(4, 0x04); // event variable index on event
        t.message(m);        
        
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
        t.message(m);
        
        // delete event on node
        m = new CanMessage( tcis.getCanid() );
        m.setNumDataElements(5);
        m.setElement(0, CbusConstants.CBUS_EVULN); 
        m.setElement(1, 0x00); // node 4
        m.setElement(2, 0x04); // node 4
        m.setElement(3, 0x00); // event 1
        m.setElement(4, 0x01); // event 1
        t.message(m);
        
        // exit learn mode
        m = new CanMessage( tcis.getCanid() );
        m.setNumDataElements(3);
        m.setElement(0, CbusConstants.CBUS_NNULN); 
        m.setElement(1, 0x04);
        m.setElement(2, 0xd2);
        t.message(m);
        
        // ask node how many events it has
        m = new CanMessage( tcis.getCanid() );
        m.setNumDataElements(3);
        m.setElement(0, CbusConstants.CBUS_RQEVN); 
        m.setElement(1, 0x04); // node 1234
        m.setElement(2, 0xd2); // node 1234
        t.message(m);
        
        JUnitUtil.waitFor(()->{ return(tcis.inbound.size()>17); }, " inbound 18 didn't arrive");
        Assert.assertEquals(" 18 inbound increased", 18,(tcis.inbound.size() ) );
        Assert.assertEquals("node responds to request number of events", "[5f8] 74 04 D2 00",
            tcis.inbound.elementAt(tcis.inbound.size() - 1).toString());
        
        m = null;
        t.dispose();
        t = null;
        tcis=null;
        memo = null;
        
    }
    
    @Test
    public void testNodeLearnTwoEventsThenDelete() {
    
        CanSystemConnectionMemo memo = new CanSystemConnectionMemo();
        tcis = new TrafficControllerScaffold();
        memo.setTrafficController(tcis);
        
        CbusDummyNode t = new CbusDummyNode(1234,165,29,4,memo); // nn, manufacturer, type, canid, memo
        t.setDelay(0);
        t.setNodeInFLiMMode(true);
        
        // frame to set node into learn
        CanMessage m = new CanMessage( tcis.getCanid() );
        m.setNumDataElements(3);
        m.setElement(0, CbusConstants.CBUS_NNLRN); 
        m.setElement(1, 0x04);
        m.setElement(2, 0xd2);
        t.message(m);
        
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
        t.message(m);
    
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
        t.message(m);
        
        // exit learn mode
        m = new CanMessage( tcis.getCanid() );
        m.setNumDataElements(3);
        m.setElement(0, CbusConstants.CBUS_NNULN); 
        m.setElement(1, 0x04);
        m.setElement(2, 0xd2);
        t.message(m);
        
        // ask node how many events it has
        m = new CanMessage( tcis.getCanid() );
        m.setNumDataElements(3);
        m.setElement(0, CbusConstants.CBUS_RQEVN); 
        m.setElement(1, 0x04); // node 1234
        m.setElement(2, 0xd2); // node 1234
        t.message(m);
        
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
        t.message(m);
        
        // clear all events
        m = new CanMessage( tcis.getCanid() );
        m.setNumDataElements(3);
        m.setElement(0, CbusConstants.CBUS_NNCLR); 
        m.setElement(1, 0x04);
        m.setElement(2, 0xd2);
        t.message(m);
        
        // exit learn mode
        m = new CanMessage( tcis.getCanid() );
        m.setNumDataElements(3);
        m.setElement(0, CbusConstants.CBUS_NNULN); 
        m.setElement(1, 0x04);
        m.setElement(2, 0xd2);
        t.message(m);
        
        // ask node how many events it has
        m = new CanMessage( tcis.getCanid() );
        m.setNumDataElements(3);
        m.setElement(0, CbusConstants.CBUS_RQEVN); 
        m.setElement(1, 0x04); // node 1234
        m.setElement(2, 0xd2); // node 1234
        t.message(m);
        
        JUnitUtil.waitFor(()->{ return(tcis.inbound.size()>3); }, " inbound 4 didn't arrive");
        Assert.assertEquals(" 4 inbound increased", 4,(tcis.inbound.size() ) );
        Assert.assertEquals("node responds to request number of events 0", "[5f8] 74 04 D2 00",
            tcis.inbound.elementAt(tcis.inbound.size() - 1).toString());
    
        m = null;
        t.dispose();
        t = null;
        tcis=null;
        memo = null;
    
    }
    
    @Test
    public void testResponseToNameRequest() {
    
        CanSystemConnectionMemo memo = new CanSystemConnectionMemo();
        tcis = new TrafficControllerScaffold();
        memo.setTrafficController(tcis);
        
        CbusDummyNode t = new CbusDummyNode(1234,165,29,4,memo); // nn, manufacturer, type, canid, memo
        t.setDelay(0);
        t.setNodeInFLiMMode(true);
        t.setNodeInSetupMode(true);
        
        CanMessage m = new CanMessage( tcis.getCanid() );
        m.setNumDataElements(1);
        m.setElement(0, CbusConstants.CBUS_RQMN);
        t.message(m);
        
        
        JUnitUtil.waitFor(()->{ return(tcis.inbound.size()>0); }, " inbound 1 didn't arrive");
        Assert.assertEquals(" 1 inbound increased", 1,(tcis.inbound.size() ) );
        Assert.assertEquals("node responds to RQMN 0", "[5f8] E2 50 41 4E 20 20 20 20",
            tcis.inbound.elementAt(tcis.inbound.size() - 1).toString());
        
        m = null;
        t.dispose();
        t = null;
        tcis=null;
        memo = null;
        
    }
    
    // The minimal setup for log4J
    @Before
    public void setUp() {
        JUnitUtil.setUp();
    }

    @After
    public void tearDown() {
        JUnitUtil.clearShutDownManager(); // put in place because AbstractMRTrafficController implementing subclass was not terminated properly
        JUnitUtil.tearDown();

    }

    // private final static Logger log = LoggerFactory.getLogger(CbusDummyNodeTest.class);

}
