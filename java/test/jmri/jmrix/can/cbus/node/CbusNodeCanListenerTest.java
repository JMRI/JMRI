package jmri.jmrix.can.cbus.node;

import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;

import jmri.jmrix.can.CanMessage;
import jmri.jmrix.can.CanReply;
import jmri.jmrix.can.CanSystemConnectionMemo;
import jmri.jmrix.can.TrafficControllerScaffold;
import jmri.jmrix.can.cbus.CbusConfigurationManager;
import jmri.jmrix.can.cbus.CbusConstants;
import jmri.jmrix.can.cbus.CbusPreferences;
import jmri.util.JUnitAppender;
import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 * @author Steve Young Copyright (C) 2019
 */
public class CbusNodeCanListenerTest {

    @Test
    public void testCTor() {
        CbusNodeCanListener t = new CbusNodeCanListener(null,null);
        assertNotNull( t, "exists");
    }

    /**
     * A module which only accepts NV writes in learn mode ( eg. CANSERVO8C )
     * must still be tracked as in learn mode when an NNLRN is sent,
     * else the WRACK responses while teaching events are not processed and
     * the event teach never completes.
     */
    @Test
    public void testTeachEventsToNvWriteInLearnOnlyModule() {

        // MERG CANSERVO8C, module type 19, 2 event variables per event, 3 NVs
        nodeToEdit.getNodeParamManager().setParameters(new int[]{8,165,2,19,4,2,3,7,8});
        CbusNodeConstants.setTraits(nodeToEdit);
        assertTrue( nodeToEdit.getnvWriteInLearnOnly(), "CANSERVO8C requires learn mode for NV writes");
        assertFalse( nodeToEdit.getNodeInLearnMode(), "not in learn mode at start");

        CbusNodeEvent ev = new CbusNodeEvent(memo, 123, 456, 7961, -1, 2);
        ev.setEvArr(new int[]{1,255});
        ArrayList<CbusNodeEvent> list = new ArrayList<>();
        list.add(ev);

        nodeToEdit.getNodeEventManager().sendNewEvSToNode(list);

        JUnitUtil.waitFor(()->{ return(tcis.outbound.size()>1); }, " outbound 2 didn't arrive");

        assertEquals( "[5f8] 53 1F 19",
            tcis.outbound.elementAt(tcis.outbound.size() - 2).toString(), "Node 7961 enter learn mode");

        // forward the outgoing NNLRN to the Node CanListener, as the Traffic Controller would.
        CanMessage mLearn = new CanMessage(tcis.getCanid());
        mLearn.setNumDataElements(3);
        mLearn.setElement(0, CbusConstants.CBUS_NNLRN);
        mLearn.setElement(1, 0x1F); // node 7961
        mLearn.setElement(2, 0x19); // node 7961
        nodeToEdit.getCanListener().message(mLearn);

        assertTrue( nodeToEdit.getNodeInLearnMode(), "node in learn mode after NNLRN sent");

        assertEquals( "[5f8] D2 00 7B 01 C8 01 01",
            tcis.outbound.elementAt(tcis.outbound.size() - 1).toString(), "Learn event n123 e456 evar1 val 1");

        CanMessage m = new CanMessage(tcis.getCanid());
        m.setNumDataElements(7);
        m.setElement(0, CbusConstants.CBUS_EVLRN);
        m.setElement(1, 0x00); // ev node 123
        m.setElement(2, 0x7B); // ev node 123
        m.setElement(3, 0x01); // ev num 456
        m.setElement(4, 0xC8); // ev num 456
        m.setElement(5, 0x01); // ev index 1
        m.setElement(6, 0x01); // value 1
        nodeToEdit.getCanListener().message(m);

        assertEquals( 1, nodeToEdit.getNodeEventManager().getTotalNodeEvents(), "1 event on node");
        assertEquals( 1, nodeToEdit.getNodeEventManager().getOutstandingEvVars(), "1 ev var outstanding");

        // write confirm from the physical node
        CanReply wrack = new CanReply(0x12);
        wrack.setNumDataElements(3);
        wrack.setElement(0, CbusConstants.CBUS_WRACK);
        wrack.setElement(1, 0x1F); // node 7961
        wrack.setElement(2, 0x19); // node 7961
        nodeToEdit.getCanListener().reply(wrack);

        JUnitUtil.waitFor(()->{ return(tcis.outbound.size()>2); }, " outbound 3 didn't arrive");
        assertEquals( "[5f8] D2 00 7B 01 C8 02 FF",
            tcis.outbound.elementAt(tcis.outbound.size() - 1).toString(), "Learn event n123 e456 evar2 val 255");

        m.setElement(5, 0x02); // ev index 2
        m.setElement(6, 0xFF); // value 255
        nodeToEdit.getCanListener().message(m);
        assertEquals( 0, nodeToEdit.getNodeEventManager().getOutstandingEvVars(), "0 ev vars outstanding");

        nodeToEdit.getCanListener().reply(wrack);

        JUnitUtil.waitFor(()->{ return(tcis.outbound.size()>3); }, " outbound 4 didn't arrive");
        assertEquals( 4, tcis.outbound.size(), "NNLRN, 2 x EVLRN, NNULN sent");
        assertEquals( "[5f8] 54 1F 19",
            tcis.outbound.elementAt(tcis.outbound.size() - 1).toString(), "Node 7961 exit learn mode");
        assertFalse( nodeToEdit.getNodeEventManager().TEACH_OUTSTANDING_EVS, "teach complete");
        assertFalse( nodeToEdit.getNodeTimerManager().hasActiveTimers(), "no timers after teach complete");

        CanMessage mUnlearn = new CanMessage(tcis.getCanid());
        mUnlearn.setNumDataElements(3);
        mUnlearn.setElement(0, CbusConstants.CBUS_NNULN);
        mUnlearn.setElement(1, 0x1F); // node 7961
        mUnlearn.setElement(2, 0x19); // node 7961
        nodeToEdit.getCanListener().message(mUnlearn);
        assertFalse( nodeToEdit.getNodeInLearnMode(), "node out of learn mode after NNULN sent");

        CbusNodeEvent taught = nodeToEdit.getNodeEventManager().getNodeEvent(123, 456);
        assertNotNull(taught);
        assertEquals( 1, taught.getEvVar(1), "ev var 1 taught");
        assertEquals( 255, taught.getEvVar(2), "ev var 2 taught");

    }

