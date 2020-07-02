package jmri.jmrix.can.cbus.node;

import java.util.ArrayList;

import jmri.jmrix.can.CanMessage;
import jmri.jmrix.can.CanReply;
import jmri.jmrix.can.CanSystemConnectionMemo;
import jmri.jmrix.can.TrafficControllerScaffold;
import jmri.jmrix.can.cbus.CbusConstants;
import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 * @author Steve Young Copyright (C) 2019
 */
public class CbusNodeEventManagerTest {

    @Test
    public void testCTor() {
        Assert.assertNotNull("exists",t);
    }
    
    @Test
    public void testSendEventsToNode(){
    
        Assert.assertEquals("events unset",-1,t.getTotalNodeEvents());
        Assert.assertEquals("outstanding ev vars unset",-1,t.getOutstandingEvVars());
        Assert.assertNull("null node event", t.getNodeEventByIndex(789));
        
        CbusNodeEvent ev = t.provideNodeEvent(123, 456);
        ev.setEvArr(new int[]{1,255,0,4,5});
        
        Assert.assertEquals("events set",1,t.getTotalNodeEvents());
        Assert.assertEquals("outstanding ev vars 0",0,t.getOutstandingEvVars());
    
        ArrayList<CbusNodeEvent> emptyList = new ArrayList<>();
        t.sendNewEvSToNode(emptyList); // does nothing
        
        ArrayList<CbusNodeEvent> list = t.getEventArray();
        
        Assert.assertNotNull(list);
        
        t.resetNodeEvents();
        Assert.assertEquals("0 events set",-1,t.getTotalNodeEvents());
        Assert.assertEquals("outstanding ev vars 0",-1,t.getOutstandingEvVars());
        
        t.sendNewEvSToNode(list);
        
        JUnitUtil.waitFor(()->{ return(tcis.outbound.size()>2); }, " outbound 3 didn't arrive");
        
        Assert.assertEquals("Node 7961 enter learn mode", "[5f8] 53 1F 19",
        tcis.outbound.elementAt(tcis.outbound.size() - 2).toString());
        
        // also forward this outgoing message to Node Canlistener
        CanMessage mLearn = new CanMessage(0x12);
        mLearn.setNumDataElements(3);
        mLearn.setElement(0, 0x53); // Node enter in learn mode
        mLearn.setElement(1, 0x1F); // ev node 123
        mLearn.setElement(2, 0x19); // ev node 123
        nodeToEdit.getCanListener().message(mLearn);
        
        Assert.assertEquals("Learn et event n123 e456 evar1 val 1 ", "[5f8] D2 00 7B 01 C8 01 01",
        tcis.outbound.elementAt(tcis.outbound.size() - 1).toString());
        
        // also forward this outgoing message to Node Canlistener
        CanMessage m = new CanMessage(0x12);
        m.setNumDataElements(7);
        m.setElement(0, 0xD2); // teach event in learn mode
        m.setElement(1, 0x00); // ev node 123
        m.setElement(2, 0x7B); // ev node 123
        m.setElement(3, 0x01); // ev num 456 
        m.setElement(4, 0xC8); // ev num 456
        m.setElement(5, 0x01); // ev index 1
        m.setElement(6, 0x01); // value 1
        nodeToEdit.getCanListener().message(m);
        
        Assert.assertEquals("1 event set",1,t.getTotalNodeEvents());
        Assert.assertEquals("outstanding ev vars 4",4,t.getOutstandingEvVars());
        
        // confirm write from physical node to CanListener
        CanReply wrack = new CanReply(0x12);
        wrack.setNumDataElements(3);
        wrack.setElement(0, CbusConstants.CBUS_WRACK);
        wrack.setElement(1, 0x1C); // Node 7423
        wrack.setElement(2, 0xFF); // Node 7423
        
        nodeToEdit.getCanListener().reply(wrack);
        
        
        JUnitUtil.waitFor(()->{ return(tcis.outbound.size()>3); }, " outbound 4 didn't arrive");
        
        Assert.assertEquals("Learn event n123 e456 evar2 val 255", "[5f8] D2 00 7B 01 C8 02 FF",
        tcis.outbound.elementAt(tcis.outbound.size() - 1).toString());
        
        m.setElement(5, 0x02); // ev index 1
        m.setElement(6, 0xff); // value 1
        nodeToEdit.getCanListener().message(m);
        
        Assert.assertEquals("outstanding ev vars 3",3,t.getOutstandingEvVars());
        
        nodeToEdit.getCanListener().reply(wrack);
        
        
        JUnitUtil.waitFor(()->{ return(tcis.outbound.size()>4); }, " outbound 5 didn't arrive");
        
        Assert.assertEquals("Learn event n123 e456 evar3 val0", "[5f8] D2 00 7B 01 C8 03 00",
        tcis.outbound.elementAt(tcis.outbound.size() - 1).toString());
        
        m.setElement(5, 0x03); // ev index 3
        m.setElement(6, 0x00); // value 0
        nodeToEdit.getCanListener().message(m);
        
        Assert.assertEquals("outstanding ev vars 2",2,t.getOutstandingEvVars());
        
        nodeToEdit.getCanListener().reply(wrack);
        
        JUnitUtil.waitFor(()->{ return(tcis.outbound.size()>5); }, " outbound 6 didn't arrive");
                
        Assert.assertEquals("Learn event n123 e456 evar4 val4", "[5f8] D2 00 7B 01 C8 04 04",
        tcis.outbound.elementAt(tcis.outbound.size() - 1).toString());
        
        m.setElement(5, 0x04); // ev index 4
        m.setElement(6, 0x04); // value 4
        nodeToEdit.getCanListener().message(m);
        
        Assert.assertEquals("outstanding ev vars 1",1,t.getOutstandingEvVars());
        
        nodeToEdit.getCanListener().reply(wrack);
        
        JUnitUtil.waitFor(()->{ return(tcis.outbound.size()>6); }, " outbound 7 didn't arrive");
                
        Assert.assertEquals("Learn event n123 e456 evar5 val5", "[5f8] D2 00 7B 01 C8 05 05",
        tcis.outbound.elementAt(tcis.outbound.size() - 1).toString());
        
        m.setElement(5, 0x05); // ev index 5
        m.setElement(6, 0x05); // value 5
        nodeToEdit.getCanListener().message(m);
        
        Assert.assertEquals("outstanding ev vars 0",0,t.getOutstandingEvVars());
        
        nodeToEdit.getCanListener().reply(wrack);
        
        JUnitUtil.waitFor(()->{ return(tcis.outbound.size()>7); }, " outbound 8 didn't arrive");
                
        Assert.assertEquals("Learn 7961 exit learn mode", "[5f8] 54 1F 19",
        tcis.outbound.elementAt(tcis.outbound.size() - 1).toString());
        
        
        
    }
    
    private CbusNodeEventManager t;
    
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
        
        nodeToEdit = nodeModel.provideNodeByNodeNum(7961);
        t = nodeToEdit.getNodeEventManager();
        
        // set node to 3 node vars , param6, 5 ev vars per event
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
