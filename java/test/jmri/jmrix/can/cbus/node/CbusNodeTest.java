package jmri.jmrix.can.cbus.node;

import jmri.jmrix.can.CanMessage;
import jmri.jmrix.can.CanReply;
import jmri.jmrix.can.CanSystemConnectionMemo;
import jmri.jmrix.can.TrafficControllerScaffold;
import jmri.jmrix.can.cbus.CbusConstants;
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
public class CbusNodeTest {

    private CanSystemConnectionMemo memo;
    private TrafficControllerScaffold tcis;

    @Test
    public void testCTor() {
        CbusNode t = new CbusNode(memo,256);
        Assert.assertNotNull("exists",t);
        t.dispose();
        t = null;
    }
    
    @Test
    public void testCanListenAndRemove() {
        Assert.assertEquals("no listener to start with",0,tcis.numListeners());
        CbusNode t = new CbusNode(memo,256);
        Assert.assertTrue("table listening",1 == tcis.numListeners());
        t.dispose();
        Assert.assertTrue("no listener to finish with",0 == tcis.numListeners());
    }
    
    @Test
    public void testDefaultGets() {
        
        CbusNode t = new CbusNode(memo,256);
        Assert.assertTrue("nodenum",t.getNodeNumber()==256);
        Assert.assertTrue("default cs num",t.getCsNum()== -1 );
        Assert.assertTrue("default getTotalNodeEvents ",t.getTotalNodeEvents()== -1 );
        Assert.assertTrue("default getLoadedNodeEvents",t.getLoadedNodeEvents()== -1 );
        Assert.assertTrue("default parameter 0",t.getParameter(0)== -1 );
        Assert.assertTrue("default getNV 0",t.getNV(0)== -1 );
        Assert.assertTrue("default getTotalNVs 0",t.getTotalNVs()== 0 );
        Assert.assertTrue("default getNodeCanId ",t.getNodeCanId()== -1 );
        Assert.assertTrue("default getNodeTypeName ",t.getNodeTypeName().isEmpty() );
        Assert.assertEquals("default getNodeInFLiMMode ",true,t.getNodeInFLiMMode() );
        Assert.assertEquals("default getNodeInSetupMode ",false,t.getNodeInSetupMode() );
        Assert.assertEquals("default getNodeNumberName ","256 ",t.getNodeNumberName() );
        Assert.assertEquals("default getsendsWRACKonNVSET ",true,t.getsendsWRACKonNVSET() );
        Assert.assertTrue("default totalNodeBytes ",-1 == t.totalNodeBytes() );
        Assert.assertTrue("default totalRemainingNodeBytes ",-1 == t.totalRemainingNodeBytes() );
        Assert.assertEquals("default toString ","256 ",t.toString() );
        
        t.dispose();
        t = null;
    }
    
    @Test
    public void testInOutLearnMode() {
        CbusNode t = new CbusNode(memo,1234);
        
        Assert.assertEquals("default getNodeInLearnMode ",false,t.getNodeInLearnMode() );
        
        // frame to set node into learn
        CanReply r = new CanReply();
        r.setHeader(tcis.getCanid());
        r.setNumDataElements(3);
        r.setElement(0, CbusConstants.CBUS_NNLRN); 
        r.setElement(1, 0x04);
        r.setElement(2, 0xd2);
        t.reply(r);
        Assert.assertEquals("reply in learn mode ",true,t.getNodeInLearnMode() );
        
        r = new CanReply();
        r.setHeader(tcis.getCanid());
        r.setNumDataElements(3);
        r.setElement(0, CbusConstants.CBUS_NNULN); 
        r.setElement(1, 0x04);
        r.setElement(2, 0xd2);
        t.reply(r);
        Assert.assertEquals("reply exit learn mode ",false,t.getNodeInLearnMode() );
        
        // frame to set node into learn
        CanMessage m = new CanMessage( tcis.getCanid() );
        m.setNumDataElements(3);
        m.setElement(0, CbusConstants.CBUS_NNLRN); 
        m.setElement(1, 0x04);
        m.setElement(2, 0xd2);
        t.message(m);
        Assert.assertEquals("message enter learn mode ",true,t.getNodeInLearnMode() );
        
        m = new CanMessage( tcis.getCanid() );
        m.setNumDataElements(3);
        m.setElement(0, CbusConstants.CBUS_NNULN); 
        m.setElement(1, 0x04);
        m.setElement(2, 0xd2);
        t.message(m);
        Assert.assertEquals("message exit learn mode ",false,t.getNodeInLearnMode() );

        m = null;
        r = null;
        
        t.dispose();
        t = null;
    }
    
