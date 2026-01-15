package jmri.jmrix.can.cbus;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import jmri.jmrix.can.CanSystemConnectionMemo;
import jmri.jmrix.can.TrafficControllerScaffold;
import jmri.util.JUnitUtil;
import jmri.util.swing.TextAreaFIFO;

import org.junit.jupiter.api.*;

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
        assertNotNull( send, "exists");
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
    public void testCbusEnumerationSent() {
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
        assertNotNull( tasend, "exists");
        tasend.nodeExitLearnEvMode(22222);
        assertTrue( (ta.getText()).contains("2 exit learn mode."),
            "textarea is updated 2");
        
    }


    @Test
    public void testTextAreaAdd3() {
        ta = new TextAreaFIFO(9);
        CbusSend tasend = new CbusSend(memo,ta);
        assertNotNull( tasend, "exists");
        tasend.nodeEnterLearnEvMode(33333);
        assertTrue( (ta.getText()).contains("3 enter learn mode."), "textarea is updated 3");
    }

    private void checknodeExitLearnEvMode() {
        assertEquals( "[5f8] 54 D4 31",
        tcis.outbound.elementAt(tcis.outbound.size() - 1).toString(),
        "Node exit learn sent");
    }

    private void checknodeEnterLearnEvMode() {
        assertEquals( "[5f8] 53 D4 31",
        tcis.outbound.elementAt(tcis.outbound.size() - 1).toString(),
        "Node enter learn sent");
    }

    private void checknodeSetNodeNumber() {
        assertEquals( "[5f8] 42 30 39",
        tcis.outbound.elementAt(tcis.outbound.size() - 1).toString(),
        "Node set nn sent");
    }
    
    private void checknodeRequestParamSetup() {
        assertEquals( "[5f8] 10",
            tcis.outbound.elementAt(tcis.outbound.size() - 1).toString(),
            "Node request param setup sent");
    }

    private void checknodeTeachEventLearnMode() {
        assertEquals( "[5f8] D2 D4 31 30 39 08 FF",
            tcis.outbound.elementAt(tcis.outbound.size() - 1).toString(),
            "Node Teach Event LearnMode sent");
    }

    private void checknodeUnlearnEvent() {
        assertEquals( "[5f8] 95 00 FD 00 EE",
            tcis.outbound.elementAt(tcis.outbound.size() - 1).toString(),
            "Node Unlearn Event sent");
    }

    private void checkrEVAL() {
        assertEquals( "[5f8] 9C 03 DB 7B 8D",
            tcis.outbound.elementAt(tcis.outbound.size() - 1).toString(),
            "rEVAL Event sent");
    }

    private void checkrQNPN() {
        assertEquals( "[5f8] 73 02 2B 09",
            tcis.outbound.elementAt(tcis.outbound.size() - 1).toString(),
            "rQNPN Event sent");
    }

    private void checksearchForNodes() {
        assertEquals( "[5f8] 0D",
            tcis.outbound.elementAt(tcis.outbound.size() - 1).toString(),
            "searchForNodes sent");
    }

    private void checknVRD() {
        assertEquals( "[5f8] 71 10 E1 7B",
            tcis.outbound.elementAt(tcis.outbound.size() - 1).toString(),
            "nVRD sent");
    }

    private void checknVSET() {
        assertEquals( "[5f8] 96 FF 98 7B 01",
            tcis.outbound.elementAt(tcis.outbound.size() - 1).toString(),
            "nVSET sent");
    }

    private void checkrQEVN() {
        assertEquals( "[5f8] 58 30 39",
            tcis.outbound.elementAt(tcis.outbound.size() - 1).toString(),
            "rQEVN sent");
    }

    private void checknERD() {
        assertEquals( "[5f8] 57 AD 9C",
            tcis.outbound.elementAt(tcis.outbound.size() - 1).toString(),
            "nERD sent");
    }

    private void checkaRST() {
        assertEquals( "[5f8] 07",
            tcis.outbound.elementAt(tcis.outbound.size() - 1).toString(),
            "aRST sent");
    }

    private void checkeNUM() {
        assertEquals( "[5f8] 5D 04 D2",
            tcis.outbound.elementAt(tcis.outbound.size() - 1).toString(),
            "eNUM sent");
    }

    private void checkcANID() {
        assertEquals( "[5f8] 75 19 8F 4D",
            tcis.outbound.elementAt(tcis.outbound.size() - 1).toString(),
            "cANID sent");
    }

    private void checknNCLR() {
        assertEquals( "[5f8] 55 10 E1",
            tcis.outbound.elementAt(tcis.outbound.size() - 1).toString(),
            "nNCLR sent");
    }

    private void checkrQmn() {
        assertEquals( "[5f8] 11",
            tcis.outbound.elementAt(tcis.outbound.size() - 1).toString(),
            "rQmn sent");
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        memo = new CanSystemConnectionMemo();
        tcis = new TrafficControllerScaffold();
        memo.setTrafficController(tcis);
        send = new CbusSend(memo,null);
    }

    @AfterEach
    public void tearDown() {
        send = null;
        memo.dispose();
        tcis.terminateThreads();
        tcis = null;
        memo = null;
        ta = null;
        JUnitUtil.tearDown();

    }

    // private final static Logger log = LoggerFactory.getLogger(CbusSendTest.class);
}
