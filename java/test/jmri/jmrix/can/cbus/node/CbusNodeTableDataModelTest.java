package jmri.jmrix.can.cbus.node;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;

import jmri.jmrix.can.CanMessage;
import jmri.jmrix.can.CanReply;
import jmri.jmrix.can.CanSystemConnectionMemo;
import jmri.jmrix.can.TrafficControllerScaffold;
import jmri.jmrix.can.TrafficControllerScaffoldLoopback;
import jmri.jmrix.can.cbus.CbusConfigurationManager;
import jmri.jmrix.can.cbus.CbusConstants;
import jmri.jmrix.can.cbus.CbusPreferences;
import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 * @author Steve Young Copyright (C) 2019
 */
public class CbusNodeTableDataModelTest {

    @Test
    public void testCTor() {

        t = memo.get(CbusConfigurationManager.class)
            .provide(CbusNodeTableDataModel.class);
        assertNotNull( t, "exists");
        t.dispose();

    }

    @Test
    public void testDefaults() {

        t = memo.get(CbusConfigurationManager.class)
            .provide(CbusNodeTableDataModel.class);
        assertEquals( -1, t.getAnyNodeInLearnMode(), "default getAnyNodeInLearnMode -1" );
        assertNull( t.getCsByNum(0), "default getCsByNum0 null");
        assertTrue( t.getListOfNodeNumberNames().isEmpty(), "default getListOfNodeNumberNames 0 length list");
        assertEquals( 777, t.getNextAvailableNodeNumber(777), "default getNextAvailableNodeNumber 777");
        assertNull( t.getNodeByNodeNum(1234), "default getNodeByNodeNum 1234");
        assertTrue( t.getNodeName(1234).isEmpty(), "default getNodeName 1234");
        assertTrue( t.getNodeNameFromCanId(15).isEmpty(), "default getNodeNameFromCanId 15");
        assertTrue( t.getNodeNumberName(1234).isEmpty(), "default getNodeNumberName 1234");
        assertEquals( -1, t.getNodeRowFromNodeNum(1234), "default getNodeRowFromNodeNum 1234");
        assertEquals( 0, t.getRowCount(), "default getRowCount 0");

        t.dispose();
    }

    @Test
    public void testCanListener() {
        assertEquals( 0, tcis.numListeners(), "no listener to start with");
        t = memo.get(CbusConfigurationManager.class)
            .provide(CbusNodeTableDataModel.class);
        t.setBackgroundAllocateListener(false);
        assertNotNull( t, "exists");

        assertEquals( 2, tcis.numListeners(), "listener attached");
        t.dispose();
        assertEquals( 1, tcis.numListeners(), "programmer listener remaining");

    }