    @Test
    public void testSetName() {
        
        CbusNode t = new CbusNode(memo,12345);
        Assert.assertTrue("default getUserName ",t.getUserName().isEmpty() );
        
        t.setUserName("Alonso Smith");
        Assert.assertEquals("username set","Alonso Smith",t.getUserName() );
        Assert.assertEquals("Alonso toString ","12345 Alonso Smith",t.toString() );
        
        t.setNameIfNoName("purple");
        Assert.assertEquals("username unchanged","Alonso Smith",t.getUserName() );
        
        CbusNode tb = new CbusNode(memo,123);
        tb.setNameIfNoName("shirley");
        Assert.assertEquals("username set if no name","shirley",tb.getUserName() );
        Assert.assertEquals("username number","123 shirley",tb.getNodeNumberName() );
        
        t.dispose();
        t = null;
        tb.dispose();
        tb = null;
    }

    @Test
    public void testStartGetParams() {
        
        Assert.assertEquals("tcis empty at start", 0 ,tcis.outbound.size() );
        CbusNode t = new CbusNode(memo,12345);
        
        Assert.assertTrue("default parameter 0",t.getParameter(0)== -1 );
        // only 1 parameter awaiting knowledge of until total confirmed
        Assert.assertTrue("default outstanding parameters",t.getOutstandingParams()== 1 );
        
        Assert.assertEquals("tcis empty after creating new node", 0 ,tcis.outbound.size() );
        t.startParamsLookup();
        Assert.assertEquals("Node has requested parameter 0", 1 ,tcis.outbound.size() );
        t.startParamsLookup();
        Assert.assertEquals("Node has already requested parameter 0", 1 ,tcis.outbound.size() );
        Assert.assertEquals("Message sent is parameter request", "[5f8] 73 30 39 00",
            tcis.outbound.elementAt(tcis.outbound.size() - 1).toString());
        
        // frame from physical node to CbusNode advising has 7 parameters
        CanReply r = new CanReply();
        r.setHeader(tcis.getCanid());
        r.setNumDataElements(5);
        r.setElement(0, CbusConstants.CBUS_PARAN); 
        r.setElement(1, 0x30); // nodenum 12345
        r.setElement(2, 0x39); // nodenum 12345
        r.setElement(3, 0x00); // parameter 0
        r.setElement(4, 0x07); // value 7
        t.reply(r);
        Assert.assertTrue("parameter 0 value 7",t.getParameter(0)== 7 );
        Assert.assertTrue("default outstanding parameters 7",t.getOutstandingParams()== 7 );
        Assert.assertEquals("CbusNode has requested parameter 1 manufacturer", 2 ,tcis.outbound.size() );
        Assert.assertEquals("Message sent is parameter 1 request", "[5f8] 73 30 39 01",
            tcis.outbound.elementAt(tcis.outbound.size() - 1).toString());
        
        r = new CanReply();
        r.setHeader(tcis.getCanid());
        r.setNumDataElements(5);
        r.setElement(0, CbusConstants.CBUS_PARAN); 
        r.setElement(1, 0x30);
        r.setElement(2, 0x39);
        r.setElement(3, 0x01); // parameter 1
        r.setElement(4, 0xa5); // dec 165 MERG
        t.reply(r);
        Assert.assertTrue("parameter 1 value 165",t.getParameter(1)== 165 );
        
        Assert.assertEquals("CbusNode has requested parameter 3 module type identifier", 3 ,tcis.outbound.size() );
        Assert.assertEquals("Message sent is parameter 3 request", "[5f8] 73 30 39 03",
            tcis.outbound.elementAt(tcis.outbound.size() - 1).toString());
        
        r = new CanReply();
        r.setHeader(tcis.getCanid());
        r.setNumDataElements(5);
        r.setElement(0, CbusConstants.CBUS_PARAN); 
        r.setElement(1, 0x30);
        r.setElement(2, 0x39);
        r.setElement(3, 0x03); // parameter 3
        r.setElement(4, 0x1d); // dec 29 CANPAN
        t.reply(r);

        // now we know params 1 and 3, try nodetype lookup
        Assert.assertEquals("CbusNode identified as a CANPAN", "CANPAN" ,t.getNodeTypeName() );
        
        Assert.assertEquals("CbusNode has requested parameter 6 number nv's", 4 ,tcis.outbound.size() );
        Assert.assertEquals("Message sent is parameter 6 request", "[5f8] 73 30 39 06",
            tcis.outbound.elementAt(tcis.outbound.size() - 1).toString());
        
        r = new CanReply();
        r.setHeader(tcis.getCanid());
        r.setNumDataElements(5);
        r.setElement(0, CbusConstants.CBUS_PARAN); 
        r.setElement(1, 0x30);
        r.setElement(2, 0x39);
        r.setElement(3, 0x06); // parameter 6
        r.setElement(4, 0x03); // 3 NV's
        t.reply(r);
        
        // now we know number of NV's
        Assert.assertTrue("getTotalNVs 3",t.getTotalNVs()== 3 );
        Assert.assertTrue("get oustanding NVs 3",t.getOutstandingNvCount()== 3 );
        Assert.assertTrue("get NVs 0 3",t.getNV(0)== 3 );
        Assert.assertTrue("get NVs 1 -1",t.getNV(1)== -1 );
        Assert.assertTrue("get NVs 3 -1",t.getNV(3)== -1 );
        
        Assert.assertEquals("CbusNode has requested parameter 6 number ev vars per event", 5 ,tcis.outbound.size() );
        Assert.assertEquals("Message sent is parameter 5 request", "[5f8] 73 30 39 05",
            tcis.outbound.elementAt(tcis.outbound.size() - 1).toString());
        
        r = new CanReply();
        r.setHeader(tcis.getCanid());
        r.setNumDataElements(5);
        r.setElement(0, CbusConstants.CBUS_PARAN); 
        r.setElement(1, 0x30);
        r.setElement(2, 0x39);
        r.setElement(3, 0x05); // parameter 5
        r.setElement(4, 0x07); // 7 ev vars per ev
        t.reply(r);
        
        Assert.assertTrue(" outstanding parameters 3",t.getOutstandingParams()== 3 );
        
        Assert.assertEquals("CbusNode has requested parameter 7 firmware major", 6 ,tcis.outbound.size() );
        Assert.assertEquals("Message sent is parameter 7 request", "[5f8] 73 30 39 07",
            tcis.outbound.elementAt(tcis.outbound.size() - 1).toString());
        
        r = new CanReply();
        r.setHeader(tcis.getCanid());
        r.setNumDataElements(5);
        r.setElement(0, CbusConstants.CBUS_PARAN); 
        r.setElement(1, 0x30);
        r.setElement(2, 0x39);
        r.setElement(3, 0x07); // parameter 7
        r.setElement(4, 0x02); // firmware pt1 2
        t.reply(r);
        
        Assert.assertEquals("CbusNode has requested parameter 2 firmware minor", 7 ,tcis.outbound.size() );
        Assert.assertEquals("Message sent is parameter 2 request", "[5f8] 73 30 39 02",
            tcis.outbound.elementAt(tcis.outbound.size() - 1).toString());
            
        Assert.assertEquals("default getsendsWRACKonNVSET ",true,t.getsendsWRACKonNVSET() );

        r = new CanReply();
        r.setHeader(tcis.getCanid());
        r.setNumDataElements(5);
        r.setElement(0, CbusConstants.CBUS_PARAN); 
        r.setElement(1, 0x30);
        r.setElement(2, 0x39);
        r.setElement(3, 0x02); // parameter 2
        r.setElement(4, 0x01); // firmware pt2 1
        t.reply(r);
        
        Assert.assertTrue(" outstanding parameter 1",t.getOutstandingParams()== 1 );

        // with this we should expect CbusNodeConstants.setTraits to have been called
        Assert.assertEquals("setTraits getsendsWRACKonNVSET ",false,t.getsendsWRACKonNVSET() );

        Assert.assertEquals("CbusNode has requested number of events", 8 ,tcis.outbound.size() );
        Assert.assertEquals("Message sent numev request", "[5f8] 58 30 39",
            tcis.outbound.elementAt(tcis.outbound.size() - 1).toString());
        
        r = null;
        t.dispose();
        t = null;
        
    }

