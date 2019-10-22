package jmri.jmrix.can.cbus;

import jmri.jmrix.can.CanMessage;
import jmri.jmrix.can.CanSystemConnectionMemo;
import jmri.jmrix.can.TrafficControllerScaffold;
import jmri.util.JUnitUtil;
import jmri.util.swing.TextAreaFIFO;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

// import org.slf4j.Logger;
// import org.slf4j.LoggerFactory;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 * @author Steve Young Copyright (C) 2019
 */
public class CbusSendTest {

    private TrafficControllerScaffold tcis;
    private CanSystemConnectionMemo memo;
    private CbusSend send;
    private TextAreaFIFO ta;

    @Test
    public void testCTor() {
        Assert.assertNotNull("exists",send);
    }

    @Test
    public void testnodeExitLearnEvMode() {
        send.nodeExitLearnEvMode(54321);
        checknodeExitLearnEvMode();
    }

    @Test
    public void testnodeEnterLearnEvMode() {
        send.nodeEnterLearnEvMode(54321);
        checknodeEnterLearnEvMode();
    }

    @Test
    public void testnodeSetNodeNumber() {
        send.nodeSetNodeNumber(12345);
        checknodeSetNodeNumber();
    }
    
    @Test
    public void testnodeRequestParamSetup() {
        send.nodeRequestParamSetup();
        checknodeRequestParamSetup();
    }    

    @Test
    public void testnodeTeachEventLearnMode() {
        send.nodeTeachEventLearnMode(54321,12345,8,255);
        checknodeTeachEventLearnMode();
    }   

    @Test
    public void testnodenodeUnlearnEvent() {
        send.nodeUnlearnEvent(0XFD,0xee);
        checknodeUnlearnEvent();
    }       

    @Test
    public void testrEVAL() {
        send.rEVAL(987,123,6541);
        checkrEVAL();
    }  

    @Test
    public void testrQNPN() {
        send.rQNPN(555,777);
        checkrQNPN();
    }  

    @Test
    public void testsearchForNodes() {
        send.searchForNodes();
        checksearchForNodes();
    }  
    
    @Test
    public void testnVRD() {
        send.nVRD(4321,123);
        checknVRD();
    }

    @Test
    public void testnVSET() {
        send.nVSET(65432,123,1);
        checknVSET();
    }

    @Test
    public void testrQEVN() {
        send.rQEVN(12345);
        checkrQEVN();
    }

    @Test
    public void testnERD() {
        send.nERD(44444);
        checknERD();
    }

    @Test
    public void testaRST() {
        send.aRST();
        checkaRST();
    }
    
    @Test
    public void testeNUM() {
        send.eNUM(1234);
        checkeNUM();
    }
    
    @Test
    public void testcANID() {
        send.cANID(6543,77);
        checkcANID();
    }

    @Test
    public void testnNCLR() {
        send.nNCLR(4321);
        checknNCLR();
    }

    @Test
    public void testrQmn() {
        send.rQmn();
        checkrQmn();
    }
    
    @Test
    public void testTextAreaAdd2() {
        ta = new TextAreaFIFO(9);
        CbusSend tasend = new CbusSend(memo,ta);
        Assert.assertNotNull("exists",tasend);
        tasend.nodeExitLearnEvMode(22222);
        Assert.assertTrue("textarea is updated 2",(ta.getText()).contains("2 exit learn mode."));
        tasend = null;
        ta = null;
    }


    @Test
    public void testTextAreaAdd3() {
        TextAreaFIFO ta = new TextAreaFIFO(9);
        CbusSend tasend = new CbusSend(memo,ta);
        Assert.assertNotNull("exists",tasend);
        tasend.nodeEnterLearnEvMode(33333);
        Assert.assertTrue("textarea is updated 3",(ta.getText()).contains("3 enter learn mode."));
        tasend = null;
        ta = null;
    }

    public void checknodeExitLearnEvMode() {
        Assert.assertEquals("Node exit learn sent", "[5f8] 54 D4 31",
        tcis.outbound.elementAt(tcis.outbound.size() - 1).toString());
    }