    /**
     * EVLRNI Frame is NN NN EN EN EN# EV# EVval,
     * the event variable index is a single byte.
     */
    @Test
    public void testEvlrniUpdatesEventVariable() {

        // 4 event variables per event
        nodeToEdit.getNodeParamManager().setParameters(new int[]{8,1,2,3,4,4,3,7,8});

        CbusNodeEvent ev = new CbusNodeEvent(memo, 0, 7, 7961, -1, 4);
        nodeToEdit.getNodeEventManager().addNewEvent(ev);
        ev.setIndex(3);
        nodeToEdit.getNodeEventManager().setEvIndexValid(true);
        nodeToEdit.setNodeInLearnMode(true);

        CanReply r = new CanReply(0x12);
        r.setNumDataElements(8);
        r.setElement(0, CbusConstants.CBUS_EVLRNI);
        r.setElement(1, 0x30); // event node 12344
        r.setElement(2, 0x38); // event node 12344
        r.setElement(3, 0x00); // event 7
        r.setElement(4, 0x07); // event 7
        r.setElement(5, 0x03); // event index 3
        r.setElement(6, 0x01); // event variable 1
        r.setElement(7, 0xD1); // value 209
        nodeToEdit.getCanListener().reply(r);

        assertEquals( 12344, ev.getNn(), "event node number updated");
        assertEquals( 7, ev.getEn(), "event number updated");
        assertEquals( 209, ev.getEvVar(1), "event variable 1 updated");
        assertEquals( -1, ev.getEvVar(2), "event variable 2 unchanged");

        r.setElement(6, 0x04); // event variable 4
        r.setElement(7, 0x02); // value 2
        nodeToEdit.getCanListener().reply(r);
        assertEquals( 2, ev.getEvVar(4), "event variable 4 updated");

        // event variable index out of range does not throw
        r.setElement(6, 0x09); // event variable 9
        r.setElement(7, 0x03); // value 3
        nodeToEdit.getCanListener().reply(r);
        JUnitAppender.assertErrorMessageStartsWith("Event Variable Index 9 out of range");
        assertEquals( 209, ev.getEvVar(1), "event variable 1 unchanged");
        assertEquals( 2, ev.getEvVar(4), "event variable 4 unchanged");

        assertEquals( -1, ev.getEvVar(5), "get event variable out of range returns -1");
        JUnitAppender.assertErrorMessageStartsWith("Event Variable Index 5 out of range");

        assertEquals( 0, tcis.outbound.size(), "nothing sent");

    }

