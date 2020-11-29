package jmri.jmrix.can.cbus.node;

import jmri.jmrix.can.CanMessage;
import jmri.jmrix.can.CanReply;
import jmri.jmrix.can.CanSystemConnectionMemo;
import jmri.jmrix.can.TrafficControllerScaffold;
import jmri.jmrix.can.cbus.CbusConstants;
import jmri.util.JUnitAppender;
import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 * @author Steve Young Copyright (C) 2019
 */
public class CbusNodeNVManagerTest {

    @Test
    public void testCTor() {
        Assert.assertNotNull("exists",t);
    }
    
    @Test
    public void testSetNvs() {
    
        Assert.assertArrayEquals(new int[]{3,-1,-1,-1}, t.getNvArray());
        
        Assert.assertTrue("3 NVs Outstanding",t.getOutstandingNvCount()==3);
        
        
        t.setNV(1, 12);
        
        Assert.assertArrayEquals(new int[]{3,12,-1,-1}, t.getNvArray());
        Assert.assertTrue("2 NVs Outstanding",t.getOutstandingNvCount()==2);
        
        
        t.setNV(-44, 20);
        JUnitAppender.checkForMessageStartingWith("Attempted to set NV -44");
        
        t.setNV(777, 20);
        JUnitAppender.checkForMessageStartingWith("Attempted to set NV 777");
        
        t.setNV(2, -11);
        JUnitAppender.checkForMessageStartingWith(
        "Attempted to set NV 2 Invalid Value -11 on Node 7423");
        
        t.setNV(2, 588);
        JUnitAppender.checkForMessageStartingWith(
        "Attempted to set NV 2 Invalid Value 588 on Node 7423");
        
        t.setNV(0, 77);
        JUnitAppender.checkForMessageStartingWith(
        "Node 7423 NV Count mismatch. Parameters report 3 NVs, received set for 77 NVs");
        
        t.reset();
        
        t.setNV(1, 12);
        JUnitAppender.checkForMessageStartingWith(
        "Attempted to set NV 1 on a null NV Array on Node 7423");
        
        Assert.assertTrue("-1 NVs Outstanding Array unset",t.getOutstandingNvCount()==-1);
       
    }
    
    @Test
    public void testGetNextRequest(){
    
        Assert.assertEquals("Starting outbound frames 0",0,tcis.outbound.size());

        Assert.assertFalse("Node has no active Timers",nodeToEdit.getNodeTimerManager().hasActiveTimers());
        
        t.setNV(2, 44);
        
        t.sendNextNVToFetch();
        
        Assert.assertEquals("1 outbound frame sent",1,tcis.outbound.size());
    
        Assert.assertEquals("Request for Node 7423 NV1", "[5f8] 71 1C FF 01",
        tcis.outbound.elementAt(tcis.outbound.size() - 1).toString());
        
        Assert.assertTrue("Node has active Timer",nodeToEdit.getNodeTimerManager().hasActiveTimers());
        
        t.sendNextNVToFetch();

        Assert.assertEquals("Still only 1 outbound frame sent",1,tcis.outbound.size());
    
        CanReply r = new CanReply(0x12);
        r.setNumDataElements(5);
        r.setElement(0, CbusConstants.CBUS_NVANS);
        r.setElement(1, 0x1C); // Node 7423
        r.setElement(2, 0xFF); // Node 7423
        r.setElement(3, 0x01); // NV1
        r.setElement(4, 77); // Value 77
        
        nodeToEdit.getCanListener().reply(r);
        
        Assert.assertArrayEquals(new int[]{3,77,44,-1}, t.getNvArray());

        Assert.assertFalse("Node has no active Timers",nodeToEdit.getNodeTimerManager().hasActiveTimers());
        
        t.sendNextNVToFetch();

        Assert.assertEquals("2 outbound frames sent",2,tcis.outbound.size());
        Assert.assertEquals("Request for Node 7423 NV3", "[5f8] 71 1C FF 03",
        tcis.outbound.elementAt(tcis.outbound.size() - 1).toString());
        
        r.setElement(3, 0x03); // NV1
        r.setElement(4, 250); // Value 250
        
        nodeToEdit.getCanListener().reply(r);
        
        Assert.assertArrayEquals(new int[]{3,77,44,250}, t.getNvArray());
        
        t.sendNextNVToFetch();
        Assert.assertEquals("Still just 2 outbound frames sent",2,tcis.outbound.size());
        
    }
    
