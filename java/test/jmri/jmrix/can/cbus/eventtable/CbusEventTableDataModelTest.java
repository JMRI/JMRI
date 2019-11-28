package jmri.jmrix.can.cbus.eventtable;

import jmri.jmrix.can.CanMessage;
import jmri.jmrix.can.CanReply;
import jmri.jmrix.can.CanSystemConnectionMemo;
import jmri.jmrix.can.TrafficControllerScaffold;
import jmri.jmrix.can.TrafficControllerScaffoldLoopback;
import jmri.jmrix.can.cbus.CbusConstants;
import jmri.jmrix.can.cbus.CbusMessage;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

// import org.slf4j.Logger;
// import org.slf4j.LoggerFactory;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 * @author Steve Young Copyright (C) 2019
 */
public class CbusEventTableDataModelTest {

    private CanSystemConnectionMemo memo;

    @Test
    public void testCTor() {
        CbusEventTableDataModel t = new CbusEventTableDataModel(memo,4,CbusEventTableDataModel.MAX_COLUMN);
        Assert.assertNotNull("exists",t);
    }

    @Test
    public void testCanReply() {
        
        TrafficControllerScaffold tcis = new TrafficControllerScaffold();
        memo.setTrafficController(tcis);
        
        CbusEventTableDataModel t = new CbusEventTableDataModel( memo,4,CbusEventTableDataModel.MAX_COLUMN);
        Assert.assertNotNull("exists",t);
        
        Assert.assertTrue(t.getRowCount()==0);
        t.reply(new CanReply( new int[]{0x05},0x12 ));
        Assert.assertTrue(t.getRowCount()==0);
        
        CanReply m = new CanReply();
        m.setHeader(tcis.getCanid());
        CbusMessage.setPri(m, CbusConstants.DEFAULT_DYNAMIC_PRIORITY * 4 + CbusConstants.DEFAULT_MINOR_PRIORITY);
        m.setNumDataElements(5);
        m.setElement(0, 0x98); 
        m.setElement(1, 0x00);
        m.setElement(2, 0x00);
        m.setElement(3, 0xde);
        m.setElement(4, 0x00);
        t.reply(m);
        Assert.assertTrue(t.getRowCount()==1);
        Assert.assertEquals("reply on", CbusTableEvent.EvState.ON,t.getValueAt(0,CbusEventTableDataModel.STATE_COLUMN));
        
        m.setElement(0, 0x99); 
        t.reply(m);
        Assert.assertTrue(t.getRowCount()==1);
        Assert.assertEquals("reply off", CbusTableEvent.EvState.OFF,t.getValueAt(0,CbusEventTableDataModel.STATE_COLUMN));

        Assert.assertEquals("session in 2", 2 ,t.getValueAt(0,CbusEventTableDataModel.SESSION_IN_COLUMN));
        Assert.assertEquals("session out 0", 0 ,t.getValueAt(0,CbusEventTableDataModel.SESSION_OUT_COLUMN));
        
        Assert.assertEquals("session total 2", 2 ,t.getValueAt(0,CbusEventTableDataModel.SESSION_TOTAL_COLUMN));
        t.reply(m);
        Assert.assertEquals("session total 3", 3 ,t.getValueAt(0,CbusEventTableDataModel.SESSION_TOTAL_COLUMN));

        m.setElement(0, 0x9A); 
        t.reply(m);
        Assert.assertTrue(t.getRowCount()==1);
        Assert.assertEquals("reply request", CbusTableEvent.EvState.REQUEST,t.getValueAt(0,CbusEventTableDataModel.STATE_COLUMN));
        
        m.setElement(0, 0x90); 
        m.setElement(1, 0x01);
        t.reply(m);
        Assert.assertTrue(t.getRowCount()==2);        

        m.setElement(1, 0x02);
        t.reply(m);
        Assert.assertTrue(t.getRowCount()==3); 
        
        Assert.assertEquals("can id reply", tcis.getCanid(),t.getValueAt(0,CbusEventTableDataModel.CANID_COLUMN));
        
    }