    /**
     * Frames sent from software ( eg. NVSET ), heard via message(),
     * must not update the Node CAN ID with the software CAN ID.
     */
    @Test
    public void testSoftwareFrameDoesNotChangeCanId() {

        nodeToEdit.getNodeParamManager().setParameters(new int[]{8,1,2,3,4,5,3,7,8});

        AtomicInteger canIdChanges = new AtomicInteger(0);
        PropertyChangeListener listener = (evt) -> {
            if ( "CANID".equals(evt.getPropertyName()) ) {
                canIdChanges.incrementAndGet();
            }
        };
        nodeToEdit.addPropertyChangeListener(listener);

        assertNotEquals( 0x12, nodeToEdit.getNodeCanId(), "default CAN ID not the test CAN ID");

        // response from the node with CAN ID 0x12
        CanReply r = new CanReply();
        r.setHeader(0x12); // standard frame, CAN ID in the header
        r.setNumDataElements(5);
        r.setElement(0, CbusConstants.CBUS_NVANS);
        r.setElement(1, 0x1F); // node 7961
        r.setElement(2, 0x19); // node 7961
        r.setElement(3, 0x01); // NV1
        r.setElement(4, 77); // Value 77
        nodeToEdit.getCanListener().reply(r);

        assertEquals( 0x12, nodeToEdit.getNodeCanId(), "CAN ID set from node reply");
        assertEquals( 1, canIdChanges.get(), "1 CAN ID change notified");

        // same node again, no change so no notification
        nodeToEdit.getCanListener().reply(r);
        assertEquals( 0x12, nodeToEdit.getNodeCanId(), "CAN ID unchanged");
        assertEquals( 1, canIdChanges.get(), "no further CAN ID change notified");

        // NVSET sent from JMRI, with the JMRI CAN ID
        CanMessage m = new CanMessage(tcis.getCanid());
        m.setNumDataElements(5);
        m.setElement(0, CbusConstants.CBUS_NVSET);
        m.setElement(1, 0x1F); // node 7961
        m.setElement(2, 0x19); // node 7961
        m.setElement(3, 0x02); // NV2
        m.setElement(4, 0xFA); // Value 250
        nodeToEdit.getCanListener().message(m);

        assertEquals( 250, nodeToEdit.getNodeNvManager().getNV(2), "NV2 set from software NVSET");
        assertEquals( 0x12, nodeToEdit.getNodeCanId(), "CAN ID not changed by software NVSET");
        assertEquals( 1, canIdChanges.get(), "no CAN ID change notified for software NVSET");

        // NNLRN / NNULN sent from JMRI
        CanMessage mLearn = new CanMessage(tcis.getCanid());
        mLearn.setNumDataElements(3);
        mLearn.setElement(0, CbusConstants.CBUS_NNLRN);
        mLearn.setElement(1, 0x1F); // node 7961
        mLearn.setElement(2, 0x19); // node 7961
        nodeToEdit.getCanListener().message(mLearn);
        assertTrue( nodeToEdit.getNodeInLearnMode(), "in learn mode");
        mLearn.setElement(0, CbusConstants.CBUS_NNULN);
        nodeToEdit.getCanListener().message(mLearn);
        assertFalse( nodeToEdit.getNodeInLearnMode(), "out of learn mode");
        assertEquals( 0x12, nodeToEdit.getNodeCanId(), "CAN ID not changed by software NNLRN / NNULN");
        assertEquals( 1, canIdChanges.get(), "no CAN ID change notified for software frames");

        nodeToEdit.removePropertyChangeListener(listener);
    }