    @Test
    public void testSendNvSToNode() {
    
        Assert.assertFalse("Node has no active Timers",nodeToEdit.getNodeTimerManager().hasActiveTimers());
        Assert.assertFalse("Node has no active outstanding teach NVs",t.teachOutstandingNvs());
        
        t.setNV(2, 79);
        Assert.assertArrayEquals(new int[]{3,-1,79,-1}, t.getNvArray());
                
        t.sendNvsToNode(new int[]{3,250,79,44});
        
        Assert.assertTrue("Node has active outstanding teach NVs",t.teachOutstandingNvs());
        

        Assert.assertTrue("Node has active Timer",nodeToEdit.getNodeTimerManager().hasActiveTimers());
        
        Assert.assertEquals("1 outbound frame sent",1,tcis.outbound.size());
    
        Assert.assertEquals("Node 7423 Sets NV1 to 250", "[5f8] 96 1C FF 01 FA",
        tcis.outbound.elementAt(tcis.outbound.size() - 1).toString());
        
        // also forward this outgoing message to Node Canlistener
        CanMessage m = new CanMessage(0x12);
        m.setNumDataElements(5);
        m.setElement(0, 0x96);
        m.setElement(1, 0x1C);
        m.setElement(2, 0xFF);
        m.setElement(3, 0x01);                
        m.setElement(4, 0xFA);
        
        nodeToEdit.getCanListener().message(m);
        
        Assert.assertArrayEquals(new int[]{3,250,79,-1}, t.getNvArray());
        
        t.sendNextNvToNode();
        
        Assert.assertEquals("Still only 1 outbound frame sent",1,tcis.outbound.size());
    
        CanReply r = new CanReply(0x12);
        r.setNumDataElements(3);
        r.setElement(0, CbusConstants.CBUS_WRACK);
        r.setElement(1, 0x1C); // Node 7423
        r.setElement(2, 0xFF); // Node 7423
        
        nodeToEdit.getCanListener().reply(r);
        
        Assert.assertEquals("2 outbound frames sent",2,tcis.outbound.size());
    
        Assert.assertEquals("Node 7423 Sets NV3 to 44", "[5f8] 96 1C FF 03 2C",
        tcis.outbound.elementAt(tcis.outbound.size() - 1).toString());
        
        m.setElement(3, 0x03);                
        m.setElement(4, 0x2C);
        
        nodeToEdit.getCanListener().message(m);
        
        Assert.assertArrayEquals(new int[]{3,250,79,44}, t.getNvArray());
        
        Assert.assertTrue("Node has active outstanding teach NVs",t.teachOutstandingNvs());
        
        nodeToEdit.getCanListener().reply(r);
        
        Assert.assertFalse("Node has no active Timers after write confirm",nodeToEdit.getNodeTimerManager().hasActiveTimers());
        
        Assert.assertFalse("Node has active outstanding teach NVs",t.teachOutstandingNvs());
                
        
    }
    
    private CbusNodeNVManager t;
    
    private CbusNode nodeToEdit;
    private CbusNodeTableDataModel nodeModel;
    private CanSystemConnectionMemo memo;
    private TrafficControllerScaffold tcis;
    
    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        memo = new CanSystemConnectionMemo();
        tcis = new TrafficControllerScaffold();
        memo.setTrafficController(tcis);
        
        nodeModel = new CbusNodeTableDataModel(memo, 3,CbusNodeTableDataModel.MAX_COLUMN);
        
        nodeToEdit = nodeModel.provideNodeByNodeNum(7423);
        t = nodeToEdit.getNodeNvManager();
        
        // set node to 3 node vars , param6
        nodeToEdit.getNodeParamManager().setParameters(new int[]{8,1,2,3,4,5,3,7,8});
        
    }

    @AfterEach
    public void tearDown() {
        nodeToEdit.dispose();
        tcis.terminateThreads();
        memo.dispose();
        tcis = null;
        memo = null;
        
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(CbusNodeEventManagerTest.class);

}