    @Test
    public void testCanMessage() {
        
        TrafficControllerScaffold tcis = new TrafficControllerScaffold();
        memo.setTrafficController(tcis);
        
        Assert.assertEquals("no listener to start with",0,tcis.numListeners());
        
        CbusEventTableDataModel t = new CbusEventTableDataModel( memo,4,CbusEventTableDataModel.MAX_COLUMN);
        Assert.assertEquals("listener attached",1,tcis.numListeners());
        
        Assert.assertEquals("rowcount 0",0,t.getRowCount());
        t.message(new CanMessage( new int[]{0x05},0x12 ));
        Assert.assertTrue(t.getRowCount()==0);
        
        CanMessage m = new CanMessage(tcis.getCanid());
        CbusMessage.setPri(m, CbusConstants.DEFAULT_DYNAMIC_PRIORITY * 4 + CbusConstants.DEFAULT_MINOR_PRIORITY);
        m.setNumDataElements(5);
        m.setElement(0, 0x98); 
        m.setElement(1, 0x00);
        m.setElement(2, 0x00);
        m.setElement(3, 0xde);
        m.setElement(4, 0x00);
        t.message(m);
        Assert.assertTrue(t.getRowCount()==1);
        Assert.assertEquals("message on", CbusTableEvent.EvState.ON,t.getValueAt(0,CbusEventTableDataModel.STATE_COLUMN));
        
        m.setElement(0, 0x99); 
        t.message(m);
        Assert.assertTrue(t.getRowCount()==1);
        Assert.assertEquals("message off", CbusTableEvent.EvState.OFF,t.getValueAt(0,CbusEventTableDataModel.STATE_COLUMN));

        Assert.assertEquals("message session out 2", 2 ,t.getValueAt(0,CbusEventTableDataModel.SESSION_OUT_COLUMN));
        Assert.assertEquals("message session in 0", 0 ,t.getValueAt(0,CbusEventTableDataModel.SESSION_IN_COLUMN));
        
        Assert.assertEquals("message session total 2", 2 ,t.getValueAt(0,CbusEventTableDataModel.SESSION_TOTAL_COLUMN));
        t.message(m);
        Assert.assertEquals("message session total 3", 3 ,t.getValueAt(0,CbusEventTableDataModel.SESSION_TOTAL_COLUMN));

        m.setElement(0, 0x9A); 
        t.message(m);
        Assert.assertTrue(t.getRowCount()==1);
        Assert.assertEquals("message request", CbusTableEvent.EvState.REQUEST,t.getValueAt(0,CbusEventTableDataModel.STATE_COLUMN));
        
        m.setElement(0, 0x90); 
        m.setElement(1, 0x01);
        t.message(m);
        Assert.assertTrue(t.getRowCount()==2);        

        m.setElement(1, 0x02);
        t.message(m);
        Assert.assertTrue(t.getRowCount()==3); 
        
        Assert.assertEquals("can id message", tcis.getCanid(),t.getValueAt(0,CbusEventTableDataModel.CANID_COLUMN));
        
        Assert.assertEquals("not editable",false,t.isCellEditable(0,CbusEventTableDataModel.SESSION_OFF_COLUMN));
        Assert.assertEquals("editable",true,t.isCellEditable(0,CbusEventTableDataModel.COMMENT_COLUMN));
        
        
        t.removeRow(0);
        Assert.assertTrue(t.getRowCount()==2); 
        
        t.removeRow(1);
        Assert.assertTrue(t.getRowCount()==1);         

        t.removeRow(0);
        Assert.assertTrue(t.getRowCount()==0); 
        
        
        Assert.assertEquals("listener",1,tcis.numListeners());
        t.dispose();
        Assert.assertEquals("listener",0,tcis.numListeners());
        t = null;
    }

