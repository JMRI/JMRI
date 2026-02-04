package jmri.jmrix.can.cbus.node;

import java.io.File;

import jmri.jmrix.can.CanMessage;
import jmri.jmrix.can.CanReply;
import jmri.jmrix.can.CanSystemConnectionMemo;
import jmri.jmrix.can.TrafficControllerScaffold;
import jmri.jmrix.can.cbus.*;
import jmri.util.JUnitUtil;
import jmri.util.JUnitAppender;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;

import static org.junit.jupiter.api.Assertions.*;

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
        assertNotNull( t, "exists");
        t.dispose();
    }

    @Test
    public void testCanListenAndRemove() {
        assertEquals( 0, tcis.numListeners(), "no listener to start with");
        CbusNode t = new CbusNode(memo,256);
        assertEquals( 1, tcis.numListeners(), "table listening");
        t.dispose();
        assertEquals( 0, tcis.numListeners(), "no listener to finish with");
    }

    @Test
    public void testDefaultGets() {

        CbusNode t = new CbusNode(memo,256);
        assertEquals( 256, t.getNodeNumber(), "nodenum");
        assertEquals( -1, t.getCsNum(), "default cs num");
        assertEquals( -1, t.getNodeEventManager().getTotalNodeEvents(), "default getTotalNodeEvents");
        assertEquals( -1, t.getNodeEventManager().getLoadedNodeEvents(), "default getLoadedNodeEvents");
        assertEquals( -1, t.getNodeParamManager().getParameter(0), "default parameter 0");
        assertEquals( -1, t.getNodeNvManager().getNV(0), "default getNV 0");
        assertEquals( 0, t.getNodeNvManager().getTotalNVs(), "default getTotalNVs 0");
        assertEquals( 1, t.getNodeCanId(), "default getNodeCanId");
        assertTrue( t.getNodeStats().getNodeTypeName().isEmpty(), "default getNodeTypeName");
        assertTrue( t.getNodeInFLiMMode(),"default getNodeInFLiMMode");
        assertFalse( t.getNodeInSetupMode(),"default getNodeInSetupMode");
        assertEquals( "256", t.getNodeStats().getNodeNumberName(), "default getNodeNumberName");
        assertTrue( t.getsendsWRACKonNVSET(),"default getsendsWRACKonNVSET");
        assertFalse( t.getnvWriteInLearnOnly(),"default getsendsWRACKonNVSET");
        assertEquals( -1, t.getNodeStats().totalNodeBytes(),"default totalNodeBytes");
        assertEquals( -1, t.getNodeStats().totalRemainingNodeBytes(),"default totalRemainingNodeBytes");
        assertEquals( "256", t.toString(), "default toString");
        assertEquals( -1, t.getNodeFlags(), "default getNodeFlags");
        assertEquals( -1, t.getNodeEventManager().getOutstandingEvVars(), "default getOutstandingEvVars");
        assertFalse( t.getNodeTimerManager().hasActiveTimers(), "default hasActiveTimers");
        assertFalse( t.getNodeEventManager().isEventIndexValid(), "default isEventIndexValid");
        assertNull( t.getNodeBackupManager().getFirstBackupTime(), "No First Backup Timestamp");
        assertNull( t.getNodeBackupManager().getLastBackupTime(), "No Last Backup Timestamp");
        assertEquals( 0, t.getNodeBackupManager().getNumCompleteBackups(), "0 Backups");
        assertEquals( t.getNodeBackupManager().getSessionBackupStatus(),
            CbusNodeConstants.BackupType.OUTSTANDING, "Backup Outstanding");

        t.dispose();
    }

    @Test
    public void testInOutLearnModeExtendedRtr() {
        CbusNode t = new CbusNode(memo,1234);

        assertFalse( t.getNodeInLearnMode(),"default getNodeInLearnMode");

        // frame to set node into learn
        CanReply r = new CanReply();
        r.setHeader(tcis.getCanid());
        r.setNumDataElements(3);
        r.setElement(0, CbusConstants.CBUS_NNLRN);
        r.setElement(1, 0x04);
        r.setElement(2, 0xd2);
        t.getCanListener().reply(r);
        assertTrue( t.getNodeInLearnMode(), "reply in learn mode");

        r = new CanReply();
        r.setHeader(tcis.getCanid());
        r.setNumDataElements(3);
        r.setElement(0, CbusConstants.CBUS_NNULN);
        r.setElement(1, 0x04);
        r.setElement(2, 0xd2);
        t.getCanListener().reply(r);
        assertFalse( t.getNodeInLearnMode(), "reply exit learn mode");

        // frame to set node into learn
        CanMessage m = new CanMessage( tcis.getCanid() );
        m.setNumDataElements(3);
        m.setElement(0, CbusConstants.CBUS_NNLRN);
        m.setElement(1, 0x04);
        m.setElement(2, 0xd2);
        t.getCanListener().message(m);
        assertTrue( t.getNodeInLearnMode(), "message enter learn mode");

        m = new CanMessage( tcis.getCanid() );
        m.setNumDataElements(3);
        m.setElement(0, CbusConstants.CBUS_NNULN);
        m.setElement(1, 0x04);
        m.setElement(2, 0xd2);
        t.getCanListener().message(m);
        assertFalse( t.getNodeInLearnMode(), "message exit learn mode");

        // any message which does nothing to node does not crash it
        m = new CanMessage( tcis.getCanid() );
        m.setNumDataElements(1);
        m.setElement(0, CbusConstants.CBUS_RTOF); // request track off
        t.getCanListener().message(m);
        assertFalse( t.getNodeInLearnMode(), "not in learn mode");


        m = new CanMessage( tcis.getCanid() );
        m.setElement(0, CbusConstants.CBUS_NNLRN); // enter learn mode
        m.setElement(1, 0x04);
        m.setElement(2, 0xd2);

        m.setExtended(true);

        t.getCanListener().message(m);
        assertFalse( t.getNodeInLearnMode(), "no change ext");

        m.setExtended(false);
        m.setRtr(true);
        t.getCanListener().message(m);
        assertFalse( t.getNodeInLearnMode(), "no change rtr");

        m.setRtr(false);
        t.getCanListener().message(m);
        assertTrue( t.getNodeInLearnMode(), "message enter learn mode");


        r = new CanReply();
        r.setHeader(tcis.getCanid());
        r.setNumDataElements(3);
        r.setElement(0, CbusConstants.CBUS_NNULN);
        r.setElement(1, 0x04);
        r.setElement(2, 0xd2);

        r.setExtended(true);
        t.getCanListener().reply(r);
        assertTrue( t.getNodeInLearnMode(), "no change ext");

        r.setExtended(false);
        r.setRtr(true);
        t.getCanListener().reply(r);
        assertTrue( t.getNodeInLearnMode(), "no change rtr");

        t.dispose();

    }

    @Test
    public void testSetName() {

        CbusNode t = new CbusNode(memo,12345);
        assertTrue( t.getUserName().isEmpty(), "default getUserName");

        t.setUserName("Alonso Smith");
        assertEquals( "Alonso Smith", t.getUserName(), "username set");
        assertEquals( "12345 Alonso Smith", t.toString(), "Alonso toString");

        t.setNameIfNoName("purple");
        assertEquals( "Alonso Smith",t.getUserName(), "username unchanged");

        CbusNode tb = new CbusNode(memo,123);
        tb.setNameIfNoName("shirley");
        assertEquals( "shirley", tb.getUserName(), "username set if no name");
        assertEquals( "123 shirley", tb.getNodeStats().getNodeNumberName(), "username number");

        t.dispose();
        tb.dispose();
    }

    @Test
    public void testStartGetParams() {

        assertEquals( 0, tcis.outbound.size(), "tcis empty at start");
        CbusNode t = new CbusNode(memo,12345);

        assertEquals( -1, t.getNodeParamManager().getParameter(0), "default parameter 0");
        // only 1 parameter awaiting knowledge of until total confirmed
        assertEquals( 8,t.getNodeParamManager().getOutstandingParams(), "default outstanding parameters");

        assertEquals( 0 ,tcis.outbound.size(), "tcis empty after creating new node");
        t.getNodeParamManager().sendRequestNextParam();
        assertEquals( 1 ,tcis.outbound.size(), "Node has requested parameter 0");
        t.getNodeParamManager().sendRequestNextParam();
        assertEquals( 1 ,tcis.outbound.size(), "Node has already requested parameter 0");
        assertEquals( "[5f8] 73 30 39 00", tcis.outbound.elementAt(tcis.outbound.size() - 1).toString(),
            "Message sent is parameter request");

        // frame from physical node to CbusNode advising has 7 parameters
        CanReply r = new CanReply();
        r.setHeader(tcis.getCanid());
        r.setNumDataElements(5);
        r.setElement(0, CbusConstants.CBUS_PARAN);
        r.setElement(1, 0x30); // nodenum 12345
        r.setElement(2, 0x39); // nodenum 12345
        r.setElement(3, 0x00); // parameter 0
        r.setElement(4, 0x07); // value 7
        t.getCanListener().reply(r);
        assertEquals( 7, t.getNodeParamManager().getParameter(0), "parameter 0 value 7");
        assertEquals( 7, t.getNodeParamManager().getOutstandingParams(), "default outstanding parameters 7" );
        t.getNodeParamManager().sendRequestNextParam();
        // JUnitUtil.waitFor(()->{ return(tcis.outbound.size()>1); }, " outbound 1 didn't arrive");


        assertEquals( 2 ,tcis.outbound.size(), "CbusNode has requested parameter 1 manufacturer");
        assertEquals( "[5f8] 73 30 39 01", tcis.outbound.elementAt(tcis.outbound.size() - 1).toString(),
            "Message sent is parameter 1 request");

        r = new CanReply();
        r.setHeader(tcis.getCanid());
        r.setNumDataElements(5);
        r.setElement(0, CbusConstants.CBUS_PARAN);
        r.setElement(1, 0x30);
        r.setElement(2, 0x39);
        r.setElement(3, 0x01); // parameter 1
        r.setElement(4, 0xa5); // dec 165 MERG
        t.getCanListener().reply(r);
        assertEquals( 165, t.getNodeParamManager().getParameter(1), "parameter 1 value 165");
        t.getNodeParamManager().sendRequestNextParam();
        assertEquals( 3 ,tcis.outbound.size(), "CbusNode has requested parameter 3 module type identifier");
        assertEquals( "[5f8] 73 30 39 03", tcis.outbound.elementAt(tcis.outbound.size() - 1).toString(),
            "Message sent is parameter 3 request");

        r = new CanReply();
        r.setHeader(tcis.getCanid());
        r.setNumDataElements(5);
        r.setElement(0, CbusConstants.CBUS_PARAN);
        r.setElement(1, 0x30);
        r.setElement(2, 0x39);
        r.setElement(3, 0x03); // parameter 3
        r.setElement(4, 0x1d); // dec 29 CANPAN
        t.getCanListener().reply(r);

        // now we know params 1 and 3, try nodetype lookup
        assertEquals( "CANPAN" ,t.getNodeStats().getNodeTypeName(), "CbusNode identified as a CANPAN");
        t.getNodeParamManager().sendRequestNextParam();
        assertEquals( 4 ,tcis.outbound.size(), "CbusNode has requested parameter 6 number nv's");
        assertEquals( "[5f8] 73 30 39 06", tcis.outbound.elementAt(tcis.outbound.size() - 1).toString(),
            "Message sent is parameter 6 request");

        r = new CanReply();
        r.setHeader(tcis.getCanid());
        r.setNumDataElements(5);
        r.setElement(0, CbusConstants.CBUS_PARAN);
        r.setElement(1, 0x30);
        r.setElement(2, 0x39);
        r.setElement(3, 0x06); // parameter 6
        r.setElement(4, 0x03); // 3 NV's
        t.getCanListener().reply(r);

        // now we know number of NV's
        assertEquals( 3, t.getNodeNvManager().getTotalNVs(), "getTotalNVs 3");
        assertEquals( 3, t.getNodeNvManager().getOutstandingNvCount(), "get oustanding NVs 3");
        assertEquals( 3, t.getNodeNvManager().getNV(0), "get NVs 0 3");
        assertEquals( -1, t.getNodeNvManager().getNV(1), "get NVs 1 -1");
        assertEquals( -1, t.getNodeNvManager().getNV(3), "get NVs 3 -1");

        t.getNodeParamManager().sendRequestNextParam();
        assertEquals( 5 ,tcis.outbound.size(), "CbusNode has requested parameter 6 number ev vars per event");
        assertEquals( "[5f8] 73 30 39 05", tcis.outbound.elementAt(tcis.outbound.size() - 1).toString(),
            "Message sent is parameter 5 request");

        r = new CanReply();
        r.setHeader(tcis.getCanid());
        r.setNumDataElements(5);
        r.setElement(0, CbusConstants.CBUS_PARAN);
        r.setElement(1, 0x30);
        r.setElement(2, 0x39);
        r.setElement(3, 0x05); // parameter 5
        r.setElement(4, 0x07); // 7 ev vars per ev
        t.getCanListener().reply(r);

        assertEquals( 3, t.getNodeParamManager().getOutstandingParams(), "outstanding parameters 3");

        t.getNodeParamManager().sendRequestNextParam();
        assertEquals( 6 ,tcis.outbound.size(), "CbusNode has requested parameter 7 firmware major");
        assertEquals( "[5f8] 73 30 39 07", tcis.outbound.elementAt(tcis.outbound.size() - 1).toString(),
            "Message sent is parameter 7 request");

        r = new CanReply();
        r.setHeader(tcis.getCanid());
        r.setNumDataElements(5);
        r.setElement(0, CbusConstants.CBUS_PARAN);
        r.setElement(1, 0x30);
        r.setElement(2, 0x39);
        r.setElement(3, 0x07); // parameter 7
        r.setElement(4, 0x02); // firmware pt1 2
        t.getCanListener().reply(r);

        t.getNodeParamManager().sendRequestNextParam();

        assertEquals( 7 ,tcis.outbound.size(), "CbusNode has requested parameter 2 firmware minor");
        assertEquals( "[5f8] 73 30 39 02", tcis.outbound.elementAt(tcis.outbound.size() - 1).toString(),
            "Message sent is parameter 2 request");
        assertTrue( t.getsendsWRACKonNVSET(), "default getsendsWRACKonNVSET");
        assertFalse( t.getnvWriteInLearnOnly(), "default getnvWriteInLearnOnly");

        r = new CanReply();
        r.setHeader(tcis.getCanid());
        r.setNumDataElements(5);
        r.setElement(0, CbusConstants.CBUS_PARAN);
        r.setElement(1, 0x30);
        r.setElement(2, 0x39);
        r.setElement(3, 0x02); // parameter 2
        r.setElement(4, 0x01); // firmware pt2 1
        t.getCanListener().reply(r);

        assertEquals( 1, t.getNodeParamManager().getOutstandingParams(), " outstanding parameter 1");

        assertEquals( "[7, 165, 1, 29, -1, 7, 3, 2]",
            java.util.Arrays.toString(t.getNodeParamManager().getParameters() ),
            "getParameters ");

        t.getNodeParamManager().sendRequestNextParam();

        // with this we should expect CbusNodeConstants.setTraits to have been called
        assertFalse( t.getsendsWRACKonNVSET(), "setTraits getsendsWRACKonNVSET ");

        assertEquals( 8 ,tcis.outbound.size(), "CbusNode has requested number of events");
        assertEquals( "[5f8] 58 30 39",
            tcis.outbound.elementAt(tcis.outbound.size() - 1).toString(),
            "Message sent numev request");

        t.dispose();

    }

    @Test
    public void testAddEvents() {

        CbusNode t = new CbusNode(memo,12345);

        assertEquals( -1, t.getNodeEventManager().getTotalNodeEvents(), "default getTotalNodeEvents");
        assertFalse( t.getNodeTimerManager().hasActiveTimers(), "default active timers false, ie nothing requested");


        // set node to 4 ev vars per event, para 5
        t.getNodeParamManager().setParameters(new int[]{8,1,2,3,4,4,6,7,8});

        CbusNodeEvent ev = new CbusNodeEvent(memo,0,7,12345,77,4);  // nn, en, thisnode, index, maxevvar

        t.getNodeEventManager().addNewEvent(ev);
        // index on the event is set to -1, ie unknown when an event is added to the node
        assertEquals( -1, ev.getIndex(), "index in array -1");

        assertEquals( 1, t.getNodeEventManager().getTotalNodeEvents(), "after new ev getTotalNodeEvents 1");
        assertEquals( 4, t.getNodeEventManager().getOutstandingEvVars(), "after new ev getOutstandingEvVars 1");

        CbusNodeEvent evb = new CbusNodeEvent(memo,0,8,12345,2,4);
        t.getNodeEventManager().addNewEvent(evb);
        assertEquals( 2, t.getNodeEventManager().getTotalNodeEvents(), "after new ev getTotalNodeEvents 2");
        assertEquals( 2, t.getNodeEventManager().getLoadedNodeEvents(), "after new ev getLoadedNodeEvents 2");
        assertEquals( 0, t.getNodeEventManager().getOutstandingIndexNodeEvents(), "getOutstandingIndexNodeEvents 0");

        assertEquals( ev, t.getNodeEventManager().getNodeEvent(0,7), "node event fetch node 0 ev 7");
        assertNull( t.getNodeEventManager().getNodeEvent(321,654), "node event fetch null node 321 ev 645");
        assertEquals( evb, t.getNodeEventManager().provideNodeEvent(0,8), "node event provide node 0 ev87");
        assertEquals( 2, t.getNodeEventManager().getTotalNodeEvents(), "after provide 321 645 getTotalNodeEvents 3");
        t.getNodeEventManager().provideNodeEvent(321,654);
        assertEquals( 3, t.getNodeEventManager().getTotalNodeEvents(), "after provide 321 645 getTotalNodeEvents 3");

        java.util.ArrayList<CbusNodeEvent> _tArr = t.getNodeEventManager().getEventArray();
        assertNotNull( _tArr, "Array should not be null");
        assertEquals( 3, _tArr.size(), "event array size 3");

        ev.setIndex(77);
        evb.setIndex(88);

        assertEquals( 1, t.getNodeEventManager().getEventRowFromIndex(88), "index in array 1");
        assertEquals( 0, t.getNodeEventManager().getEventRowFromIndex(77), "index in array 0");
        assertEquals( -1, t.getNodeEventManager().getEventRowFromIndex(999), "index 999 not in array");

        t.getNodeEventManager().addNewEvent(new CbusNodeEvent(memo,123,-1,12345,3,4));
        t.getNodeEventManager().addNewEvent(new CbusNodeEvent(memo,-1,123,12345,4,4));

        assertEquals( 5, t.getNodeEventManager().getTotalNodeEvents(), "after remove getTotalNodeEvents 2");
        assertEquals( 3, t.getNodeEventManager().getLoadedNodeEvents(), "after new ev getLoadedNodeEvents 3");

        t.getNodeEventManager().removeEvent(0,8);
        assertNull( t.getNodeEventManager().getNodeEvent(0,8), "node event remove node 0 ev 8");
        assertNotNull( t.getNodeEventManager().getNodeEvent(321,654), "node event not removed others");
        assertEquals( 4, t.getNodeEventManager().getTotalNodeEvents(), "after remove getTotalNodeEvents 4");
        assertNull( t.getNodeEventManager().getNodeEventByIndex(999), "event fetched by index");

        t.setNodeInLearnMode(true);

        CanReply r = new CanReply();
        r.setHeader(tcis.getCanid());
        r.setNumDataElements(3);
        r.setElement(0, CbusConstants.CBUS_NNCLR);
        r.setElement(1, 0x30);
        r.setElement(2, 0x00);
        t.getCanListener().reply(r);

        assertEquals( 4, t.getNodeEventManager().getTotalNodeEvents(), "after remove getTotalNodeEvents 4 wrong node number");
        r.setElement(2, 0x39);
        t.getCanListener().reply(r);
        assertEquals( 0,t.getNodeEventManager().getTotalNodeEvents(), "after CBUS_NNCLR getTotalNodeEvents 0");

        t.dispose();

    }

    @Test
    public void testAddEventsFromCan() {

        CbusNode t = new CbusNode(memo,12345);
        // set node to 4 ev vars per event
        t.getNodeParamManager().setParameters(new int[]{8,1,2,3,4,4,6,7,8});

        t.setNodeInLearnMode(true);

        // frame to set node into learn
        CanReply r = new CanReply();
        r.setHeader(tcis.getCanid());
        r.setNumDataElements(7);
        r.setElement(0, CbusConstants.CBUS_EVLRN);
        r.setElement(1, 0x30); // nn 12344
        r.setElement(2, 0x38); // nn 12344
        r.setElement(3, 0x00); // en 7
        r.setElement(4, 0x07); // en 7
        r.setElement(5, 0x01); // ev index 1
        r.setElement(6, 0xff); // ev val 255
        t.getCanListener().reply(r);

        assertEquals( 1, t.getNodeEventManager().getTotalNodeEvents(), "getTotalNodeEvents 1");
        assertEquals( 1, t.getNodeEventManager().getLoadedNodeEvents(), "getLoadedNodeEvents 1");
        assertEquals( 3, t.getNodeEventManager().getOutstandingEvVars(), "getOutstandingEvVars 3");
        assertEquals( 0 ,tcis.outbound.size(), "Node has not sent a message");

        r = new CanReply();
        r.setHeader(tcis.getCanid());
        r.setNumDataElements(5);
        r.setElement(0, CbusConstants.CBUS_EVULN);
        r.setElement(1, 0x30); // nn 12344
        r.setElement(2, 0x38); // nn 12344
        r.setElement(3, 0x00); // en 6
        r.setElement(4, 0x06); // en 6
        t.getCanListener().reply(r);
        assertEquals( 1, t.getNodeEventManager().getTotalNodeEvents(), "getTotalNodeEvents 1 wrong event");

        r.setElement(4, 0x07); // en 7
        t.getCanListener().reply(r);
        assertEquals( 0, t.getNodeEventManager().getTotalNodeEvents(), "getTotalNodeEvents 0");

        t.dispose();

    }


    @Test
    public void testFetchEventsFromCanWithNodeTable() {

        // needs table model adding to check for any other nodes in learn mode
        CbusNodeTableDataModel tModel = memo.get(CbusConfigurationManager.class)
            .provide(CbusNodeTableDataModel.class);

        CbusNode t = new CbusNode(memo,12345);
        // set node to 3 ev vars per event( param 5) , 0 NV's ( param 6)
        t.getNodeParamManager().setParameters(new int[]{8,1,2,3,4,3,0,7,8});

        tModel.addNode(t);

        CanReply r = new CanReply();
        r.setHeader(tcis.getCanid());
        r.setNumDataElements(4);
        r.setElement(0, CbusConstants.CBUS_NUMEV);
        r.setElement(1, 0x30); // nn 12345
        r.setElement(2, 0x39); // nn 12345
        r.setElement(3, 0x02); // node has 2 events
        t.getCanListener().reply(r);
        assertEquals( 2, t.getNodeEventManager().getTotalNodeEvents(), "CBUS_NUMEV 2");
        assertEquals(0.5 ,t.getNodeStats().floatPercentageRemaining(), 0.0001f );

        t.getNodeEventManager().sendNextEvVarToFetch();
        assertEquals( 1 ,tcis.outbound.size(), "Node has sent a message");
        assertEquals( "[5f8] 57 30 39",
            tcis.outbound.elementAt(tcis.outbound.size() - 1).toString(),
            "Node sends NERD request");
        assertTrue( t.getNodeTimerManager().hasActiveTimers(), "active timer true for NERD / ENRSP responses");

        // reply to NERD
        r = new CanReply();
        r.setHeader(tcis.getCanid());
        r.setNumDataElements(8);
        r.setElement(0, CbusConstants.CBUS_ENRSP);
        r.setElement(1, 0x30); // nn 12345
        r.setElement(2, 0x39); // nn 12345
        r.setElement(3, 0x30); // nn 12344
        r.setElement(4, 0x38); // nn 12344
        r.setElement(5, 0x00); // en 7
        r.setElement(6, 0x07); // en 7
        r.setElement(7, 0x01); // ev index 1

        t.getCanListener().reply(r);
        assertEquals( 2, t.getNodeEventManager().getTotalNodeEvents(), "CBUS_ENRSP 2");
        assertEquals(0.5625 ,t.getNodeStats().floatPercentageRemaining(), 0.0001f );

        assertEquals( 1, t.getNodeEventManager().getLoadedNodeEvents(), "getLoadedNodeEvents 1");
        assertEquals( 6, t.getNodeEventManager().getOutstandingEvVars(), "getOutstandingEvVars 3");
        assertFalse( t.getNodeEventManager().isEventIndexValid(), "isEventIndexValid false");

        r = new CanReply();
        r.setHeader(tcis.getCanid());
        r.setNumDataElements(8);
        r.setElement(0, CbusConstants.CBUS_ENRSP);
        r.setElement(1, 0x30); // nn 12345
        r.setElement(2, 0x39); // nn 12345
        r.setElement(3, 0x30); // nn 12344
        r.setElement(4, 0x38); // nn 12344
        r.setElement(5, 0x00); // en 4
        r.setElement(6, 0x04); // en 4
        r.setElement(7, 0x03); // ev index 3
        t.getCanListener().reply(r);

        assertEquals( 2, t.getNodeEventManager().getTotalNodeEvents(), "CBUS_ENRSP 2 again");
        assertEquals(0.625 ,t.getNodeStats().floatPercentageRemaining(), 0.0001f );
        assertTrue( t.getNodeEventManager().isEventIndexValid(), "isEventIndexValid true");
        assertFalse( t.getNodeTimerManager().hasActiveTimers(), "No active timer after NERD / ENRSP responses");
        assertEquals( 2, t.getNodeEventManager().getLoadedNodeEvents(), "getLoadedNodeEvents 2");

        JUnitUtil.waitFor( () -> tcis.outbound.size() > 1, "Node sends message via Model");
        assertEquals( 2 ,tcis.outbound.size(), "Node has sent a message via model " + tcis.outbound);

        assertEquals( "[5f8] 9C 30 39 01 01",
            tcis.outbound.elementAt(tcis.outbound.size() - 1).toString(),
            "Node sends ev var request");
        assertTrue( t.getNodeTimerManager().hasActiveTimers(), "active timer true awaiting response");

        r = new CanReply();
        r.setHeader(tcis.getCanid());
        r.setNumDataElements(6);
        r.setElement(0, CbusConstants.CBUS_NEVAL);
        r.setElement(1, 0x30); // nn 12345
        r.setElement(2, 0x39); // nn 12345
        r.setElement(3, 0x01); // ev index
        r.setElement(4, 0x01); // ev var index
        r.setElement(5, 0x00); // ev var value

        t.getCanListener().reply(r);

        assertEquals(0.6875 ,t.getNodeStats().floatPercentageRemaining(), 0.0001f );
        assertFalse( t.getNodeTimerManager().hasActiveTimers(), "No active timer after NEVAL response");

        JUnitUtil.waitFor( () -> tcis.outbound.size() > 2, "Node sends message 2 via Model");
        assertEquals( 3 ,tcis.outbound.size(), "Node has sent a message via model");
        assertEquals( "[5f8] 9C 30 39 01 02",
            tcis.outbound.elementAt(tcis.outbound.size() - 1).toString(),
            "Node sends ev var request");

        r.setElement(4, 0x02); // ev var index
        r.setElement(5, 0xff); // ev var value
        t.getCanListener().reply(r);
        assertEquals(0.75 ,t.getNodeStats().floatPercentageRemaining(), 0.0001f );

        JUnitUtil.waitFor(() -> tcis.outbound.size() > 3, "tcis outbound has 4, was " + tcis.outbound);
        assertEquals( 4, tcis.outbound.size(), "Node has sent message 4 via model " + tcis.outbound );
        assertEquals( "[5f8] 9C 30 39 01 03", tcis.outbound.elementAt(tcis.outbound.size() - 1).toString(),
            "Node sends ev var request " + tcis.outbound);


        r.setElement(4, 0x03); // ev var index
        r.setElement(5, 0xd1); // ev var value
        t.getCanListener().reply(r);
        assertEquals(0.8125 ,t.getNodeStats().floatPercentageRemaining(), 0.0001f );

        JUnitUtil.waitFor(() -> tcis.outbound.size() > 4, "tcis outbound has 5, was " + tcis.outbound);
        assertEquals( "[5f8] 9C 30 39 03 01",
            tcis.outbound.elementAt(tcis.outbound.size() - 1).toString(),
            "Node sends ev var request index 3");

        r.setElement(3, 0x03); // ev index
        r.setElement(4, 0x01); // ev var index
        r.setElement(5, 0xd1); // ev var value
        t.getCanListener().reply(r);
        assertEquals(0.875 ,t.getNodeStats().floatPercentageRemaining(), 0.0001f );

        assertEquals( 0, t.getNodeBackupManager().getNumCompleteBackups(),
            "0 Backups in middle of fetch");

        JUnitUtil.waitFor(() -> tcis.outbound.size() > 5, "tcis outbound has 6, was " + tcis.outbound);
        r.setElement(4, 0x02); // ev var index
        r.setElement(5, 0xd1); // ev var value
        t.getCanListener().reply(r);

        assertEquals(0.9375 ,t.getNodeStats().floatPercentageRemaining(), 0.0001f );

        JUnitUtil.waitFor(() -> tcis.outbound.size() > 6, "tcis outbound has 7, was " + tcis.outbound);
        r.setElement(4, 0x03); // ev var index
        r.setElement(5, 0xd1); // ev var value
        t.getCanListener().reply(r);

        assertEquals(1.0 ,t.getNodeStats().floatPercentageRemaining(), 0.0001f );

        tModel.sendNextBackgroundFetch(); // all done, should send nothing

        assertEquals( 7 ,tcis.outbound.size(), "Node has NOT sent a message via model");
        assertFalse( t.getNodeTimerManager().hasActiveTimers(), "No active timer after event fetch complete");

        // check if node has loaded an xml file
        assertNotNull( t.getNodeBackupManager().getFirstBackupTime(), "First Backup Timestamp exists");
        assertNotNull( t.getNodeBackupManager().getLastBackupTime(), "Last Backup Timestamp exists");

        assertEquals( t.getNodeBackupManager().getSessionBackupStatus(),CbusNodeConstants.BackupType.COMPLETE,
            "Backup Complete");
        assertTrue(t.getNodeBackupManager().removeNode(false));

        tModel.dispose();
        t.dispose();

    }

    @Test
    public void testAddNodeVariables() {

        CbusNode t = new CbusNode(memo,12345);

        assertNull( t.getNodeNvManager().getNvArray(), "default getNvArray ");
        assertEquals( -1, t.getNodeNvManager().getNV(0), "default getNv 0 ");
        assertEquals( -1, t.getNodeNvManager().getNV(3), "default getNv 3 ");
        assertEquals( -1, t.getNodeNvManager().getOutstandingNvCount(), "default getOutstandingNvCount");

        // set node to 3 node vars , param6
        t.getNodeParamManager().setParameters(new int[]{7,1,2,3,4,5,3,7});

        assertNotNull( t.getNodeNvManager().getNvArray(), "3 node vars getNvArray ");
        assertEquals( 3, t.getNodeNvManager().getNV(0), "3 node vars getNv 0");
        assertEquals( -1, t.getNodeNvManager().getNV(3), "3 node vars getNv 3");
        assertEquals( 3, t.getNodeNvManager().getOutstandingNvCount(), "3 node vars getOutstandingNvCount ");

        t.getNodeNvManager().setNV(1,1);
        t.getNodeNvManager().setNV(2,2);
        t.getNodeNvManager().setNV(3,3);
        assertEquals( 0, t.getNodeNvManager().getOutstandingNvCount(), "node vars getOutstandingNvCount");

        // setNVs
        t.dispose();

    }


    @Test
    public void testErrorsFromCan() {

        CbusNode t = new CbusNode(memo,12345);
        // set node to 4 ev vars per event
        t.getNodeParamManager().setParameters(new int[]{8,1,2,3,4,4,6,7,8});

        CanReply r = new CanReply();
        r.setHeader(tcis.getCanid());
        r.setNumDataElements(4);
        r.setElement(0, CbusConstants.CBUS_CMDERR);
        r.setElement(1, 0x30); // nn 12345
        r.setElement(2, 0x39); // nn 12345
        r.setElement(3, 0xff); // error 255 does not exist
        t.getCanListener().reply(r);
        JUnitAppender.assertErrorMessageStartsWith("Node 12345 Reporting Error Code 255 (decimal)");

        r.setElement(3, 0x00); // error 0 does not exist
        t.getCanListener().reply(r);

        JUnitAppender.assertErrorMessageStartsWith("Node 12345 Reporting Error Code 0 (decimal)");

        r.setElement(3, 0x04); // error code 4
        t.getCanListener().reply(r);
        JUnitAppender.assertErrorMessageStartsWith("Node 12345 reporting ERROR : Too Many Events");

        t.dispose();
    }

    @Test
    public void testgetNodeTypeString() {

        CbusNode t = new CbusNode(memo,12345);
        t.getNodeParamManager().setParameters(new int[]{8,165,89,10,4,5,3,4,8});

        assertEquals( "MERG Command Station CANCMD Firmware ver: 4Y Node 12345",
            t.getNodeParamManager().getNodeTypeString(),
            "165 10 getNodeTypeString ");

        t.getNodeParamManager().setParameters(new int[]{20,70,90,4,4,5,3,4,8,9,10,11,12,13,14,15,16,17,18,19,77});

        assertEquals( "ROCRAIL 8 channel RFID reader. CANGC4 Firmware ver: 4Z Beta 77 Node 12345",
            t.getNodeParamManager().getNodeTypeString(),
            "70 4 getNodeTypeString ");

        t.getNodeParamManager().setParameters(new int[]{8,0,89,0,4,5,3,4,8});
        assertEquals("",t.getNodeStats().getNodeTypeName(), "0 0 getNodeTypeName");
        t.setNodeNameFromName("Alonso");
        assertEquals( "Alonso",t.getNodeStats().getNodeTypeName(),"0 0 Alonso getNodeTypeName");

        t.dispose();

    }

    @BeforeEach
    public void setUp(@TempDir File folder) throws java.io.IOException {
        JUnitUtil.setUp();
        JUnitUtil.resetInstanceManager();
        JUnitUtil.resetProfileManager(new jmri.profile.NullProfile(folder));

        memo = new CanSystemConnectionMemo();
        tcis = new TrafficControllerScaffold();
        memo.setTrafficController(tcis);
        memo.setProtocol(jmri.jmrix.can.CanConfigurationManager.MERGCBUS);

    }

    @AfterEach
    public void tearDown() {

        tcis.terminateThreads();
        memo.dispose();
        tcis = null;
        memo = null;
        JUnitUtil.tearDown();

    }

    // private final static Logger log = LoggerFactory.getLogger(CbusNodeTest.class);

}
