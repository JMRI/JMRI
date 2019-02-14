package jmri.jmrix.can.cbus.eventtable;

import jmri.jmrix.can.CanMessage;
import jmri.jmrix.can.CanReply;
import jmri.jmrix.can.CanSystemConnectionMemo;
import jmri.jmrix.can.TrafficControllerScaffold;
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

    CanSystemConnectionMemo memo;

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
        
        Assert.assertTrue(t.getRowCount()==0);
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
        JUnitUtil.tearDown();
        memo = null;
    }

    // private final static Logger log = LoggerFactory.getLogger(CbusEventTableDataModelTest.class);

}
