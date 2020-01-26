package jmri.jmrix.can.cbus.node;

import jmri.jmrix.can.CanMessage;
import jmri.jmrix.can.CanReply;
import jmri.jmrix.can.CanSystemConnectionMemo;
import jmri.jmrix.can.TrafficControllerScaffold;
import jmri.jmrix.can.TrafficControllerScaffoldLoopback;
import jmri.jmrix.can.cbus.CbusConfigurationManager;
import jmri.jmrix.can.cbus.CbusConstants;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 * @author Steve Young Copyright (C) 2019
 */
public class CbusNodeTableDataModelTest {
    
    private CanSystemConnectionMemo memo;
    private TrafficControllerScaffold tcis;

    @Test
    public void testCTor() {
        
        CbusNodeTableDataModel t = new CbusNodeTableDataModel(
            memo, 3,CbusNodeTableDataModel.MAX_COLUMN);
        Assert.assertNotNull("exists",t);
        t.dispose();
        t = null;
    }

    @Test
    public void testDefaults() {
        
        CbusNodeTableDataModel t = new CbusNodeTableDataModel(
            memo, 3,CbusNodeTableDataModel.MAX_COLUMN);
        Assert.assertTrue("default getAnyNodeInLearnMode -1",t.getAnyNodeInLearnMode()== -1 );
        Assert.assertTrue("default getCsByNum0 null",t.getCsByNum(0)== null );
        Assert.assertTrue("default getListOfNodeNumberNames 0 length list",t.getListOfNodeNumberNames().size() == 0 );
        Assert.assertTrue("default getNextAvailableNodeNumber 777",t.getNextAvailableNodeNumber(777) == 777 );
        Assert.assertTrue("default getNodeByNodeNum 1234",t.getNodeByNodeNum(1234) == null);
        Assert.assertTrue("default getNodeName 1234",t.getNodeName(1234).isEmpty() );
        Assert.assertTrue("default getNodeNameFromCanId 15",t.getNodeNameFromCanId(15).isEmpty() );
        Assert.assertTrue("default getNodeNumberName 1234",t.getNodeNumberName(1234).isEmpty() );
        Assert.assertTrue("default getNodeRowFromNodeNum 1234",t.getNodeRowFromNodeNum(1234) == -1 );
        Assert.assertTrue("default getRowCount 0",t.getRowCount() == 0 );
        
        t.dispose();
        t = null;
    }
    
    @Test
    public void testCanListener() {
        Assert.assertTrue("no listener to start with",0 == tcis.numListeners());
        CbusNodeTableDataModel t = new CbusNodeTableDataModel(
            memo, 3,CbusNodeTableDataModel.MAX_COLUMN);
        Assert.assertTrue("listener attached",1 == tcis.numListeners());
        t.dispose();
        Assert.assertTrue("no listener to finish with",0 == tcis.numListeners());
        t = null;
    }
    