    @Test
    public void testCanMsgReplyCmndstation() {

        CbusPreferences pref = memo.get(CbusPreferences.class);
        Assertions.assertNotNull(pref, "preferences available via memo");

        t = memo.get(CbusConfigurationManager.class)
            .provide(CbusNodeTableDataModel.class);

        assertNull( t.getCsByNum(0), "default getCsByNum0 null");

        assertFalse( pref.getStartupSearchForCs(), "default search cs pref");
        assertFalse( pref.getStartupSearchForNodes(), "default search node pref");

        pref.setAddCommandStations(true);

        CanMessage m = new CanMessage( tcis.getCanid() );
        m.setNumDataElements(8);
        m.setElement(0, CbusConstants.CBUS_STAT); // report from command station
        m.setElement(1, 0x04); // node 1234
        m.setElement(2, 0xd2); // node 1234
        m.setElement(3, 0x00); // cs num
        m.setElement(4, 0x00); // flags
        m.setElement(5, 0x04); // major fw
        m.setElement(6, 0x05); // minor fw
        m.setElement(7, 0x00); // build no. fw
        t.message(m);

        // ignores CanMessage
        assertNull( t.getCsByNum(0), "ignores CanMessage");
        assertEquals( 0, t.getRowCount(), "ignores CanMessage row");

        CanReply r = new CanReply();
        r.setHeader(tcis.getCanid());
        r.setNumDataElements(8);
        r.setElement(0, CbusConstants.CBUS_STAT); // report from command station
        r.setElement(1, 0x04); // node 1234
        r.setElement(2, 0xd2); // node 1234
        r.setElement(3, 0x00); // cs num
        r.setElement(4, 0x00); // flags
        r.setElement(5, 0x04); // major fw
        r.setElement(6, 0x05); // minor fw
        r.setElement(7, 0x00); // build no. fw
        t.reply(r);

        assertNotNull( t.getCsByNum(0), "provides cs 0 CanReply");
        assertNotNull( t.getNodeByNodeNum(1234), "provides cs node 1234 CanReply");
        assertEquals( 1, t.getRowCount(), "provides cs row");
        assertEquals( 1, t.getListOfNodeNumberNames().size(), "default getListOfNodeNumberNames 1 length list");

        assertEquals( 1234, (Integer)t.getValueAt(0,CbusNodeTableDataModel.NODE_NUMBER_COLUMN), "getValueAt cs node");
        assertTrue( ((String)t.getValueAt(0,CbusNodeTableDataModel.NODE_USER_NAME_COLUMN)).isEmpty(), "getValueAt cs user nm");
        assertTrue( ((String)t.getValueAt(0,CbusNodeTableDataModel.NODE_TYPE_NAME_COLUMN)).isEmpty(), "getValueAt cs type nm");
        assertEquals( tcis.getCanid(), (Integer)t.getValueAt(0,CbusNodeTableDataModel.CANID_COLUMN), "getValueAt cs can");
        assertEquals( 0, (Integer)t.getValueAt(0,CbusNodeTableDataModel.COMMAND_STAT_NUMBER_COLUMN), "getValueAt cs num");
        assertEquals( -1,(Integer)t.getValueAt(0,CbusNodeTableDataModel.NODE_EVENTS_COLUMN), "getValueAt cs ev");
        assertEquals( -1, (Integer)t.getValueAt(0,CbusNodeTableDataModel.NODE_TOTAL_BYTES_COLUMN), "getValueAt cs tot bytes");
        assertEquals( 0.0f, (float)t.getValueAt(0,CbusNodeTableDataModel.BYTES_REMAINING_COLUMN), "getValueAt cs byte remain" );
        assertFalse( (boolean)t.getValueAt(0,CbusNodeTableDataModel.NODE_IN_LEARN_MODE_COLUMN), "getValueAt cs ev md");
        assertNull( t.getValueAt(0,999), "getValueAt null");

        t.setValueAt(7,0,CbusNodeTableDataModel.NODE_EVENTS_COLUMN);
        assertEquals( -1, (Integer)t.getValueAt(0,CbusNodeTableDataModel.NODE_EVENTS_COLUMN), "setValueAt does nothing");

        t.setValueAt("Alonso",0,CbusNodeTableDataModel.NODE_USER_NAME_COLUMN);
        assertEquals( "Alonso", ( t.getValueAt(0,CbusNodeTableDataModel.NODE_USER_NAME_COLUMN)), "setValueAt user nm");

        assertNull( t.getNodeByNodeNum(7), "no node 7 CanReply");


        pref.setAddNodes(true);
        t.startASearchForNodes(null,1000);

        r = new CanReply();
        r.setHeader(tcis.getCanid());
        r.setNumDataElements(6);
        r.setElement(0, CbusConstants.CBUS_PNN); // report from node
        r.setElement(1, 0x00); // node 7
        r.setElement(2, 0x07); // node 7
        r.setElement(3, 0xa5); // manu id MERG
        r.setElement(4, 0x1d); // module id CANPAN
        r.setElement(5, 0x04); // flags
        t.reply(r);

        assertNotNull( t.getNodeByNodeNum(7), "provides node 7 CanReply");
        assertEquals( 2, t.getListOfNodeNumberNames().size(), "getListOfNodeNumberNames 2 length list");

        t.dispose();

    }

    @Test
    public void testsendSystemResetAndColumns() {

        t = memo.get(CbusConfigurationManager.class)
            .provide(CbusNodeTableDataModel.class);


        assertEquals( 0 ,tcis.outbound.size(), "tcis empty to start");

        t.provideNodeByNodeNum(123);

        for (int i = 0; i <t.getColumnCount(); i++) {
            assertFalse( t.getColumnName(i).isEmpty(), "column has name");
        }

        assertEquals( "unknown 999", t.getColumnName(999), "column has NO name" );

        assertFalse( t.isCellEditable(0,CbusNodeTableDataModel.NODE_NUMBER_COLUMN),
            "cell not editable");

        assertTrue( t.isCellEditable(0,CbusNodeTableDataModel.NODE_USER_NAME_COLUMN),
            "cell editable");

        assertEquals( Integer.class, t.getColumnClass(CbusNodeTableDataModel.NODE_NUMBER_COLUMN),
            "column class int");

        assertEquals( String.class, t.getColumnClass(CbusNodeTableDataModel.NODE_USER_NAME_COLUMN),
            "column class string" );

        assertEquals( Boolean.class, t.getColumnClass(CbusNodeTableDataModel.NODE_IN_LEARN_MODE_COLUMN),
            "column class Boolean" );

        t.dispose();
    }

