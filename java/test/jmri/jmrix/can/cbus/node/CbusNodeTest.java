package jmri.jmrix.can.cbus.node;

import java.io.File;

import jmri.jmrix.can.CanMessage;
import jmri.jmrix.can.CanReply;
import jmri.jmrix.can.CanSystemConnectionMemo;
import jmri.jmrix.can.TrafficControllerScaffold;
import jmri.jmrix.can.cbus.CbusConstants;
import jmri.util.JUnitUtil;
import jmri.util.JUnitAppender;

import org.junit.Assert;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;

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
        Assert.assertTrue("default getTotalNodeEvents ",t.getNodeEventManager().getTotalNodeEvents()== -1 );
        Assert.assertTrue("default getLoadedNodeEvents",t.getNodeEventManager().getLoadedNodeEvents()== -1 );
        Assert.assertTrue("default parameter 0",t.getNodeParamManager().getParameter(0)== -1 );
        Assert.assertTrue("default getNV 0",t.getNodeNvManager().getNV(0)== -1 );
        Assert.assertTrue("default getTotalNVs 0",t.getNodeNvManager().getTotalNVs()== 0 );
        Assert.assertTrue("default getNodeCanId ",t.getNodeCanId()== -1 );
        Assert.assertTrue("default getNodeTypeName ",t.getNodeStats().getNodeTypeName().isEmpty() );
        Assert.assertTrue("default getNodeInFLiMMode",t.getNodeInFLiMMode() );
        Assert.assertFalse("default getNodeInSetupMode",t.getNodeInSetupMode() );
        Assert.assertEquals("default getNodeNumberName","256",t.getNodeStats().getNodeNumberName() );
        Assert.assertTrue("default getsendsWRACKonNVSET",t.getsendsWRACKonNVSET() );
        Assert.assertFalse("default getsendsWRACKonNVSET",t.getnvWriteInLearn() );
        Assert.assertTrue("default totalNodeBytes ",-1 == t.getNodeStats().totalNodeBytes() );
        Assert.assertTrue("default totalRemainingNodeBytes",-1 == t.getNodeStats().totalRemainingNodeBytes() );
        Assert.assertEquals("default toString ","256",t.toString() );
        Assert.assertTrue("default getNodeFlags ",t.getNodeFlags() == -1 );
        Assert.assertTrue("default getOutstandingEvVars",t.getNodeEventManager().getOutstandingEvVars() == -1);
        Assert.assertFalse("default hasActiveTimers",t.getNodeTimerManager().hasActiveTimers());
        Assert.assertFalse("default isEventIndexValid",t.getNodeEventManager().isEventIndexValid());
        Assert.assertNull("No First Backup Timestamp",t.getNodeBackupManager().getFirstBackupTime());
        Assert.assertNull("No Last Backup Timestamp",t.getNodeBackupManager().getLastBackupTime());
        Assert.assertEquals("0 Backups",0,t.getNodeBackupManager().getNumCompleteBackups());
        Assert.assertEquals("Backup Outstanding",
            t.getNodeBackupManager().getSessionBackupStatus(),CbusNodeConstants.BackupType.OUTSTANDING);
        
        t.dispose();
    }
    
    @Test
    public void testInOutLearnModeExtendedRtr() {
        CbusNode t = new CbusNode(memo,1234);
        
        Assert.assertEquals("default getNodeInLearnMode ",false,t.getNodeInLearnMode() );
        
        // frame to set node into learn
        CanReply r = new CanReply();
        r.setHeader(tcis.getCanid());
        r.setNumDataElements(3);
        r.setElement(0, CbusConstants.CBUS_NNLRN); 
        r.setElement(1, 0x04);
        r.setElement(2, 0xd2);
        t.getCanListener().reply(r);
        Assert.assertEquals("reply in learn mode ",true,t.getNodeInLearnMode() );
        
        r = new CanReply();
        r.setHeader(tcis.getCanid());
        r.setNumDataElements(3);
        r.setElement(0, CbusConstants.CBUS_NNULN); 
        r.setElement(1, 0x04);
        r.setElement(2, 0xd2);
        t.getCanListener().reply(r);
        Assert.assertEquals("reply exit learn mode ",false,t.getNodeInLearnMode() );
        
        // frame to set node into learn
        CanMessage m = new CanMessage( tcis.getCanid() );
        m.setNumDataElements(3);
        m.setElement(0, CbusConstants.CBUS_NNLRN); 
        m.setElement(1, 0x04);
        m.setElement(2, 0xd2);
        t.getCanListener().message(m);
        Assert.assertEquals("message enter learn mode ",true,t.getNodeInLearnMode() );
        
        m = new CanMessage( tcis.getCanid() );
        m.setNumDataElements(3);
        m.setElement(0, CbusConstants.CBUS_NNULN); 
        m.setElement(1, 0x04);
        m.setElement(2, 0xd2);
        t.getCanListener().message(m);
        Assert.assertEquals("message exit learn mode ",false,t.getNodeInLearnMode() );
        
        // any message which does nothing to node does not crash it
        m = new CanMessage( tcis.getCanid() );
        m.setNumDataElements(1);
        m.setElement(0, CbusConstants.CBUS_RTOF); // request track off
        t.getCanListener().message(m);
        Assert.assertEquals("not in learn mode ",false,t.getNodeInLearnMode() );
        
        
        m = new CanMessage( tcis.getCanid() );
        m.setElement(0, CbusConstants.CBUS_NNLRN); // enter learn mode
        m.setElement(1, 0x04);
        m.setElement(2, 0xd2);
        
        m.setExtended(true);
        
        t.getCanListener().message(m);
        Assert.assertEquals("no change ext",false,t.getNodeInLearnMode() );
        
        m.setExtended(false);
        m.setRtr(true);
        t.getCanListener().message(m);
        Assert.assertEquals("no change rtr",false,t.getNodeInLearnMode() );
        
        m.setRtr(false);
        t.getCanListener().message(m);
        Assert.assertEquals("message enter learn mode ",true,t.getNodeInLearnMode() );
        
        
        r = new CanReply();
        r.setHeader(tcis.getCanid());
        r.setNumDataElements(3);
        r.setElement(0, CbusConstants.CBUS_NNULN); 
        r.setElement(1, 0x04);
        r.setElement(2, 0xd2);
        
        r.setExtended(true);
        t.getCanListener().reply(r);
        Assert.assertEquals("no change ext",true,t.getNodeInLearnMode() );
        
        r.setExtended(false);
        r.setRtr(true);
        t.getCanListener().reply(r);
        Assert.assertEquals("no change rtr",true,t.getNodeInLearnMode() );
        
        t.dispose();

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
        Assert.assertEquals("username number","123 shirley",tb.getNodeStats().getNodeNumberName() );
        
        t.dispose();
        tb.dispose();
    }

    @Test
    public void testStartGetParams() {
        
        Assert.assertEquals("tcis empty at start", 0 ,tcis.outbound.size() );
        CbusNode t = new CbusNode(memo,12345);
        
        Assert.assertTrue("default parameter 0",t.getNodeParamManager().getParameter(0)== -1 );
        // only 1 parameter awaiting knowledge of until total confirmed
        Assert.assertEquals("default outstanding parameters",8,t.getNodeParamManager().getOutstandingParams() );
        
        Assert.assertEquals("tcis empty after creating new node", 0 ,tcis.outbound.size() );
        t.getNodeParamManager().sendRequestNextParam();
        Assert.assertEquals("Node has requested parameter 0", 1 ,tcis.outbound.size() );
        t.getNodeParamManager().sendRequestNextParam();
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
        t.getCanListener().reply(r);
        Assert.assertTrue("parameter 0 value 7",t.getNodeParamManager().getParameter(0)== 7 );
        Assert.assertTrue("default outstanding parameters 7",t.getNodeParamManager().getOutstandingParams()== 7 );
        t.getNodeParamManager().sendRequestNextParam();
        // JUnitUtil.waitFor(()->{ return(tcis.outbound.size()>1); }, " outbound 1 didn't arrive");
        
        
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
        t.getCanListener().reply(r);
        Assert.assertTrue("parameter 1 value 165",t.getNodeParamManager().getParameter(1)== 165 );
        t.getNodeParamManager().sendRequestNextParam();
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
        t.getCanListener().reply(r);

        // now we know params 1 and 3, try nodetype lookup
        Assert.assertEquals("CbusNode identified as a CANPAN", "CANPAN" ,t.getNodeStats().getNodeTypeName() );
        t.getNodeParamManager().sendRequestNextParam();
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
        t.getCanListener().reply(r);

        // now we know number of NV's
        Assert.assertTrue("getTotalNVs 3",t.getNodeNvManager().getTotalNVs()== 3 );
        Assert.assertTrue("get oustanding NVs 3",t.getNodeNvManager().getOutstandingNvCount()== 3 );
        Assert.assertTrue("get NVs 0 3",t.getNodeNvManager().getNV(0)== 3 );
        Assert.assertTrue("get NVs 1 -1",t.getNodeNvManager().getNV(1)== -1 );
        Assert.assertTrue("get NVs 3 -1",t.getNodeNvManager().getNV(3)== -1 );
        
        t.getNodeParamManager().sendRequestNextParam();
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
        t.getCanListener().reply(r);
        
        Assert.assertTrue(" outstanding parameters 3",t.getNodeParamManager().getOutstandingParams()== 3 );
        
        t.getNodeParamManager().sendRequestNextParam();
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
        t.getCanListener().reply(r);
        
        t.getNodeParamManager().sendRequestNextParam();
        
        Assert.assertEquals("CbusNode has requested parameter 2 firmware minor", 7 ,tcis.outbound.size() );
        Assert.assertEquals("Message sent is parameter 2 request", "[5f8] 73 30 39 02",
            tcis.outbound.elementAt(tcis.outbound.size() - 1).toString());
        Assert.assertTrue("default getsendsWRACKonNVSET ",t.getsendsWRACKonNVSET() );
        Assert.assertFalse("default getsendsWRACKonNVSET ",t.getnvWriteInLearn() );

        r = new CanReply();
        r.setHeader(tcis.getCanid());
        r.setNumDataElements(5);
        r.setElement(0, CbusConstants.CBUS_PARAN); 
        r.setElement(1, 0x30);
        r.setElement(2, 0x39);
        r.setElement(3, 0x02); // parameter 2
        r.setElement(4, 0x01); // firmware pt2 1
        t.getCanListener().reply(r);
        
        Assert.assertTrue(" outstanding parameter 1",t.getNodeParamManager().getOutstandingParams()== 1 );
        
        Assert.assertEquals("getParameters ", "[7, 165, 1, 29, -1, 7, 3, 2]",
            java.util.Arrays.toString(t.getNodeParamManager().getParameters() ) );

        t.getNodeParamManager().sendRequestNextParam();

        // with this we should expect CbusNodeConstants.setTraits to have been called
        Assert.assertFalse("setTraits getsendsWRACKonNVSET ",t.getsendsWRACKonNVSET() );
        
        Assert.assertEquals("CbusNode has requested number of events", 8 ,tcis.outbound.size() );
        Assert.assertEquals("Message sent numev request", "[5f8] 58 30 39",
            tcis.outbound.elementAt(tcis.outbound.size() - 1).toString());
        
        t.dispose();
        
    }

    @Test
    public void testAddEvents() {
        
        CbusNode t = new CbusNode(memo,12345);
        
        Assert.assertTrue("default getTotalNodeEvents ",t.getNodeEventManager().getTotalNodeEvents()== -1 );
        Assert.assertFalse("default active timers false, ie nothing requested ",t.getNodeTimerManager().hasActiveTimers() );
        
        
        // set node to 4 ev vars per event, para 5
        t.getNodeParamManager().setParameters(new int[]{8,1,2,3,4,4,6,7,8});
        
        CbusNodeEvent ev = new CbusNodeEvent(memo,0,7,12345,77,4);  // nn, en, thisnode, index, maxevvar
        
        t.getNodeEventManager().addNewEvent(ev);
        // index on the event is set to -1, ie unknown when an event is added to the node
        Assert.assertEquals("index in array -1",-1,ev.getIndex() );
        
        Assert.assertTrue("after new ev getTotalNodeEvents 1",t.getNodeEventManager().getTotalNodeEvents()== 1 );
        Assert.assertTrue("after new ev getOutstandingEvVars 1",t.getNodeEventManager().getOutstandingEvVars()== 4 ); 
        
        CbusNodeEvent evb = new CbusNodeEvent(memo,0,8,12345,2,4);
        t.getNodeEventManager().addNewEvent(evb);
        Assert.assertTrue("after new ev getTotalNodeEvents 2",t.getNodeEventManager().getTotalNodeEvents()== 2 );
        Assert.assertTrue("after new ev getLoadedNodeEvents 2",t.getNodeEventManager().getLoadedNodeEvents()== 2 );
        Assert.assertTrue("getOutstandingIndexNodeEvents 0",t.getNodeEventManager().getOutstandingIndexNodeEvents()== 0 );
        
        Assert.assertTrue("node event fetch node 0 ev 7",t.getNodeEventManager().getNodeEvent(0,7) == ev );
        Assert.assertTrue("node event fetch null node 321 ev 645",t.getNodeEventManager().getNodeEvent(321,654) == null );
        Assert.assertTrue("node event provide node 0 ev87",t.getNodeEventManager().provideNodeEvent(0,8) == evb );
        Assert.assertTrue("after provide 321 645 getTotalNodeEvents 3",t.getNodeEventManager().getTotalNodeEvents()== 2 );
        t.getNodeEventManager().provideNodeEvent(321,654);
        Assert.assertTrue("after provide 321 645 getTotalNodeEvents 3",t.getNodeEventManager().getTotalNodeEvents()== 3 );
        
        java.util.ArrayList _tArr = t.getNodeEventManager().getEventArray();
        if (_tArr!=null){
            Assert.assertEquals("event array size 3",3,_tArr.size() );
        } else {
            Assert.fail("Array should not be null");
        }
        
        ev.setIndex(77);
        evb.setIndex(88);
        
        Assert.assertEquals("index in array 1",1,t.getNodeEventManager().getEventRowFromIndex(88) );
        Assert.assertEquals("index in array 0",0,t.getNodeEventManager().getEventRowFromIndex(77) );
        Assert.assertEquals("index 999 not in array",-1,t.getNodeEventManager().getEventRowFromIndex(999) );
        
        t.getNodeEventManager().addNewEvent(new CbusNodeEvent(memo,123,-1,12345,3,4));
        t.getNodeEventManager().addNewEvent(new CbusNodeEvent(memo,-1,123,12345,4,4));
        
        Assert.assertTrue("after remove getTotalNodeEvents 2",t.getNodeEventManager().getTotalNodeEvents()== 5 );
        Assert.assertTrue("after new ev getLoadedNodeEvents 3",t.getNodeEventManager().getLoadedNodeEvents()== 3 );
        
        t.getNodeEventManager().removeEvent(0,8);
        Assert.assertTrue("node event remove node 0 ev 8",t.getNodeEventManager().getNodeEvent(0,8) == null );
        Assert.assertTrue("node event not removed others",t.getNodeEventManager().getNodeEvent(321,654) != null );
        Assert.assertTrue("after remove getTotalNodeEvents 4",t.getNodeEventManager().getTotalNodeEvents()==4 );
        Assert.assertNull("event fetched by index",t.getNodeEventManager().getNodeEventByIndex(999) );
        
        t.setNodeInLearnMode(true);
        
        CanReply r = new CanReply();
        r.setHeader(tcis.getCanid());
        r.setNumDataElements(3);
        r.setElement(0, CbusConstants.CBUS_NNCLR); 
        r.setElement(1, 0x30);
        r.setElement(2, 0x00);
        t.getCanListener().reply(r);
        
        Assert.assertTrue("after remove getTotalNodeEvents 4 wrong node number",t.getNodeEventManager().getTotalNodeEvents()==4 );
        r.setElement(2, 0x39);
        t.getCanListener().reply(r);
        Assert.assertEquals("after CBUS_NNCLR getTotalNodeEvents 0",0,t.getNodeEventManager().getTotalNodeEvents() );
        
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
        
        Assert.assertTrue("getTotalNodeEvents 1",t.getNodeEventManager().getTotalNodeEvents()==1 );
        Assert.assertTrue("getLoadedNodeEvents 1",t.getNodeEventManager().getLoadedNodeEvents()== 1 );
        Assert.assertTrue("getOutstandingEvVars 3",t.getNodeEventManager().getOutstandingEvVars() == 3);
        Assert.assertEquals("Node has not sent a message", 0 ,tcis.outbound.size() );
        
        r = new CanReply();
        r.setHeader(tcis.getCanid());
        r.setNumDataElements(5);
        r.setElement(0, CbusConstants.CBUS_EVULN); 
        r.setElement(1, 0x30); // nn 12344
        r.setElement(2, 0x38); // nn 12344
        r.setElement(3, 0x00); // en 6
        r.setElement(4, 0x06); // en 6
        t.getCanListener().reply(r);
        Assert.assertTrue("getTotalNodeEvents 1 wrong event",t.getNodeEventManager().getTotalNodeEvents()==1 );
        
        r.setElement(4, 0x07); // en 7
        t.getCanListener().reply(r);
        Assert.assertTrue("getTotalNodeEvents 0",t.getNodeEventManager().getTotalNodeEvents()==0 );
        
        t.dispose();
        
    }
    
    
    @Test
    public void testFetchEventsFromCanWithNodeTable() {
        
        // needs table model adding to check for any other nodes in learn mode
        CbusNodeTableDataModel tModel = new CbusNodeTableDataModel(
            memo, 3,CbusNodeTableDataModel.MAX_COLUMN);
        
        CbusNode t = tModel.provideNodeByNodeNum(12345);
        // set node to 3 ev vars per event( param 5) , 0 NV's ( param 6)
        t.getNodeParamManager().setParameters(new int[]{8,1,2,3,4,3,0,7,8});
        
        CanReply r = new CanReply();
        r.setHeader(tcis.getCanid());
        r.setNumDataElements(4);
        r.setElement(0, CbusConstants.CBUS_NUMEV); 
        r.setElement(1, 0x30); // nn 12345
        r.setElement(2, 0x39); // nn 12345
        r.setElement(3, 0x02); // node has 2 events
        t.getCanListener().reply(r);
        Assert.assertTrue("CBUS_NUMEV 2",t.getNodeEventManager().getTotalNodeEvents()==2 );
        Assert.assertEquals(0.5 ,t.getNodeStats().floatPercentageRemaining(), 0.0001f );
        
        t.getNodeEventManager().sendNextEvVarToFetch();
        Assert.assertEquals("Node has sent a message", 1 ,tcis.outbound.size() );
        Assert.assertEquals("Node sends NERD request", "[5f8] 57 30 39",
            tcis.outbound.elementAt(tcis.outbound.size() - 1).toString());
        Assert.assertTrue("active timer true for NERD / ENRSP responses ",t.getNodeTimerManager().hasActiveTimers() );
        
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
        Assert.assertTrue("CBUS_ENRSP 2",t.getNodeEventManager().getTotalNodeEvents()==2 );
        Assert.assertEquals(0.5625 ,t.getNodeStats().floatPercentageRemaining(), 0.0001f );
        
        Assert.assertTrue("getLoadedNodeEvents 1",t.getNodeEventManager().getLoadedNodeEvents()== 1 );
        Assert.assertTrue("getOutstandingEvVars 3",t.getNodeEventManager().getOutstandingEvVars() == 6);
        Assert.assertFalse("isEventIndexValid false",t.getNodeEventManager().isEventIndexValid());
        
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
        
        Assert.assertTrue("CBUS_ENRSP 2 again",t.getNodeEventManager().getTotalNodeEvents()==2 );
        Assert.assertEquals(0.625 ,t.getNodeStats().floatPercentageRemaining(), 0.0001f );
        Assert.assertTrue("isEventIndexValid true",t.getNodeEventManager().isEventIndexValid());
        Assert.assertFalse("No active timer after NERD / ENRSP responses ",t.getNodeTimerManager().hasActiveTimers() );
        Assert.assertTrue("getLoadedNodeEvents 2",t.getNodeEventManager().getLoadedNodeEvents()== 2 );
        
        tModel.sendNextBackgroundFetch();
        Assert.assertEquals("Node has sent a message via model", 2 ,tcis.outbound.size() );
        Assert.assertEquals("Node sends ev var request", "[5f8] 9C 30 39 01 01",
            tcis.outbound.elementAt(tcis.outbound.size() - 1).toString());
        Assert.assertTrue("active timer true awaiting response ",t.getNodeTimerManager().hasActiveTimers() );
        
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
        
        Assert.assertEquals(0.6875 ,t.getNodeStats().floatPercentageRemaining(), 0.0001f );
        Assert.assertFalse("No active timer after NEVAL response",t.getNodeTimerManager().hasActiveTimers() );
        
        tModel.sendNextBackgroundFetch();
        Assert.assertEquals("Node has sent a message via model", 3 ,tcis.outbound.size() );
        Assert.assertEquals("Node sends ev var request", "[5f8] 9C 30 39 01 02",
            tcis.outbound.elementAt(tcis.outbound.size() - 1).toString());
        
        r.setElement(4, 0x02); // ev var index
        r.setElement(5, 0xff); // ev var value
        t.getCanListener().reply(r);
        Assert.assertEquals(0.75 ,t.getNodeStats().floatPercentageRemaining(), 0.0001f );
        
        tModel.sendNextBackgroundFetch();
        Assert.assertEquals("Node has sent a message via model", 4 ,tcis.outbound.size() );
        Assert.assertEquals("Node sends ev var request", "[5f8] 9C 30 39 01 03",
            tcis.outbound.elementAt(tcis.outbound.size() - 1).toString());
        
        
        r.setElement(4, 0x03); // ev var index
        r.setElement(5, 0xd1); // ev var value
        t.getCanListener().reply(r);
        Assert.assertEquals(0.8125 ,t.getNodeStats().floatPercentageRemaining(), 0.0001f );
        
        tModel.sendNextBackgroundFetch();
        Assert.assertEquals("Node has sent a message via model", 5 ,tcis.outbound.size() );
        Assert.assertEquals("Node sends ev var request index 3", "[5f8] 9C 30 39 03 01",
            tcis.outbound.elementAt(tcis.outbound.size() - 1).toString());
        
        r.setElement(3, 0x03); // ev index
        r.setElement(4, 0x01); // ev var index
        r.setElement(5, 0xd1); // ev var value
        t.getCanListener().reply(r);
        Assert.assertEquals(0.875 ,t.getNodeStats().floatPercentageRemaining(), 0.0001f );
        
        Assert.assertEquals("0 Backups in middle of fetch",0,t.getNodeBackupManager().getNumCompleteBackups() );
        
        tModel.sendNextBackgroundFetch();
        Assert.assertEquals("Node has sent a message via model", 6 ,tcis.outbound.size() );
        r.setElement(4, 0x02); // ev var index
        r.setElement(5, 0xd1); // ev var value
        t.getCanListener().reply(r);
        
        Assert.assertEquals(0.9375 ,t.getNodeStats().floatPercentageRemaining(), 0.0001f );
        
        tModel.sendNextBackgroundFetch();
        Assert.assertEquals("Node has sent a message via model", 7 ,tcis.outbound.size() );
        r.setElement(4, 0x03); // ev var index
        r.setElement(5, 0xd1); // ev var value
        t.getCanListener().reply(r);
        
        Assert.assertEquals(1.0 ,t.getNodeStats().floatPercentageRemaining(), 0.0001f );
        
        tModel.sendNextBackgroundFetch();
        
        Assert.assertEquals("Node has NOT sent a message via model", 7 ,tcis.outbound.size() );
        Assert.assertFalse("No active timer after event fetch complete",t.getNodeTimerManager().hasActiveTimers() );
        
        // check if node has loaded an xml file
        Assert.assertNotNull("First Backup Timestamp exists",t.getNodeBackupManager().getFirstBackupTime());
        Assert.assertNotNull("Last Backup Timestamp exists",t.getNodeBackupManager().getLastBackupTime());
        
        Assert.assertEquals("Backup Complete",
            t.getNodeBackupManager().getSessionBackupStatus(),CbusNodeConstants.BackupType.COMPLETE);
        Assert.assertTrue(t.getNodeBackupManager().removeNode(false));
        
        tModel.dispose();
        t.dispose();

    }
    
    @Test
    public void testAddNodeVariables() {
        
        CbusNode t = new CbusNode(memo,12345);
        
        Assert.assertTrue("default getNvArray ",t.getNodeNvManager().getNvArray()== null );
        Assert.assertTrue("default getNv 0 ",t.getNodeNvManager().getNV(0)== -1 );
        Assert.assertTrue("default getNv 3 ",t.getNodeNvManager().getNV(3)== -1 );
        Assert.assertTrue("default getOutstandingNvCount ",t.getNodeNvManager().getOutstandingNvCount()== -1 );
        
        // set node to 3 node vars , param6
        t.getNodeParamManager().setParameters(new int[]{7,1,2,3,4,5,3,7});
        
        Assert.assertTrue("3 node vars getNvArray ",t.getNodeNvManager().getNvArray() != null );
        Assert.assertTrue("3 node vars getNv 0 ",t.getNodeNvManager().getNV(0)== 3 );
        Assert.assertTrue("3 node vars getNv 3 ",t.getNodeNvManager().getNV(3)== -1 );
        Assert.assertTrue("3 node vars getOutstandingNvCount ",t.getNodeNvManager().getOutstandingNvCount()== 3 );
        
        t.getNodeNvManager().setNV(1,1);
        t.getNodeNvManager().setNV(2,2);
        t.getNodeNvManager().setNV(3,3);
        Assert.assertTrue("node vars getOutstandingNvCount ",t.getNodeNvManager().getOutstandingNvCount()== 0 );
        
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
        
        Assert.assertEquals("165 10 getNodeTypeString ",
            "MERG Command Station CANCMD Firmware ver: 4Y Node 12345",t.getNodeParamManager().getNodeTypeString() );
        
        t.getNodeParamManager().setParameters(new int[]{20,70,90,4,4,5,3,4,8,9,10,11,12,13,14,15,16,17,18,19,77});
        
        Assert.assertEquals("70 4 getNodeTypeString ",
            "ROCRAIL 8 channel RFID reader. CANGC4 Firmware ver: 4Z Beta 77 Node 12345",t.getNodeParamManager().getNodeTypeString() );
            
        t.getNodeParamManager().setParameters(new int[]{8,0,89,0,4,5,3,4,8});
        Assert.assertEquals("0 0 getNodeTypeName ","",t.getNodeStats().getNodeTypeName() );
        t.setNodeNameFromName("Alonso");
        Assert.assertEquals("0 0 Alonso getNodeTypeName ","Alonso",t.getNodeStats().getNodeTypeName() );
        
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