    @Test
    public void testColumnHeadings() {
        
        CbusEventTableDataModel t = new CbusEventTableDataModel( memo,4,CbusEventTableDataModel.MAX_COLUMN);
        
        Assert.assertEquals("column count",CbusEventTableDataModel.MAX_COLUMN,t.getColumnCount());
        
        Assert.assertTrue(t.getColumnName(CbusEventTableDataModel.CANID_COLUMN).contains("ID"));
        Assert.assertTrue(t.getColumnName(CbusEventTableDataModel.NODE_COLUMN).contains("Node"));
        Assert.assertTrue(t.getColumnName(CbusEventTableDataModel.NODENAME_COLUMN).contains("Name"));
        Assert.assertTrue(t.getColumnName(CbusEventTableDataModel.NAME_COLUMN).contains("Name"));
        Assert.assertTrue(t.getColumnName(CbusEventTableDataModel.EVENT_COLUMN).contains("Event"));
        Assert.assertTrue(t.getColumnName(CbusEventTableDataModel.STATE_COLUMN).contains("On"));
        Assert.assertTrue(t.getColumnName(CbusEventTableDataModel.COMMENT_COLUMN).contains("Comment"));
        Assert.assertTrue(t.getColumnName(CbusEventTableDataModel.DELETE_BUTTON_COLUMN).contains("Delete"));
        Assert.assertTrue(t.getColumnName(CbusEventTableDataModel.ON_BUTTON_COLUMN).contains("On"));
        Assert.assertTrue(t.getColumnName(CbusEventTableDataModel.OFF_BUTTON_COLUMN).contains("Off"));
        Assert.assertTrue(t.getColumnName(CbusEventTableDataModel.TOGGLE_BUTTON_COLUMN).contains("Toggle"));
        Assert.assertTrue(t.getColumnName(CbusEventTableDataModel.STATUS_REQUEST_BUTTON_COLUMN).contains("Status"));
        Assert.assertTrue(t.getColumnName(CbusEventTableDataModel.SESSION_ON_COLUMN).contains("On"));
        Assert.assertTrue(t.getColumnName(CbusEventTableDataModel.SESSION_OFF_COLUMN).contains("Off"));
        Assert.assertTrue(t.getColumnName(CbusEventTableDataModel.SESSION_IN_COLUMN).contains("In"));
        Assert.assertTrue(t.getColumnName(CbusEventTableDataModel.SESSION_OUT_COLUMN).contains("Out"));
        Assert.assertTrue(t.getColumnName(CbusEventTableDataModel.SESSION_TOTAL_COLUMN).contains("Total"));
        Assert.assertTrue(t.getColumnName(CbusEventTableDataModel.LATEST_TIMESTAMP_COLUMN).contains("Last"));
        Assert.assertTrue(t.getColumnName(CbusEventTableDataModel.STLR_ON_COLUMN).contains("On"));
        Assert.assertTrue(t.getColumnName(CbusEventTableDataModel.STLR_OFF_COLUMN).contains("Off"));
        Assert.assertTrue(t.getColumnName(999).contains("unknown"));

        t.dispose();
        t = null;

    }

    @Test
    public void testColumns() {
        
        CbusEventTableDataModel t = new CbusEventTableDataModel( memo,4,CbusEventTableDataModel.MAX_COLUMN);
        
        Assert.assertTrue( t.getColumnCount()== 25 );
        
        for (int i = 0; i <t.getColumnCount(); i++) {
            Assert.assertFalse("column has name", t.getColumnName(i).isEmpty() );
            Assert.assertTrue("column has a width", CbusEventTableDataModel.getPreferredWidth(i) > 0 );
            Assert.assertTrue("column has a print width", CbusEventTableDataModel.getColumnWidth(i) > -1 );
            
        }
        
        Assert.assertTrue("column has NO name", t.getColumnName(999).equals("unknown") );
        Assert.assertTrue("column has NO width", CbusEventTableDataModel.getPreferredWidth(999) > 0 );
        
        Assert.assertTrue("column class integer",
            t.getColumnClass(CbusEventTableDataModel.SESSION_IN_COLUMN) ==  Integer.class );
        Assert.assertTrue("column class string",
            t.getColumnClass(CbusEventTableDataModel.COMMENT_COLUMN) ==  String.class );
        Assert.assertTrue("column class JButton",
            t.getColumnClass(CbusEventTableDataModel.OFF_BUTTON_COLUMN) ==  javax.swing.JButton.class );
        Assert.assertTrue("column class date",
            t.getColumnClass(CbusEventTableDataModel.LATEST_TIMESTAMP_COLUMN) ==  java.util.Date.class );
        Assert.assertTrue("column class state enum",
            t.getColumnClass(CbusEventTableDataModel.STATE_COLUMN) ==  Enum.class );
        Assert.assertTrue("column class null", t.getColumnClass(999) ==  null );
        
        t.dispose();
        t = null;
        
    }
    
