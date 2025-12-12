package jmri.jmrix.can.cbus.node;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import jmri.jmrix.can.CanMessage;
import jmri.jmrix.can.CanReply;
import jmri.jmrix.can.CanSystemConnectionMemo;
import jmri.jmrix.can.TrafficControllerScaffold;
import jmri.jmrix.can.cbus.*;
import jmri.util.JUnitAppender;
import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 * @author Steve Young Copyright (C) 2019
 */
public class CbusNodeNVManagerTest {

    @Test
    public void testCTor() {
        assertNotNull( t, "exists");
    }

    @Test
    public void testSetNvs() {

        assertArrayEquals(new int[]{3,-1,-1,-1}, t.getNvArray());

        assertEquals( 3, t.getOutstandingNvCount(), "3 NVs Outstanding");


        t.setNV(1, 12);

        assertArrayEquals(new int[]{3,12,-1,-1}, t.getNvArray());
        assertEquals( 2, t.getOutstandingNvCount(), "2 NVs Outstanding");


        t.setNV(-44, 20);
        JUnitAppender.assertErrorMessage("Attempted to set Invalid NV -44 on Node 7423");

        t.setNV(777, 20);
        JUnitAppender.assertErrorMessage("Attempted to set Invalid NV 777 on Node 7423");

        t.setNV(2, -11);
        JUnitAppender.assertErrorMessage(
            "Attempted to set NV 2 Invalid Value -11 on Node 7423");

        t.setNV(2, 588);
        JUnitAppender.assertErrorMessage(
            "Attempted to set NV 2 Invalid Value 588 on Node 7423");

        t.setNV(0, 77);
        JUnitAppender.assertErrorMessage(
            "Node 7423 NV Count mismatch. Parameters report 3 NVs, received set for 77 NVs");

        t.reset();

        t.setNV(1, 12);
        JUnitAppender.assertErrorMessage(
            "Attempted to set NV 1 on a null NV Array on Node 7423");

        assertEquals( -1, t.getOutstandingNvCount(), "-1 NVs Outstanding Array unset");

    }

    @Test
    public void testGetNextRequest(){

        assertEquals( 0, tcis.outbound.size(), "Starting outbound frames 0");

        assertFalse( nodeToEdit.getNodeTimerManager().hasActiveTimers(), "Node has no active Timers");

        t.setNV(2, 44);

        t.sendNextNVToFetch();

        assertEquals( 1, tcis.outbound.size(), "1 outbound frame sent");

        assertEquals( "[5f8] 71 1C FF 01",
            tcis.outbound.elementAt(tcis.outbound.size() - 1).toString(),
            "Request for Node 7423 NV1");

        assertTrue( nodeToEdit.getNodeTimerManager().hasActiveTimers(), "Node has active Timer");

        t.sendNextNVToFetch();

        assertEquals( 1, tcis.outbound.size(), "Still only 1 outbound frame sent");

        CanReply r = new CanReply(0x12);
        r.setNumDataElements(5);
        r.setElement(0, CbusConstants.CBUS_NVANS);
        r.setElement(1, 0x1C); // Node 7423
        r.setElement(2, 0xFF); // Node 7423
        r.setElement(3, 0x01); // NV1
        r.setElement(4, 77); // Value 77

        nodeToEdit.getCanListener().reply(r);

        assertArrayEquals(new int[]{3,77,44,-1}, t.getNvArray());

        assertFalse( nodeToEdit.getNodeTimerManager().hasActiveTimers(), "Node has no active Timers");

        t.sendNextNVToFetch();

        assertEquals( 2, tcis.outbound.size(), "2 outbound frames sent");
        assertEquals( "[5f8] 71 1C FF 03",
            tcis.outbound.elementAt(tcis.outbound.size() - 1).toString(),
            "Request for Node 7423 NV3");

        r.setElement(3, 0x03); // NV1
        r.setElement(4, 250); // Value 250

        nodeToEdit.getCanListener().reply(r);

        assertArrayEquals(new int[]{3,77,44,250}, t.getNvArray());

        t.sendNextNVToFetch();
        assertEquals( 2, tcis.outbound.size(), "Still just 2 outbound frames sent");

    }

    @Test
    public void testSendNvSToNode() {

        assertFalse( nodeToEdit.getNodeTimerManager().hasActiveTimers(), "Node has no active Timers");
        assertFalse( t.teachOutstandingNvs(), "Node has no active outstanding teach NVs");

        t.setNV(2, 79);
        assertArrayEquals(new int[]{3,-1,79,-1}, t.getNvArray());

        t.sendNvsToNode(new int[]{3,250,79,44});

        assertTrue( t.teachOutstandingNvs(), "Node has active outstanding teach NVs");


        assertTrue( nodeToEdit.getNodeTimerManager().hasActiveTimers(), "Node has active Timer");

        assertEquals( 1, tcis.outbound.size(), "1 outbound frame sent");

        assertEquals( "[5f8] 96 1C FF 01 FA",
            tcis.outbound.elementAt(tcis.outbound.size() - 1).toString(),
            "Node 7423 Sets NV1 to 250");

        // also forward this outgoing message to Node Canlistener
        CanMessage m = new CanMessage(0x12);
        m.setNumDataElements(5);
        m.setElement(0, 0x96);
        m.setElement(1, 0x1C);
        m.setElement(2, 0xFF);
        m.setElement(3, 0x01);
        m.setElement(4, 0xFA);

        nodeToEdit.getCanListener().message(m);

        assertArrayEquals(new int[]{3,250,79,-1}, t.getNvArray());

        t.sendNextNvToNode();

        assertEquals( 1, tcis.outbound.size(), "Still only 1 outbound frame sent");

        CanReply r = new CanReply(0x12);
        r.setNumDataElements(3);
        r.setElement(0, CbusConstants.CBUS_WRACK);
        r.setElement(1, 0x1C); // Node 7423
        r.setElement(2, 0xFF); // Node 7423

        nodeToEdit.getCanListener().reply(r);

        assertEquals( 2, tcis.outbound.size(), "2 outbound frames sent");

        assertEquals( "[5f8] 96 1C FF 03 2C",
            tcis.outbound.elementAt(tcis.outbound.size() - 1).toString(),
            "Node 7423 Sets NV3 to 44");

        m.setElement(3, 0x03);
        m.setElement(4, 0x2C);

        nodeToEdit.getCanListener().message(m);

        assertArrayEquals(new int[]{3,250,79,44}, t.getNvArray());

        assertTrue( t.teachOutstandingNvs(), "Node has active outstanding teach NVs");

        nodeToEdit.getCanListener().reply(r);

        assertFalse( nodeToEdit.getNodeTimerManager().hasActiveTimers(), "Node has no active Timers after write confirm");

        assertFalse( t.teachOutstandingNvs(), "Node has active outstanding teach NVs");


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

        memo.setProtocol(jmri.jmrix.can.CanConfigurationManager.MERGCBUS);

        memo.get(CbusPreferences.class).setNodeBackgroundFetchDelay(0);
        nodeModel = memo.get(CbusConfigurationManager.class)
            .provide(CbusNodeTableDataModel.class);

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