    @Test
    public void testNextUrgentFetch() {

        // using loopback to check the ability to monitor the CAN traffic
        TrafficControllerScaffold tcisl = new TrafficControllerScaffoldLoopback();
        memo.setTrafficController(tcisl);

        t = memo.get(CbusConfigurationManager.class)
            .provide(CbusNodeTableDataModel.class);

        CbusNode n1 = t.provideNodeByNodeNum(1);


        // t.setUrgentFetch(int tabindex, int nodenum, int urgentNodeBefore, int urgentNodeAfter){

        // tabindex values see Cbus @NodeConfigToolPane#userViewChanged
        // 0 - Parameters
        // 1 - NV's
        // 2 - EV's

        t.sendNextBackgroundFetch();

        CbusNode n2 = t.provideNodeByNodeNum(2);
        CbusNode n3 = t.provideNodeByNodeNum(3);

        assertNotNull( n2, "exists");
        assertNotNull( n3, "exists");

        JUnitUtil.waitFor(()->{ return( !tcisl.outbound.isEmpty()); }, "TCIS count did not increase");

        assertEquals( 1, tcisl.outbound.size(), "1 Messages sent to get node parameters");

        assertEquals( "[5f8] 73 00 01 00",
            tcisl.outbound.elementAt(tcisl.outbound.size() - 1).toString(),
            "Message sent to query num parameters");


        // respond from nodes with 7 parameters
        CanReply r = new CanReply();
        r.setHeader(tcisl.getCanid());
        r.setNumDataElements(5);
        r.setElement(0, CbusConstants.CBUS_PARAN); // node parameter response
        r.setElement(1, 0x00); // node hi
        r.setElement(2, 1); // node 1
        r.setElement(3, 0); // param
        r.setElement(4, 7); // val

        n1.getCanListener().reply(r);
        t.sendNextBackgroundFetch();

        // JUnitUtil.waitFor(()->{ return(tcis.outbound.size()>1); }, " outbound 2 didn't arrive");

        assertEquals( 2, tcisl.outbound.size(), "2 Message sent");
        assertEquals( "[5f8] 73 00 01 01",
            tcisl.outbound.elementAt(tcisl.outbound.size() - 1).toString(),
            "Message sent to nodes param1");

        r.setElement(2, 1); // node 1
        r.setElement(3, 1); // param
        r.setElement(4, 165); // val
        n1.getCanListener().reply(r);
        t.sendNextBackgroundFetch();

        // JUnitUtil.waitFor(()->{ return(tcis.outbound.size()>2); }, " outbound 8 didn't arrive");
        assertEquals( 3, tcisl.outbound.size(), "3 Message sent");
        assertEquals( "[5f8] 73 00 01 03",
            tcisl.outbound.elementAt(tcisl.outbound.size() - 1).toString(),
            "Message sent to nodes request param3");


      //  t.setUrgentFetch(1,2,1,3 );


        r.setElement(2, 1); // node 1
        r.setElement(3, 3); // param
        r.setElement(4, 29); // val

        n1.getCanListener().reply(r);
        t.sendNextBackgroundFetch();

        // JUnitUtil.waitFor(()->{ return(tcis.outbound.size()>3); }, " outbound 3 didn't arrive");
        assertEquals( 4, tcisl.outbound.size(), "4 Message sent");
        assertEquals( "[5f8] 73 00 01 06",
            tcisl.outbound.elementAt(tcisl.outbound.size() - 1).toString(),
            "Message sent to nodes request param 6");

        r.setElement(2, 1); // node 1
        r.setElement(3, 6); // param
        r.setElement(4, 3); // 3 x nv's

        n1.getCanListener().reply(r);
        t.sendNextBackgroundFetch();

        // JUnitUtil.waitFor(()->{ return(tcis.outbound.size()>4); }, " outbound 15 didn't arrive");
        assertEquals( 5, tcisl.outbound.size(), "5 Message sent to ");
        assertEquals( "[5f8] 73 00 01 05",
            tcisl.outbound.elementAt(tcisl.outbound.size() - 1).toString(),
            "Message sent to nodes request param 5");

        r.setElement(2, 1); // node 1
        r.setElement(3, 5); // param
        r.setElement(4, 2); // 3 x ev vars per event

        n1.getCanListener().reply(r);
        t.sendNextBackgroundFetch();


        // JUnitUtil.waitFor(()->{ return(tcis.outbound.size()>5); }, " outbound 6 didn't arrive");


        assertEquals( -1, n1.getNodeEventManager().getTotalNodeEvents(), "Total Node Events");


      //  Assert.assertEquals("Outbound String", "", tcis.outbound.toString() );

        assertEquals( 6, tcisl.outbound.size(), "6 Message sent");
        assertEquals( "[5f8] 73 00 01 07",
            tcisl.outbound.elementAt(tcisl.outbound.size() - 1).toString(),
            "Message sent to nodes request param 7");

        r.setElement(2, 1); // node 1
        r.setElement(3, 7); // param
        r.setElement(4, 1); // Major FW Version

        n1.getCanListener().reply(r);
        t.sendNextBackgroundFetch();

        // JUnitUtil.waitFor(()->{ return(tcis.outbound.size()>6); }, " outbound 7 didn't arrive");

        assertEquals( 7, tcisl.outbound.size(), "7 Message sent");

        // Assert.assertEquals("Outbound String", "", tcis.outbound.toString() );
        assertEquals( "[5f8] 73 00 01 02",
            tcisl.outbound.elementAt(tcisl.outbound.size() - 1).toString(),
            "Message sent to nodes request param 2");

        r.setElement(2, 1); // node 1
        r.setElement(3, 2); // param
        r.setElement(4, 2); // Minor FW Version

        n1.getCanListener().reply(r);
        t.sendNextBackgroundFetch();

        JUnitUtil.waitFor(()->{ return(tcisl.outbound.size()>7); }, " outbound 8 didn't arrive");

        assertEquals( 8, tcisl.outbound.size(), "8 Message sent");

        // Assert.assertEquals("Outbound String", "", tcis.outbound.toString() );
        assertEquals( "[5f8] 58 00 01",
            tcisl.outbound.elementAt(tcisl.outbound.size() - 1).toString(),
            "Message sent to nodes request num evs");

        assertEquals( -1, n1.getNodeEventManager().getTotalNodeEvents(),
            "Total Node Events unset");

        r = new CanReply();
        r.setHeader(tcisl.getCanid());
        r.setNumDataElements(5);
        r.setElement(0, CbusConstants.CBUS_NUMEV); // node parameter response
        r.setElement(1, 0x00); // node hi
        r.setElement(2, 1); // node 1
        r.setElement(3, 2); // num ev's

        n1.getCanListener().reply(r);
        t.sendNextBackgroundFetch();

        assertEquals( 2, n1.getNodeEventManager().getTotalNodeEvents(), "Total Node Events 2");

        JUnitUtil.waitFor(()->{ return(tcisl.outbound.size()>8); }, " outbound 9 didn't arrive");
        assertEquals( 9, tcisl.outbound.size(), "9 Message sent");


        n1.dispose();
        n2.dispose();
        n3.dispose();
        tcisl.terminateThreads();
        t.dispose();

    }

    private CanSystemConnectionMemo memo;
    private TrafficControllerScaffold tcis;
    private CbusNodeTableDataModel t;

    @BeforeEach
    public void setUp(@TempDir File folder) throws java.io.IOException {
        JUnitUtil.setUp();
        JUnitUtil.resetInstanceManager();
        JUnitUtil.resetProfileManager(new jmri.profile.NullProfile(folder));
        memo = new CanSystemConnectionMemo();
        tcis = new TrafficControllerScaffold();
        memo.setTrafficController(tcis);
        memo.setProtocol(jmri.jmrix.can.CanConfigurationManager.MERGCBUS);
        memo.get( CbusPreferences.class).setNodeBackgroundFetchDelay(0);
    }

    @AfterEach
    public void tearDown() {
        assertNotNull(t);
        t.dispose();
        assertNotNull(tcis);
        tcis.terminateThreads();
        memo.dispose();
        memo = null;
        tcis = null;
        JUnitUtil.tearDown();

    }

    // private final static Logger log = LoggerFactory.getLogger(CbusNodeTableDataModelTest.class);

}