    @Test
    public void testAddEvents() {
        
        CbusNode t = new CbusNode(memo,12345);
        
        Assert.assertTrue("default getTotalNodeEvents ",t.getTotalNodeEvents()== -1 );
        
        // set node to 4 ev vars per event, para 5
        t.setParameters(new int[]{7,1,2,3,4,4,6,7});
        
        CbusNodeEvent ev = new CbusNodeEvent(0,7,12345,-1,4);
        t.addNewEvent(ev);
        Assert.assertTrue("after new ev getTotalNodeEvents 1",t.getTotalNodeEvents()== 1 );
        Assert.assertTrue("after new ev getOutstandingEvVars 1",t.getOutstandingEvVars()== 4 );
        
        
        CbusNodeEvent evb = new CbusNodeEvent(0,8,12345,-1,4);
        t.addNewEvent(evb);
        Assert.assertTrue("after new ev getTotalNodeEvents 2",t.getTotalNodeEvents()== 2 );
        Assert.assertTrue("after new ev getLoadedNodeEvents 2",t.getLoadedNodeEvents()== 2 );
        
        Assert.assertTrue("node event fetch node 0 ev 7",t.getNodeEvent(0,7) == ev );
        Assert.assertTrue("node event fetch null node 321 ev 645",t.getNodeEvent(321,654) == null );
        
        Assert.assertTrue("node event provide node 0 ev87",t.provideNodeEvent(0,8) == evb );
        Assert.assertTrue("node event provide node 321 ev 645",t.provideNodeEvent(321,654) != null );
        Assert.assertTrue("after provide 321 645 getTotalNodeEvents 3",t.getTotalNodeEvents()== 3 );
        
        t.removeEvent(0,8);
        Assert.assertTrue("node event remove node 0 ev 8",t.getNodeEvent(0,8) == null );
        Assert.assertTrue("node event not removed others",t.getNodeEvent(321,654) != null );
        Assert.assertTrue("after remove getTotalNodeEvents 3",t.getTotalNodeEvents()== 2 );
        
        t.dispose();
        t = null;
        ev = null;
        evb = null;
        
    }
    