    @Test
    public void testCanMsgReplyCmndstation() {
        
        CbusConfigurationManager cbcfgm = new CbusConfigurationManager( memo );
        Assert.assertTrue("preferences available",cbcfgm.getCbusPreferences() != null );
        
        CbusNodeTableDataModel t = new CbusNodeTableDataModel(
            memo, 3,CbusNodeTableDataModel.MAX_COLUMN);
        t.startup(); // so preferences available in table model

        Assert.assertTrue("default getCsByNum0 null",t.getCsByNum(0)== null );
        
        Assert.assertTrue("default search cs pref",cbcfgm.getCbusPreferences().getStartupSearchForCs()== false );
        Assert.assertTrue("default search node pref",cbcfgm.getCbusPreferences().getStartupSearchForNodes()== false );
        
        cbcfgm.getCbusPreferences().setAddCommandStations(true);
        
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
        Assert.assertTrue("ignores CanMessage",t.getCsByNum(0)== null );
        Assert.assertTrue("ignores CanMessage row",t.getRowCount()== 0 );
        
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
        
        Assert.assertTrue("provides cs 0 CanReply",t.getCsByNum(0) != null );
        Assert.assertTrue("provides cs node 1234 CanReply",t.getNodeByNodeNum(1234) != null );
        Assert.assertTrue("provides cs row",t.getRowCount()== 1 );
        Assert.assertTrue("default getListOfNodeNumberNames 1 length list",t.getListOfNodeNumberNames().size() == 1 );
        
        Assert.assertTrue("getValueAt cs node", (Integer)t.getValueAt(0,CbusNodeTableDataModel.NODE_NUMBER_COLUMN)== 1234 );
        Assert.assertTrue("getValueAt cs user nm",(String)t.getValueAt(0,CbusNodeTableDataModel.NODE_USER_NAME_COLUMN)=="" );
        Assert.assertTrue("getValueAt cs type nm",(String)t.getValueAt(0,CbusNodeTableDataModel.NODE_TYPE_NAME_COLUMN)=="" );
        Assert.assertTrue("getValueAt cs can",(Integer)t.getValueAt(0,CbusNodeTableDataModel.CANID_COLUMN)== tcis.getCanid() );
        Assert.assertTrue("getValueAt cs num",(Integer)t.getValueAt(0,CbusNodeTableDataModel.COMMAND_STAT_NUMBER_COLUMN)== 0 );
        Assert.assertTrue("getValueAt cs ev",(Integer)t.getValueAt(0,CbusNodeTableDataModel.NODE_EVENTS_COLUMN)== -1 );
        Assert.assertTrue("getValueAt cs tot bytes",(Integer)t.getValueAt(0,CbusNodeTableDataModel.NODE_TOTAL_BYTES_COLUMN)== -1 );
        Assert.assertTrue("getValueAt cs byte remain",(float)t.getValueAt(0,CbusNodeTableDataModel.BYTES_REMAINING_COLUMN)== 0.0f );
        Assert.assertTrue("getValueAt cs ev md",(boolean)t.getValueAt(0,CbusNodeTableDataModel.NODE_IN_LEARN_MODE_COLUMN)== false );
        Assert.assertNull("getValueAt null",t.getValueAt(0,999) );
        
        t.setValueAt(7,0,CbusNodeTableDataModel.NODE_EVENTS_COLUMN);
        Assert.assertTrue("setValueAt does nothing",(Integer)t.getValueAt(0,CbusNodeTableDataModel.NODE_EVENTS_COLUMN)== -1 );
        
        t.setValueAt("Alonso",0,CbusNodeTableDataModel.NODE_USER_NAME_COLUMN);
        Assert.assertTrue("setValueAt user nm",(String)t.getValueAt(0,CbusNodeTableDataModel.NODE_USER_NAME_COLUMN)=="Alonso" );
        
        Assert.assertTrue("no node 7 CanReply",t.getNodeByNodeNum(7) == null );
        
        
        cbcfgm.getCbusPreferences().setAddNodes(true);
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
        
        Assert.assertTrue("provides node 7 CanReply",t.getNodeByNodeNum(7) != null );
        Assert.assertTrue("getListOfNodeNumberNames 2 length list",t.getListOfNodeNumberNames().size() == 2 );
        
        t.dispose();
        t = null;
        cbcfgm = null;
    
    }
    
    @Test
    public void testsendSystemResetAndColumns() {
        
        CbusNodeTableDataModel t = new CbusNodeTableDataModel(
            memo, 3,CbusNodeTableDataModel.MAX_COLUMN);
        
        Assert.assertEquals("tcis empty to start", 0 ,tcis.outbound.size() );
        t.sendSystemReset();
        
        Assert.assertEquals("has sent 0", 1 ,tcis.outbound.size() );
        Assert.assertEquals("Message sent is reset", "[5f8] 07",
            tcis.outbound.elementAt(tcis.outbound.size() - 1).toString());
        
        for (int i = 0; i <t.getColumnCount(); i++) {
            Assert.assertFalse("column has name", t.getColumnName(i).isEmpty() );
            Assert.assertTrue("column has a width", CbusNodeTableDataModel.getPreferredWidth(i) > 0 );
        }
        
        Assert.assertTrue("column has NO name", t.getColumnName(999).equals("unknown 999") );
        Assert.assertTrue("column has NO width", CbusNodeTableDataModel.getPreferredWidth(999) > 0 );
        Assert.assertNull("column class null", t.getColumnClass(999));
        
        Assert.assertTrue("cell not editable", 
            t.isCellEditable(0,CbusNodeTableDataModel.NODE_NUMBER_COLUMN) == false );
        
        Assert.assertTrue("cell editable", 
            t.isCellEditable(0,CbusNodeTableDataModel.NODE_USER_NAME_COLUMN) == true );
        
        Assert.assertTrue("column class int", 
            t.getColumnClass(CbusNodeTableDataModel.NODE_NUMBER_COLUMN) == Integer.class );
        
        Assert.assertTrue("column class string", 
            t.getColumnClass(CbusNodeTableDataModel.NODE_USER_NAME_COLUMN) == String.class );
        
        Assert.assertTrue("column class Boolean", 
            t.getColumnClass(CbusNodeTableDataModel.NODE_IN_LEARN_MODE_COLUMN) == Boolean.class );
        
        t.dispose();
        t = null;
    }
    