    @Test
    public void testGetValueAtSetVaalueAts() {
        
        // uses loopback in setvalueat tests
        TrafficControllerScaffold tcis = new TrafficControllerScaffoldLoopback();
        memo.setTrafficController(tcis);
        
        CbusEventTableDataModel t = new CbusEventTableDataModel( memo,4,CbusEventTableDataModel.MAX_COLUMN);
        Assert.assertEquals("rowcount 0",0,t.getRowCount());
        
        CanMessage m = new CanMessage(tcis.getCanid());
        CbusMessage.setPri(m, CbusConstants.DEFAULT_DYNAMIC_PRIORITY * 4 + CbusConstants.DEFAULT_MINOR_PRIORITY);
        m.setNumDataElements(5);
        m.setElement(0, 0x99); // short event off
        m.setElement(1, 0x00); // node 0
        m.setElement(2, 0x00); // node 0
        m.setElement(3, 0x07); // event 2003
        m.setElement(4, 0xd3); // event 2003
        t.message(m);
        Assert.assertTrue("row created",t.getRowCount()==1);
        
        t.message(m);
        Assert.assertTrue("row NOT created",t.getRowCount()==1);
        Assert.assertEquals("Event number 2003", 2003 ,t.getValueAt(0,CbusEventTableDataModel.EVENT_COLUMN));
        Assert.assertEquals("Node number 0", 0 ,t.getValueAt(0,CbusEventTableDataModel.NODE_COLUMN));
        Assert.assertEquals("Node name empty", "" ,t.getValueAt(0,CbusEventTableDataModel.NODENAME_COLUMN));
        Assert.assertFalse("ON_BUTTON_COLUMN",t.getValueAt(0,CbusEventTableDataModel.ON_BUTTON_COLUMN) == null );
        Assert.assertFalse("OFF_BUTTON_COLUMN",t.getValueAt(0,CbusEventTableDataModel.OFF_BUTTON_COLUMN) == null );
        Assert.assertFalse("STATUS_REQUEST_BUTTON_COLUMN",t.getValueAt(0,CbusEventTableDataModel.STATUS_REQUEST_BUTTON_COLUMN) == null );
        Assert.assertFalse("DELETE_BUTTON_COLUMN",t.getValueAt(0,CbusEventTableDataModel.DELETE_BUTTON_COLUMN) == null );
        Assert.assertEquals("SESSION_OFF_COLUMN 2", 2 ,t.getValueAt(0,CbusEventTableDataModel.SESSION_OFF_COLUMN));
        Assert.assertFalse("LATEST_TIMESTAMP_COLUMN",t.getValueAt(0,CbusEventTableDataModel.LATEST_TIMESTAMP_COLUMN) == null );
        Assert.assertEquals("STLR_ON_COLUMN empty", "" ,t.getValueAt(0,CbusEventTableDataModel.STLR_ON_COLUMN));
        Assert.assertEquals("STLR_OFF_COLUMN empty", "" ,t.getValueAt(0,CbusEventTableDataModel.STLR_OFF_COLUMN));
        Assert.assertNull("default column",t.getValueAt(0,999));
        Assert.assertTrue("empty event name not on table string",t.getEventName(0,2).isEmpty() );
        Assert.assertTrue("empty event name on table string",t.getEventName(0,2003).isEmpty() );
        Assert.assertEquals("Event name empty", "" ,t.getValueAt(0,CbusEventTableDataModel.NAME_COLUMN));
        
        t.setValueAt("Alonso",0,CbusEventTableDataModel.NAME_COLUMN);
        Assert.assertEquals(" name Alonso", "Alonso" ,t.getValueAt(0,CbusEventTableDataModel.NAME_COLUMN));
        Assert.assertEquals("event name on table string","Alonso",t.getEventName(0,2003));
        Assert.assertEquals("COMMENT_COLUMN empty", "" ,t.getValueAt(0,CbusEventTableDataModel.COMMENT_COLUMN));
        
        t.setValueAt("My Comment",0,CbusEventTableDataModel.COMMENT_COLUMN);
        Assert.assertEquals("COMMENT_COLUMN", "My Comment" ,t.getValueAt(0,CbusEventTableDataModel.COMMENT_COLUMN));
        Assert.assertEquals("SESSION_ON_COLUMN 0", 0 ,t.getValueAt(0,CbusEventTableDataModel.SESSION_ON_COLUMN));
        
        t.setValueAt("do button Click",0,CbusEventTableDataModel.ON_BUTTON_COLUMN);
        JUnitUtil.waitFor(()->{ return(tcis.outbound.size()>0); }, " outbound 1 didn't arrive");
        Assert.assertEquals(" 1 outbound increased", 1,(tcis.outbound.size() ) );
        Assert.assertEquals("table sends on event for short 2003 ", "[5f8] 98 00 00 07 D3",
            tcis.outbound.elementAt(tcis.outbound.size() - 1).toString());
        Assert.assertTrue("row NOT created",t.getRowCount()==1);
        Assert.assertEquals("message session total 3", 3 ,t.getValueAt(0,CbusEventTableDataModel.SESSION_TOTAL_COLUMN));
        Assert.assertEquals("SESSION_ON_COLUMN 1", 1 ,t.getValueAt(0,CbusEventTableDataModel.SESSION_ON_COLUMN));
        Assert.assertEquals("SESSION_OFF_COLUMN 2 still", 2 ,t.getValueAt(0,CbusEventTableDataModel.SESSION_OFF_COLUMN));
        
        t.setValueAt("do button Click",0,CbusEventTableDataModel.OFF_BUTTON_COLUMN);
        JUnitUtil.waitFor(()->{ return(tcis.outbound.size()>1); }, " outbound 2 didn't arrive");
        Assert.assertEquals(" 2 outbound increased", 2,(tcis.outbound.size() ) );
        Assert.assertEquals("table sends off event for short 2003 ", "[5f8] 99 00 00 07 D3",
            tcis.outbound.elementAt(tcis.outbound.size() - 1).toString());
        Assert.assertTrue("row NOT created",t.getRowCount()==1);
        Assert.assertEquals("message session total 4", 4 ,t.getValueAt(0,CbusEventTableDataModel.SESSION_TOTAL_COLUMN));
        Assert.assertEquals("SESSION_OFF_COLUMN 3", 3 ,t.getValueAt(0,CbusEventTableDataModel.SESSION_OFF_COLUMN));
        Assert.assertTrue(( (String) t.getValueAt(0,CbusEventTableDataModel.TOGGLE_BUTTON_COLUMN) ).contains("On"));
        Assert.assertFalse(( (String) t.getValueAt(0,CbusEventTableDataModel.TOGGLE_BUTTON_COLUMN) ).contains("Off"));
        
        t.setValueAt("do button Click",0,CbusEventTableDataModel.TOGGLE_BUTTON_COLUMN);
        JUnitUtil.waitFor(()->{ return(tcis.outbound.size()>2); }, " outbound 3 didn't arrive");
        Assert.assertEquals(" 3 outbound increased", 3,(tcis.outbound.size() ) );
        Assert.assertEquals("table sends on event for short 2003 ", "[5f8] 98 00 00 07 D3",
            tcis.outbound.elementAt(tcis.outbound.size() - 1).toString());
        Assert.assertTrue("row NOT created",t.getRowCount()==1);
        Assert.assertEquals("message session total 5", 5 ,t.getValueAt(0,CbusEventTableDataModel.SESSION_TOTAL_COLUMN));
        Assert.assertTrue(( (String) t.getValueAt(0,CbusEventTableDataModel.TOGGLE_BUTTON_COLUMN) ).contains("Off"));
        Assert.assertFalse(( (String) t.getValueAt(0,CbusEventTableDataModel.TOGGLE_BUTTON_COLUMN) ).contains("On"));
        
        t.setValueAt("do button Click",0,CbusEventTableDataModel.TOGGLE_BUTTON_COLUMN);
        JUnitUtil.waitFor(()->{ return(tcis.outbound.size()>3); }, " outbound 4 didn't arrive");
        Assert.assertEquals(" 4 outbound increased", 4,(tcis.outbound.size() ) );
        Assert.assertEquals("table sends off event for short 2003 ", "[5f8] 99 00 00 07 D3",
            tcis.outbound.elementAt(tcis.outbound.size() - 1).toString());
        Assert.assertTrue("row NOT created",t.getRowCount()==1);
        Assert.assertEquals("message session total 6", 6 ,t.getValueAt(0,CbusEventTableDataModel.SESSION_TOTAL_COLUMN));
        Assert.assertTrue(( (String) t.getValueAt(0,CbusEventTableDataModel.TOGGLE_BUTTON_COLUMN) ).contains("On"));
        Assert.assertFalse(( (String) t.getValueAt(0,CbusEventTableDataModel.TOGGLE_BUTTON_COLUMN) ).contains("Off"));
        
        
        // create new event with no initial on / off status
        t.addEvent(65432,12345,77,CbusTableEvent.EvState.UNKNOWN ,"The Doctor","Doctor Event Comment",0,0,0,0);
        Assert.assertTrue("row created",t.getRowCount()==2);
        Assert.assertTrue(( (String) t.getValueAt(1,CbusEventTableDataModel.TOGGLE_BUTTON_COLUMN) ).contains("Off"));
        Assert.assertFalse(( (String) t.getValueAt(1,CbusEventTableDataModel.TOGGLE_BUTTON_COLUMN) ).contains("On"));
        Assert.assertEquals("Event number 12345", 12345 ,t.getValueAt(1,CbusEventTableDataModel.EVENT_COLUMN));
        Assert.assertEquals("Node number 65432", 65432 ,t.getValueAt(1,CbusEventTableDataModel.NODE_COLUMN));
        Assert.assertTrue("LATEST_TIMESTAMP_COLUMN",
            t.getValueAt(1,CbusEventTableDataModel.LATEST_TIMESTAMP_COLUMN) == null );
        Assert.assertEquals("name The Doctor", "The Doctor" ,
            t.getValueAt(1,CbusEventTableDataModel.NAME_COLUMN));
        Assert.assertEquals("Doctor Event Comment", "Doctor Event Comment" ,
            t.getValueAt(1,CbusEventTableDataModel.COMMENT_COLUMN));
        
        t.setValueAt("do button Click",1,CbusEventTableDataModel.TOGGLE_BUTTON_COLUMN);
        JUnitUtil.waitFor(()->{ return(tcis.outbound.size()>4); }, " outbound 5 didn't arrive");
        Assert.assertEquals(" 5 outbound increased", 5,(tcis.outbound.size() ) );
        Assert.assertEquals("table sends off event for long 12345 node 65432 ", "[5f8] 91 FF 98 30 39",
            tcis.outbound.elementAt(tcis.outbound.size() - 1).toString());
        Assert.assertTrue("row NOT created",t.getRowCount()==2);
        Assert.assertEquals("2 message session total 1", 1 ,t.getValueAt(1,CbusEventTableDataModel.SESSION_TOTAL_COLUMN));
        Assert.assertEquals("2 SESSION_OFF_COLUMN 1 ", 1 ,t.getValueAt(1,CbusEventTableDataModel.SESSION_OFF_COLUMN));
        Assert.assertEquals("2 SESSION_ON_COLUMN 0 ", 0 ,t.getValueAt(1,CbusEventTableDataModel.SESSION_ON_COLUMN));
        Assert.assertFalse("LATEST_TIMESTAMP_COLUMN",
            t.getValueAt(1,CbusEventTableDataModel.LATEST_TIMESTAMP_COLUMN) == null );
        
        t.setValueAt("do button Click",1,CbusEventTableDataModel.STATUS_REQUEST_BUTTON_COLUMN);
        JUnitUtil.waitFor(()->{ return(tcis.outbound.size()>5); }, " outbound 6 didn't arrive");
        Assert.assertEquals(" 6 outbound increased", 6,(tcis.outbound.size() ) );
        Assert.assertEquals("table sends request long 12345 node 65432 ", "[5f8] 92 FF 98 30 39",
            tcis.outbound.elementAt(tcis.outbound.size() - 1).toString());
        
        t.setValueAt("do button Click",0,CbusEventTableDataModel.STATUS_REQUEST_BUTTON_COLUMN);
        JUnitUtil.waitFor(()->{ return(tcis.outbound.size()>6); }, " outbound 7 didn't arrive");
        Assert.assertEquals(" 7 outbound increased", 7,(tcis.outbound.size() ) );
        Assert.assertEquals("table sends short request 2003 ", "[5f8] 9A 00 00 07 D3",
            tcis.outbound.elementAt(tcis.outbound.size() - 1).toString());
        
        t.setValueAt("on on text",0,CbusEventTableDataModel.STLR_ON_COLUMN);
        Assert.assertEquals("set STLR_ON_COLUMN", "on on text" ,t.getValueAt(0,CbusEventTableDataModel.STLR_ON_COLUMN));
        
        t.setValueAt("off off text",0,CbusEventTableDataModel.STLR_OFF_COLUMN);
        Assert.assertEquals("set STLR_OFF_COLUMN", "off off text" ,t.getValueAt(0,CbusEventTableDataModel.STLR_OFF_COLUMN));
        
        
        // disable confirm popup, that's tested in the table action class
        t.ta.sessionConfirmDeleteRow = false;
        
        t.setValueAt("do button Click",0,CbusEventTableDataModel.DELETE_BUTTON_COLUMN);
        Assert.assertTrue("row deleted 0",t.getRowCount()==1);
        
        // row 1 is now row 0
        Assert.assertEquals("row 0 Event number 12345", 12345 ,t.getValueAt(0,CbusEventTableDataModel.EVENT_COLUMN));
        Assert.assertEquals("row 0 Node number 65432", 65432 ,t.getValueAt(0,CbusEventTableDataModel.NODE_COLUMN));
        Assert.assertEquals("row 0 The Doctor", "The Doctor" ,
            t.getValueAt(0,CbusEventTableDataModel.NAME_COLUMN));
        Assert.assertEquals("row 0 Doctor Event Comment", "Doctor Event Comment" ,
            t.getValueAt(0,CbusEventTableDataModel.COMMENT_COLUMN));

        t.dispose();
        t = null;
        
    }
    