    /**
     * A node reporting fewer than 8 parameters must not cause an exception
     * when the parameters known from PNN / STAT are added.
     */
    @Test
    public void testParan0WithFewParameters() {

        nodeToEdit.setFW(2, 1, 0);
        nodeToEdit.setManuModule(165, 29);

        CanReply r = new CanReply(0x12);
        r.setNumDataElements(5);
        r.setElement(0, CbusConstants.CBUS_PARAN);
        r.setElement(1, 0x1F); // node 7961
        r.setElement(2, 0x19); // node 7961
        r.setElement(3, 0x00); // parameter 0
        r.setElement(4, 0x03); // 3 parameters
        nodeToEdit.getCanListener().reply(r);

        assertEquals( 3, nodeToEdit.getNodeParamManager().getParameter(0), "3 parameters");
        assertEquals( 165, nodeToEdit.getNodeParamManager().getParameter(1), "manufacturer from PNN");
        assertEquals( 1, nodeToEdit.getNodeParamManager().getParameter(2), "minor fw from STAT");
        assertEquals( 29, nodeToEdit.getNodeParamManager().getParameter(3), "module from PNN");
        assertEquals( -1, nodeToEdit.getNodeParamManager().getParameter(7), "parameter 7 not present");
        assertEquals( -1, nodeToEdit.getNodeParamManager().getParameter(20), "parameter 20 not present");

        // 19 parameters, so setting the beta build in parameter 20 is ignored
        r.setElement(4, 19);
        nodeToEdit.getCanListener().reply(r);
        assertEquals( 19, nodeToEdit.getNodeParamManager().getParameter(0), "19 parameters");
        assertEquals( 2, nodeToEdit.getNodeParamManager().getParameter(7), "major fw from STAT");
        assertEquals( -1, nodeToEdit.getNodeParamManager().getParameter(20), "parameter 20 not present");

    }

    /**
     * Parameters received from a Node in setup are applied to the initial
     * parameter fetch only.
     */
    @Test
    public void testSetupParamsAppliedOnce() {

        // manufacturer, minor fw, module, num events, ev vars per event, num NVs, major fw
        nodeToEdit.getCanListener().setParamsFromSetup(new int[]{165, 'a', 29, 128, 13, 3, 4});

        CanReply r = new CanReply(0x12);
        r.setNumDataElements(5);
        r.setElement(0, CbusConstants.CBUS_PARAN);
        r.setElement(1, 0x1F); // node 7961
        r.setElement(2, 0x19); // node 7961
        r.setElement(3, 0x00); // parameter 0
        r.setElement(4, 0x08); // 8 parameters
        nodeToEdit.getCanListener().reply(r);

        assertEquals( 165, nodeToEdit.getNodeParamManager().getParameter(1), "manufacturer from setup");
        assertEquals( 29, nodeToEdit.getNodeParamManager().getParameter(3), "module from setup");
        assertEquals( 13, nodeToEdit.getNodeParamManager().getParameter(5), "ev vars from setup");
        assertEquals( 3, nodeToEdit.getNodeParamManager().getParameter(6), "num NVs from setup");
        assertEquals( 4, nodeToEdit.getNodeParamManager().getParameter(7), "major fw from setup");
        assertEquals( 3, nodeToEdit.getNodeNvManager().getTotalNVs(), "NV array from setup");
        assertEquals( 1, nodeToEdit.getNodeParamManager().getOutstandingParams(), "1 parameter outstanding");

        nodeToEdit.resetNodeAll();
        assertEquals( -1, nodeToEdit.getNodeParamManager().getParameter(0), "parameters reset");

        nodeToEdit.getCanListener().reply(r);
        assertEquals( 8, nodeToEdit.getNodeParamManager().getParameter(0), "8 parameters");
        assertEquals( -1, nodeToEdit.getNodeParamManager().getParameter(3), "setup params not re-applied");
        assertEquals( -1, nodeToEdit.getNodeParamManager().getParameter(6), "setup num NVs not re-applied");

    }