    @Test
    public void testNextUrgentFetch() {
        
        // using loopback to check the ability to monitor the CAN traffic
        TrafficControllerScaffold tcis = new TrafficControllerScaffoldLoopback();
        memo.setTrafficController(tcis);
        
        CbusNodeTableDataModel t = new CbusNodeTableDataModel(
            memo, 3,CbusNodeTableDataModel.MAX_COLUMN);
            
        CbusNode n1 = t.provideNodeByNodeNum(1);
        
        
        // t.setUrgentFetch(int tabindex, int nodenum, int urgentNodeBefore, int urgentNodeAfter){
        
        // tabindex values see Cbus @NodeConfigToolPane#userViewChanged
        // 0 - Parameters
        // 1 - NV's
        // 2 - EV's
        
        t.sendNextBackgroundFetch();
        
        CbusNode n2 = t.provideNodeByNodeNum(2);
        CbusNode n3 = t.provideNodeByNodeNum(3);
        
        Assert.assertNotNull("exists",n2);
        Assert.assertNotNull("exists",n3);
        
        Assert.assertEquals("1 Messages sent to get node parameters", 1, tcis.outbound.size() );
        
        Assert.assertEquals("Message sent to query num parameters", "[5f8] 73 00 01 00",
            tcis.outbound.elementAt(tcis.outbound.size() - 1).toString());
        

        // respond from nodes with 7 parameters
        CanReply r = new CanReply();
        r.setHeader(tcis.getCanid());
        r.setNumDataElements(5);
        r.setElement(0, CbusConstants.CBUS_PARAN); // node parameter response
        r.setElement(1, 0x00); // node hi
        r.setElement(2, 1); // node 1
        r.setElement(3, 0); // param
        r.setElement(4, 7); // val

        n1.reply(r);
        t.sendNextBackgroundFetch();
        
        // JUnitUtil.waitFor(()->{ return(tcis.outbound.size()>1); }, " outbound 2 didn't arrive");
        
        Assert.assertEquals("2 Message sent to ", 2, tcis.outbound.size() );
        Assert.assertEquals("Message sent to nodes param1", "[5f8] 73 00 01 01",
            tcis.outbound.elementAt(tcis.outbound.size() - 1).toString());

        r.setElement(2, 1); // node 1
        r.setElement(3, 1); // param
        r.setElement(4, 165); // val
        n1.reply(r);
        t.sendNextBackgroundFetch();
        
        // JUnitUtil.waitFor(()->{ return(tcis.outbound.size()>2); }, " outbound 8 didn't arrive");
        Assert.assertEquals("3 Message sent to ", 3, tcis.outbound.size() );
        Assert.assertEquals("Message sent to nodes request param3", "[5f8] 73 00 01 03",
            tcis.outbound.elementAt(tcis.outbound.size() - 1).toString());
            
            
      //  t.setUrgentFetch(1,2,1,3 );
            
            
        r.setElement(2, 1); // node 1
        r.setElement(3, 3); // param
        r.setElement(4, 29); // val

        n1.reply(r);
        t.sendNextBackgroundFetch();
        
        // JUnitUtil.waitFor(()->{ return(tcis.outbound.size()>3); }, " outbound 3 didn't arrive");
        Assert.assertEquals("4 Message sent to ", 4, tcis.outbound.size() );
        Assert.assertEquals("Message sent to nodes request param 6", "[5f8] 73 00 01 06",
            tcis.outbound.elementAt(tcis.outbound.size() - 1).toString());
        
        r.setElement(2, 1); // node 1
        r.setElement(3, 6); // param
        r.setElement(4, 3); // 3 x nv's

        n1.reply(r);
        t.sendNextBackgroundFetch();
        
        // JUnitUtil.waitFor(()->{ return(tcis.outbound.size()>4); }, " outbound 15 didn't arrive");
        Assert.assertEquals("5 Message sent to ", 5, tcis.outbound.size() );
        Assert.assertEquals("Message sent to nodes request param 5", "[5f8] 73 00 01 05",
            tcis.outbound.elementAt(tcis.outbound.size() - 1).toString());
        
        r.setElement(2, 1); // node 1
        r.setElement(3, 5); // param
        r.setElement(4, 2); // 3 x ev vars per event

        n1.reply(r);
        t.sendNextBackgroundFetch();
        
        
        // JUnitUtil.waitFor(()->{ return(tcis.outbound.size()>5); }, " outbound 6 didn't arrive");
        
        
        
        
        
        Assert.assertEquals("Total Node Events ", -1, n1.getTotalNodeEvents() );
        
        
        
        
        
        
      //  Assert.assertEquals("Outbound String", "", tcis.outbound.toString() );
        
        Assert.assertEquals("6 Message sent to ", 6, tcis.outbound.size() );
        Assert.assertEquals("Message sent to nodes request param 7", "[5f8] 73 00 01 07",
            tcis.outbound.elementAt(tcis.outbound.size() - 1).toString());
        
        r.setElement(2, 1); // node 1
        r.setElement(3, 7); // param
        r.setElement(4, 1); // Major FW Version

        n1.reply(r);
        t.sendNextBackgroundFetch();
        
        // JUnitUtil.waitFor(()->{ return(tcis.outbound.size()>6); }, " outbound 7 didn't arrive");
        
        Assert.assertEquals("7 Message sent to ", 7, tcis.outbound.size() );
        
        // Assert.assertEquals("Outbound String", "", tcis.outbound.toString() );
        Assert.assertEquals("Message sent to nodes request param 2", "[5f8] 73 00 01 02",
            tcis.outbound.elementAt(tcis.outbound.size() - 1).toString());
       
        r.setElement(2, 1); // node 1
        r.setElement(3, 2); // param
        r.setElement(4, 2); // Minor FW Version

        n1.reply(r);
        t.sendNextBackgroundFetch();
       
        JUnitUtil.waitFor(()->{ return(tcis.outbound.size()>7); }, " outbound 8 didn't arrive");
        
        Assert.assertEquals("8 Message sent to ", 8, tcis.outbound.size() );
        
        // Assert.assertEquals("Outbound String", "", tcis.outbound.toString() );
        Assert.assertEquals("Message sent to nodes request num evs", "[5f8] 58 00 01",
            tcis.outbound.elementAt(tcis.outbound.size() - 1).toString());
        
        Assert.assertEquals("Total Node Events unset", -1, n1.getTotalNodeEvents() );
        
        r = new CanReply();
        r.setHeader(tcis.getCanid());
        r.setNumDataElements(5);
        r.setElement(0, CbusConstants.CBUS_NUMEV); // node parameter response
        r.setElement(1, 0x00); // node hi
        r.setElement(2, 1); // node 1
        r.setElement(3, 2); // num ev's
        
        n1.reply(r);
        t.sendNextBackgroundFetch();
        
        Assert.assertEquals("Total Node Events 2", 2, n1.getTotalNodeEvents() );
        
        JUnitUtil.waitFor(()->{ return(tcis.outbound.size()>8); }, " outbound 9 didn't arrive");
        Assert.assertEquals("9 Message sent to ", 9, tcis.outbound.size() );
        
        
        n1.dispose();
        n2.dispose();
        n3.dispose();
        n1 = null;
        n2 = null;
        n3 = null;
        
        t.dispose();
        t = null;
    
    }
    
    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    // The minimal setup for log4J
    @Before
    public void setUp() throws java.io.IOException {
        JUnitUtil.setUp();
        JUnitUtil.resetInstanceManager();
        JUnitUtil.resetProfileManager(new jmri.profile.NullProfile(folder.newFolder(jmri.profile.Profile.PROFILE)));
        memo = new CanSystemConnectionMemo();
        tcis = new TrafficControllerScaffold();
        memo.setTrafficController(tcis);
    }

    @After
    public void tearDown() {
        
        memo = null;
        tcis = null;
        JUnitUtil.clearShutDownManager(); // put in place because AbstractMRTrafficController implementing subclass was not terminated properly
        JUnitUtil.tearDown();

    }

    // private final static Logger log = LoggerFactory.getLogger(CbusNodeTableDataModelTest.class);

}