    @Test
    public void testProvidesEvent() {
    
        TrafficControllerScaffold tcis = new TrafficControllerScaffold();
        memo.setTrafficController(tcis);
        
        
        CbusEventTableDataModel t = new CbusEventTableDataModel( memo,4,CbusEventTableDataModel.MAX_COLUMN);
        
        CbusTableEvent event1 = t.provideEvent(123,456);
        Assert.assertTrue(t.getRowCount()==1);
        CbusTableEvent event2 = t.provideEvent(123,456);
        Assert.assertTrue(t.getRowCount()==1);
        CbusTableEvent event3 = t.provideEvent(111,222);
        Assert.assertTrue(t.getRowCount()==2);
        
        Assert.assertTrue("equals",event1.equals(event2));
        Assert.assertFalse("not equal",event1.equals(event3));
        
        event1 = null;
        event2 = null;
        event3 = null;
        t.dispose();
        t = null;
    
    }
    
    // The minimal setup for log4J
    @Before
    public void setUp() {
        JUnitUtil.setUp();
        memo = new CanSystemConnectionMemo();
    }

    @After
    public void tearDown() {
        memo = null;
        JUnitUtil.clearShutDownManager(); // put in place because AbstractMRTrafficController implementing subclass was not terminated properly
        JUnitUtil.tearDown();

        
    }

    // private final static Logger log = LoggerFactory.getLogger(CbusEventTableDataModelTest.class);

}
