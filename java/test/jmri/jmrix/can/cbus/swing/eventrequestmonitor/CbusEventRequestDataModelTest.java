package jmri.jmrix.can.cbus.swing.eventrequestmonitor;

import jmri.jmrix.can.CanMessage;
import jmri.jmrix.can.CanReply;
import jmri.jmrix.can.CanSystemConnectionMemo;
import jmri.jmrix.can.TrafficControllerScaffold;
import jmri.jmrix.can.cbus.CbusConstants;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;

/**
 * Test simple functioning of CbusEventRequestDataModel
 *
 * @author Paul Bender Copyright (C) 2016
 * @author Steve Young Copyright (C) 2019
 */
public class CbusEventRequestDataModelTest {

    

    @Test
    public void testCtor() {
        
        CbusEventRequestDataModel t = new CbusEventRequestDataModel(memo, 1,
                CbusEventRequestDataModel.MAX_COLUMN); // controller, row, column
        Assert.assertNotNull("exists", t);
        
        t.dispose();
        t = null;
        
    }
    
    @Test
    public void testCanListenAndRemove() {
        
        Assert.assertEquals("no listener to start with",0,tcis.numListeners());
        CbusEventRequestDataModel t = new CbusEventRequestDataModel(
        memo,5,CbusEventRequestDataModel.MAX_COLUMN);
        Assert.assertTrue("table listening",1 == tcis.numListeners());
        
        t.dispose();
        Assert.assertTrue("no listener to finish with",0 == tcis.numListeners());
        
        t = null;
        
    }
    
    @Test
    public void testColumns() {
        
        CbusEventRequestDataModel t = new CbusEventRequestDataModel(
        memo,5,CbusEventRequestDataModel.MAX_COLUMN);
        
        Assert.assertTrue( t.getColumnCount()== 12 );
        
        for (int i = 0; i <t.getColumnCount(); i++) {
            Assert.assertFalse("column has name", t.getColumnName(i).isEmpty() );
            Assert.assertTrue("column has a width", CbusEventRequestDataModel.getPreferredWidth(i) > 0 );
        }
        
        Assert.assertTrue("column has NO name", t.getColumnName(999).equals("unknown") );
        Assert.assertTrue("column has NO width", CbusEventRequestDataModel.getPreferredWidth(999) > 0 );
        Assert.assertTrue("column class integer",
            t.getColumnClass(CbusEventRequestDataModel.FEEDBACKTIMEOUT_COLUMN) ==  Integer.class );
        Assert.assertTrue("column class string",
            t.getColumnClass(CbusEventRequestDataModel.NAME_COLUMN) ==  String.class );
        Assert.assertTrue("column class JButton",
            t.getColumnClass(CbusEventRequestDataModel.DELETE_BUTTON_COLUMN) ==  javax.swing.JButton.class );
        Assert.assertTrue("column class LATEST_TIMESTAMP_COLUMN",
            t.getColumnClass(CbusEventRequestDataModel.LATEST_TIMESTAMP_COLUMN) ==  java.util.Date.class );
        Assert.assertTrue("column class enum",
            t.getColumnClass(CbusEventRequestDataModel.LASTFEEDBACK_COLUMN) ==  Enum.class );
        Assert.assertTrue("column class null",
            t.getColumnClass(999) ==  null );
        
        t.dispose();
        t = null;
        
    }
    