    public void checknodeEnterLearnEvMode() {
        Assert.assertEquals("Node enter learn sent", "[5f8] 53 D4 31",
        tcis.outbound.elementAt(tcis.outbound.size() - 1).toString());
    }
    
    public void checknodeSetNodeNumber() {
        Assert.assertEquals("Node set nn sent", "[5f8] 42 30 39",
        tcis.outbound.elementAt(tcis.outbound.size() - 1).toString());
    }
    
    public void checknodeRequestParamSetup() {
        Assert.assertEquals("Node request param setup sent", "[5f8] 10",
        tcis.outbound.elementAt(tcis.outbound.size() - 1).toString());
    }    

    public void checknodeTeachEventLearnMode() {
        Assert.assertEquals("Node Teach Event LearnMode sent", "[5f8] D2 D4 31 30 39 08 FF",
        tcis.outbound.elementAt(tcis.outbound.size() - 1).toString());
    }
    
    public void checknodeUnlearnEvent() {
        Assert.assertEquals("Node Unlearn Event sent", "[5f8] 95 00 FD 00 EE",
        tcis.outbound.elementAt(tcis.outbound.size() - 1).toString());
    }    

    public void checkrEVAL() {
        Assert.assertEquals("rEVAL Event sent", "[5f8] 9C 03 DB 7B 8D",
        tcis.outbound.elementAt(tcis.outbound.size() - 1).toString());
    }  

    public void checkrQNPN() {
        Assert.assertEquals("rQNPN Event sent", "[5f8] 73 02 2B 09",
        tcis.outbound.elementAt(tcis.outbound.size() - 1).toString());
    }      

    public void checksearchForNodes() {
        Assert.assertEquals("searchForNodes sent", "[5f8] 0D",
        tcis.outbound.elementAt(tcis.outbound.size() - 1).toString());
    }   

    public void checknVRD() {
        Assert.assertEquals("nVRD sent", "[5f8] 71 10 E1 7B",
        tcis.outbound.elementAt(tcis.outbound.size() - 1).toString());
    }   

    public void checknVSET() {
        Assert.assertEquals("nVSET sent", "[5f8] 96 FF 98 7B 01",
        tcis.outbound.elementAt(tcis.outbound.size() - 1).toString());
    }

    public void checkrQEVN() {
        Assert.assertEquals("rQEVN sent", "[5f8] 58 30 39",
        tcis.outbound.elementAt(tcis.outbound.size() - 1).toString());
    }
    
    public void checknERD() {
        Assert.assertEquals("nERD sent", "[5f8] 57 AD 9C",
        tcis.outbound.elementAt(tcis.outbound.size() - 1).toString());
    }

    public void checkaRST() {
        Assert.assertEquals("aRST sent", "[5f8] 07",
        tcis.outbound.elementAt(tcis.outbound.size() - 1).toString());
    }

    public void checkeNUM() {
        Assert.assertEquals("eNUM sent", "[5f8] 5D 04 D2",
        tcis.outbound.elementAt(tcis.outbound.size() - 1).toString());
    }

    public void checkcANID() {
        Assert.assertEquals("cANID sent", "[5f8] 75 19 8F 4D",
        tcis.outbound.elementAt(tcis.outbound.size() - 1).toString());
    }
    
    public void checknNCLR() {
        Assert.assertEquals("nNCLR sent", "[5f8] 55 10 E1",
        tcis.outbound.elementAt(tcis.outbound.size() - 1).toString());
    }
    
    public void checkrQmn() {
        Assert.assertEquals("rQmn sent", "[5f8] 11",
        tcis.outbound.elementAt(tcis.outbound.size() - 1).toString());
    }

    // The minimal setup for log4J
    @Before
    public void setUp() {
        JUnitUtil.setUp();
        memo = new CanSystemConnectionMemo();
        tcis = new TrafficControllerScaffold();
        memo.setTrafficController(tcis);
        send = new CbusSend(memo,null);
    }

    @After
    public void tearDown() {
        send = null;
        tcis = null;
        memo = null;
        ta = null;
        JUnitUtil.clearShutDownManager(); // put in place because AbstractMRTrafficController implementing subclass was not terminated properly
        JUnitUtil.tearDown();

    }
    
    // private final static Logger log = LoggerFactory.getLogger(CbusSendTest.class);
}