    @Test
    public void testAddNodeVariables() {
        
        CbusNode t = new CbusNode(memo,12345);
        
        Assert.assertTrue("default getNvArray ",t.getNvArray()== null );
        Assert.assertTrue("default getNv 0 ",t.getNV(0)== -1 );
        Assert.assertTrue("default getNv 3 ",t.getNV(3)== -1 );
        Assert.assertTrue("default getOutstandingNvCount ",t.getOutstandingNvCount()== -1 );
        
        // set node to 3 node vars , param6
        t.setParameters(new int[]{7,1,2,3,4,5,3,7});
        
        Assert.assertTrue("3 node vars getNvArray ",t.getNvArray() != null );
        Assert.assertTrue("3 node vars getNv 0 ",t.getNV(0)== 3 );
        Assert.assertTrue("3 node vars getNv 3 ",t.getNV(3)== -1 );
        Assert.assertTrue("3 node vars getOutstandingNvCount ",t.getOutstandingNvCount()== 3 );
        
        t.setNV(1,1);
        t.setNV(2,2);
        t.setNV(3,3);
        Assert.assertTrue("node vars getOutstandingNvCount ",t.getOutstandingNvCount()== 0 );
        
        // setNVs
        t.dispose();
        t = null;
        
    }
    
    // The minimal setup for log4J
    @Before
    public void setUp() {
        JUnitUtil.setUp();
        memo = new CanSystemConnectionMemo();
        tcis = new TrafficControllerScaffold();
        memo.setTrafficController(tcis);
        
    }

    @After
    public void tearDown() {
        JUnitUtil.tearDown();
        
        tcis = null;
        memo = null;
        
    }

    // private final static Logger log = LoggerFactory.getLogger(CbusNodeTest.class);

}