    /**
     * NUMEV response adds all events in one go with a single ALLEVUPDATE notification.
     */
    @Test
    public void testNumEvAddsEventsInBulk() {

        // 4 event variables per event
        nodeToEdit.getNodeParamManager().setParameters(new int[]{8,1,2,3,4,4,3,7,8});
        assertEquals( -1, nodeToEdit.getNodeEventManager().getTotalNodeEvents(), "events unknown");

        AtomicInteger allEvUpdates = new AtomicInteger(0);
        PropertyChangeListener listener = (evt) -> {
            if ( "ALLEVUPDATE".equals(evt.getPropertyName()) ) {
                allEvUpdates.incrementAndGet();
            }
        };
        nodeToEdit.addPropertyChangeListener(listener);

        CanReply r = new CanReply(0x12);
        r.setNumDataElements(4);
        r.setElement(0, CbusConstants.CBUS_NUMEV);
        r.setElement(1, 0x1F); // node 7961
        r.setElement(2, 0x19); // node 7961
        r.setElement(3, 25); // 25 events
        nodeToEdit.getCanListener().reply(r);

        assertEquals( 25, nodeToEdit.getNodeEventManager().getTotalNodeEvents(), "25 events");
        assertEquals( 0, nodeToEdit.getNodeEventManager().getLoadedNodeEvents(), "0 events loaded");
        assertEquals( 25, nodeToEdit.getNodeEventManager().getOutstandingIndexNodeEvents(), "25 events outstanding");
        assertEquals( 100, nodeToEdit.getNodeEventManager().getOutstandingEvVars(), "100 event variables outstanding");
        assertFalse( nodeToEdit.getNodeEventManager().isEventIndexValid(), "event index invalid");
        assertEquals( 1, allEvUpdates.get(), "1 ALLEVUPDATE notification for 25 events");
        assertEquals( 7961, nodeToEdit.getNodeEventManager().getNodeEventByArrayID(24).getParentNn(), "event parent node");
        assertEquals( -1, nodeToEdit.getNodeEventManager().getNodeEventByArrayID(24).getIndex(), "event index unknown");

        r.setElement(3, 0); // 0 events
        nodeToEdit.getCanListener().reply(r);
        assertEquals( 0, nodeToEdit.getNodeEventManager().getTotalNodeEvents(), "0 events");
        assertEquals( 0, nodeToEdit.getNodeEventManager().getOutstandingEvVars(), "0 event variables outstanding");

        nodeToEdit.removePropertyChangeListener(listener);
    }

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

        nodeToEdit = nodeModel.provideNodeByNodeNum(7961);
    }

    @AfterEach
    public void tearDown() {
        nodeToEdit.dispose();
        nodeModel.dispose();
        tcis.terminateThreads();
        memo.dispose();
        nodeToEdit = null;
        nodeModel = null;
        tcis = null;
        memo = null;

        JUnitUtil.tearDown();
    }

    // private static final Logger log = LoggerFactory.getLogger(CbusNodeCanListenerTest.class);

}