    @Test
    public void testCreateRow() {
        
        CbusEventRequestDataModel t = new CbusEventRequestDataModel(
        memo,5,CbusEventRequestDataModel.MAX_COLUMN);
        
        Assert.assertTrue("no rows to start",0 == t.getRowCount() );
        
        CanReply r = new CanReply();
        r.setHeader(tcis.getCanid());
        r.setNumDataElements(5);
        r.setElement(0, CbusConstants.CBUS_AREQ); // long event status request
        r.setElement(1, 0x04); // node 1234
        r.setElement(2, 0xd2); // node 1234
        r.setElement(3, 0x00); // event 7
        r.setElement(4, 0x07); // event 7
        
        t.reply(r);
        
        Assert.assertTrue("1 row",1 == t.getRowCount() );
        
        Assert.assertTrue("Editable",t.isCellEditable(0,CbusEventRequestDataModel.STATUS_REQUEST_BUTTON_COLUMN) );
        Assert.assertFalse("Not Editable",t.isCellEditable(0,999) );
        
        Assert.assertEquals("Event number 7", 7 ,t.getValueAt(0,CbusEventRequestDataModel.EVENT_COLUMN));
        Assert.assertEquals("Node number 1234", 1234 ,t.getValueAt(0,CbusEventRequestDataModel.NODE_COLUMN));
        Assert.assertEquals("No name set", "" ,t.getValueAt(0,CbusEventRequestDataModel.NAME_COLUMN));
        Assert.assertEquals("Status button", "Status" ,
            t.getValueAt(0,CbusEventRequestDataModel.STATUS_REQUEST_BUTTON_COLUMN));
        Assert.assertEquals("Delete button", "Delete" ,
            t.getValueAt(0,CbusEventRequestDataModel.DELETE_BUTTON_COLUMN));
        Assert.assertNull("LATEST_TIMESTAMP_COLUMN",
            t.getValueAt(0,CbusEventRequestDataModel.LATEST_TIMESTAMP_COLUMN) );
        Assert.assertEquals("last feedback col request", CbusEventRequestMonitorEvent.FbState.LfbFinding ,
            t.getValueAt(0,CbusEventRequestDataModel.LASTFEEDBACK_COLUMN));
        Assert.assertEquals("feedback tot reqd col", 1 ,
            t.getValueAt(0,CbusEventRequestDataModel.FEEDBACKREQUIRED_COLUMN));
        Assert.assertEquals("feedback still reqd col", 1 ,
            t.getValueAt(0,CbusEventRequestDataModel.FEEDBACKOUTSTANDING_COLUMN));
        Assert.assertEquals("feedback event 0", 0 ,
            t.getValueAt(0,CbusEventRequestDataModel.FEEDBACKEVENT_COLUMN));
        Assert.assertEquals("feedback node 0", 0 ,
            t.getValueAt(0,CbusEventRequestDataModel.FEEDBACKNODE_COLUMN));
        Assert.assertEquals("feedback timeout ms col", 4000 ,
            t.getValueAt(0,CbusEventRequestDataModel.FEEDBACKTIMEOUT_COLUMN));
        Assert.assertNull("no column", t.getValueAt(0,999));
        
        
        CanMessage m = new CanMessage(tcis.getCanid());
        m.setNumDataElements(5);
        m.setElement(0, CbusConstants.CBUS_ARON); // long event request response on
        m.setElement(1, 0x04); // node 1234
        m.setElement(2, 0xd2); // node 1234
        m.setElement(3, 0x00); // event 7
        m.setElement(4, 0x07); // event 7
        
        t.message(m);
        
        Assert.assertTrue("1 row",1 == t.getRowCount() );
        Assert.assertEquals("feedback lower", 0 ,
            t.getValueAt(0,CbusEventRequestDataModel.FEEDBACKOUTSTANDING_COLUMN));
        Assert.assertNotNull("LATEST_TIMESTAMP_COLUMN populated",
            t.getValueAt(0,CbusEventRequestDataModel.LATEST_TIMESTAMP_COLUMN) );
        Assert.assertEquals("last feedback good", CbusEventRequestMonitorEvent.FbState.LfbGood ,
            t.getValueAt(0,CbusEventRequestDataModel.LASTFEEDBACK_COLUMN));
        
        Assert.assertTrue("nothing sent by model",0 == tcis.outbound.size() );
        
        t.setValueAt("do button Click",0,CbusEventRequestDataModel.STATUS_REQUEST_BUTTON_COLUMN);
        
        JUnitUtil.waitFor(()->{ return(tcis.outbound.size()>0); }, " outbound 1 didn't arrive");
        Assert.assertEquals(" 1 outbound increased", 1,(tcis.outbound.size() ) );
        Assert.assertEquals("table sends request event for long 7 node 1234 ", "[5f8] 92 04 D2 00 07",
            tcis.outbound.elementAt(tcis.outbound.size() - 1).toString());
        
        t.dispose();
        t = null;
        
    }
    
    @Test
    public void testNotCreateRowExtendedRtr() {
        
        CbusEventRequestDataModel t = new CbusEventRequestDataModel(
        memo,5,CbusEventRequestDataModel.MAX_COLUMN);
        
        Assert.assertTrue("no rows to start",0 == t.getRowCount() );
        
        CanReply r = new CanReply();
        r.setHeader(tcis.getCanid());
        r.setNumDataElements(5);
        r.setElement(0, CbusConstants.CBUS_AREQ); // long event status request
        r.setElement(1, 0x04); // node 1234
        r.setElement(2, 0xd2); // node 1234
        r.setElement(3, 0x00); // event 7
        r.setElement(4, 0x07); // event 7
        
        r.setExtended(true);
        t.reply(r);
        Assert.assertTrue("no rows as extended",0 == t.getRowCount() );
        
        r.setExtended(false);
        r.setRtr(true);
        t.reply(r);
        Assert.assertTrue("no rows as rtr",0 == t.getRowCount() );
        
        CanMessage m = new CanMessage(tcis.getCanid());
        m.setNumDataElements(5);
        m.setElement(0, CbusConstants.CBUS_AREQ); // long event status request
        m.setElement(1, 0x04); // node 1234
        m.setElement(2, 0xd2); // node 1234
        m.setElement(3, 0x00); // event 7
        m.setElement(4, 0x07); // event 7
        
        m.setExtended(true);
        t.message(m);
        Assert.assertTrue("no rows as rtr",0 == t.getRowCount() );
        
        m.setExtended(false);
        m.setRtr(true);
        t.message(m);
        Assert.assertTrue("no rows as rtr",0 == t.getRowCount() );
        
        t.dispose();
        t = null;
        
    }
    
    private TrafficControllerScaffold tcis;
    private CanSystemConnectionMemo memo;

    @Before
    public void setUp() {
        JUnitUtil.setUp();
        
        memo = new CanSystemConnectionMemo();
        tcis = new TrafficControllerScaffold();
        memo.setTrafficController(tcis);
        
    }

    @After
    public void tearDown() {        
        
        tcis = null;
        memo = null;
        JUnitUtil.clearShutDownManager(); // put in place because AbstractMRTrafficController implementing subclass was not terminated properly
        JUnitUtil.tearDown();

    }

}
