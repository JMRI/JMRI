package jmri.jmrix.can.cbus.swing.cbusslotmonitor;

import jmri.jmrix.can.CanMessage;
import jmri.jmrix.can.CanReply;
import jmri.jmrix.can.cbus.CbusConstants;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;

/**
 * Test simple functioning of CbusSlotMonitorDataModel
 *
 * @author	Paul Bender Copyright (C) 2016
 * @author	Steve Young Copyright (C) 2019
 */
public class CbusSlotMonitorDataModelTest {
 
    private jmri.jmrix.can.TrafficControllerScaffold tcis = null;
    private jmri.jmrix.can.CanSystemConnectionMemo memo = null;
    private CbusSlotMonitorDataModel t = null;

    @Test
    public void testCtor() {
        Assert.assertNotNull("exists", t);
    }
    
    @Test
    public void testAddToTable() {
        
        Assert.assertEquals("column count",CbusSlotMonitorDataModel.MAX_COLUMN,t.getColumnCount());
        
        Assert.assertTrue(t.getRowCount()==0);
        t.reply(new CanReply( new int[]{0x05},0x12 ));
        t.message(new CanMessage( new int[]{0x05},0x12 ));
        Assert.assertTrue(t.getRowCount()==0);
        
        CanReply r = new CanReply();
        r.setHeader(tcis.getCanid());
        r.setNumDataElements(3);
        r.setElement(0, CbusConstants.CBUS_RLOC); 
        r.setElement(1, 0x00);
        r.setElement(2, 0x03);
        
        r.setExtended(true);
        t.reply(r);
        
        r.setExtended(false);
        r.setRtr(true);
        t.reply(r);
        
        Assert.assertTrue("reply ext rtr",t.getRowCount()==0);
        
        r.setRtr(false);
        t.reply(r);
        
        Assert.assertTrue("reply rloc 3",t.getRowCount()==1);
        
        int locoId = (Integer) t.getValueAt(0,CbusSlotMonitorDataModel.LOCO_ID_COLUMN);
        Assert.assertTrue("reply rloc 3 cell val",3 == locoId );
        String spdStep = (String) t.getValueAt(0,CbusSlotMonitorDataModel.SPEED_STEP_COLUMN);
        Assert.assertEquals("reply rloc 3 cell val speedstep","128",spdStep );
        String func = (String) t.getValueAt(0,CbusSlotMonitorDataModel.FUNCTION_LIST);
        Assert.assertTrue("reply rloc 3 cell val unknown func",func.isEmpty() );
        
        
        CanMessage m = new CanMessage( tcis.getCanid() );
        m.setNumDataElements(3);
        m.setElement(0, CbusConstants.CBUS_RLOC); 
        m.setElement(1, 0x00);
        m.setElement(2, 0x07);
        
        m.setExtended(true);
        t.message(m);
        
        m.setExtended(false);
        m.setRtr(true);
        t.message(m);
        Assert.assertTrue("msg ext rtr",t.getRowCount()==1);
        
        m.setRtr(false);
        t.message(m);
        
        Assert.assertTrue("msg rloc 7",t.getRowCount()==2);
        
        CanMessage ma = new CanMessage( tcis.getCanid() );
        ma.setNumDataElements(3);
        ma.setElement(0, CbusConstants.CBUS_RLOC); 
        ma.setElement(1, 0x00);
        ma.setElement(2, 0x03);
        t.message(ma);        
        Assert.assertTrue("msg rloc 3",t.getRowCount()==2);
        
        
        CanReply ra = new CanReply();
        ra.setHeader(tcis.getCanid());
        ra.setNumDataElements(8);
        ra.setElement(0, CbusConstants.CBUS_PLOC);
        ra.setElement(1, 0x01);
        ra.setElement(2, 0x00);
        ra.setElement(3, 0x04);
        ra.setElement(4, 0xa7);
        ra.setElement(5, 0xa2);
        ra.setElement(6, 0x7b);
        ra.setElement(7, 0x00);
        t.reply(ra);
        Assert.assertTrue("reply ploc 4",t.getRowCount()==3);
        
        int locoIdb = (Integer) t.getValueAt(2,CbusSlotMonitorDataModel.LOCO_ID_COLUMN);
        Assert.assertEquals("reply ploc 4 cell val",4,locoIdb );
        String spdStepb = (String) t.getValueAt(2,CbusSlotMonitorDataModel.SPEED_STEP_COLUMN);
        Assert.assertEquals("reply ploc 4 cell val speedstep","128",spdStepb );
        String funcb = (String) t.getValueAt(2,CbusSlotMonitorDataModel.FUNCTION_LIST);
        Assert.assertEquals("reply ploc 4 cell val funcs","2 5 6 8 ",funcb );
        int locoSpd = (Integer) t.getValueAt(2,CbusSlotMonitorDataModel.LOCO_COMMANDED_SPEED_COLUMN);
        Assert.assertEquals("reply ploc 4 cell val speed",39,locoSpd );
        String dirb = (String) t.getValueAt(2,CbusSlotMonitorDataModel.LOCO_DIRECTION_COLUMN);
        Assert.assertEquals("reply ploc 4 cell val direction",Bundle.getMessage("FWD"),dirb );
        
        CanMessage mb = new CanMessage(tcis.getCanid());
        mb.setNumDataElements(8);
        mb.setElement(0, CbusConstants.CBUS_PLOC); 
        mb.setElement(1, 0x02);
        mb.setElement(2, 0x00);
        mb.setElement(3, 0x34);
        mb.setElement(4, 0x07);
        mb.setElement(5, 0x08);
        mb.setElement(6, 0x03);
        mb.setElement(7, 0x0a);
        t.message(mb);
        Assert.assertTrue("msg ploc x34",t.getRowCount()==4);        
        
        CanReply rb = new CanReply();
        rb.setHeader(tcis.getCanid());
        rb.setNumDataElements(6);
        rb.setElement(0, CbusConstants.CBUS_GLOC);
        rb.setElement(1, 0x00);
        rb.setElement(2, 0x04);
        rb.setElement(3, 0x04);
        rb.setElement(4, 0x00);
        rb.setElement(5, 0x00);
        t.reply(rb);
        Assert.assertTrue("reply gloc 4",t.getRowCount()==4);
        
        CanMessage mc = new CanMessage(tcis.getCanid());
        mc.setNumDataElements(6);
        mc.setElement(0, CbusConstants.CBUS_GLOC); 
        mc.setElement(1, 0x04);
        mc.setElement(2, 0x34);
        mc.setElement(3, 0x34);
        mc.setElement(4, 0x07);
        mc.setElement(5, 0x08);
        t.message(mc);
        Assert.assertTrue("msg gloc 0x04 0x34",t.getRowCount()==5);
        
    }
    
    @Test
    public void testCanListenAndRemove() {
        Assert.assertTrue("table listening",1 == tcis.numListeners());
        t.dispose();
        Assert.assertEquals("no listener after didpose",0,tcis.numListeners());
        t = new CbusSlotMonitorDataModel(memo,5,CbusSlotMonitorDataModel.MAX_COLUMN);
        Assert.assertTrue("table listening again",1 == tcis.numListeners());     
    }
    
    @Test
    public void testColumns() {
        
        Assert.assertNotNull("exists", t.tablefeedback() );
        Assert.assertTrue( t.getColumnCount()== 10 );
        
        for (int i = 0; i <t.getColumnCount(); i++) {
            Assert.assertFalse("column has name", t.getColumnName(i).isEmpty() );
            Assert.assertTrue("column has a width", CbusSlotMonitorDataModel.getPreferredWidth(i) > 0 );
            
        }
        
        Assert.assertTrue("column has NO name", t.getColumnName(999).equals("unknown") );
        Assert.assertTrue("column has NO width", CbusSlotMonitorDataModel.getPreferredWidth(999) > 0 );
        
        Assert.assertTrue("column class integer",
            t.getColumnClass(CbusSlotMonitorDataModel.SESSION_ID_COLUMN) ==  Integer.class );
        Assert.assertTrue("column class string",
            t.getColumnClass(CbusSlotMonitorDataModel.FLAGS_COLUMN) ==  String.class );
        Assert.assertTrue("column class JButton",
            t.getColumnClass(CbusSlotMonitorDataModel.ESTOP_COLUMN) ==  javax.swing.JButton.class );
        Assert.assertTrue("column class Boolean",
            t.getColumnClass(CbusSlotMonitorDataModel.LOCO_ID_LONG_COLUMN) ==  Boolean.class );
        Assert.assertTrue("column class null",
            t.getColumnClass(999) ==  null );
    }
    
    @Test
    public void testGetValueAtSetValueAtFunctions() {
        
        // table hears session 1 already in progress
        CanReply r = new CanReply();
        r.setHeader(tcis.getCanid());
        r.setNumDataElements(2);
        r.setElement(0, CbusConstants.CBUS_DKEEP); 
        r.setElement(1, 0x01);
        t.reply(r);
        
        Assert.assertEquals("QLOC Message sent to request session 1 details", "[5f8] 22 01",
            tcis.outbound.elementAt(tcis.outbound.size() - 1).toString());
        
        // Command station responds with details for session 1
        r = new CanReply();
        r.setHeader(tcis.getCanid());
        r.setNumDataElements(8);
        r.setElement(0, CbusConstants.CBUS_PLOC);
        r.setElement(1, 0x01); // session 1
        r.setElement(2, 0x00); // dcc ms byte
        r.setElement(3, 0x04); // dcc ls byte
        r.setElement(4, 0xa7); // speed direction
        r.setElement(5, 0xa2); // function f0 to f4
        r.setElement(6, 0x7b); // function f5 to f8
        r.setElement(7, 0x00); // function f9 to f12
        t.reply(r);
        
        Assert.assertTrue("row added",t.getRowCount()==1);
        Assert.assertFalse("not editable",t.isCellEditable(0,CbusSlotMonitorDataModel.LOCO_ID_LONG_COLUMN)); 
        Assert.assertTrue("clickable",t.isCellEditable(0,CbusSlotMonitorDataModel.ESTOP_COLUMN)); 

        Assert.assertEquals("Session 1",1, t.getValueAt(0,CbusSlotMonitorDataModel.SESSION_ID_COLUMN) );
        Assert.assertEquals("loco 4 ",4,t.getValueAt(0,CbusSlotMonitorDataModel.LOCO_ID_COLUMN) );
        Assert.assertEquals("Not Long",false, t.getValueAt(0,CbusSlotMonitorDataModel.LOCO_ID_LONG_COLUMN) );
        Assert.assertEquals("No consist",0, t.getValueAt(0,CbusSlotMonitorDataModel.LOCO_CONSIST_COLUMN) );
        Assert.assertEquals("Flags","", t.getValueAt(0,CbusSlotMonitorDataModel.FLAGS_COLUMN) );
        Assert.assertEquals("speed 39",39,
            t.getValueAt(0,CbusSlotMonitorDataModel.LOCO_COMMANDED_SPEED_COLUMN) );
        
        t.setValueAt("do button Click",0,CbusSlotMonitorDataModel.ESTOP_COLUMN);
        Assert.assertEquals("table sends estop session 1", "[5f8] 47 01 81",
            tcis.outbound.elementAt(tcis.outbound.size() - 1).toString());
        
        r = new CanReply();
        r.setHeader(tcis.getCanid());
        r.setNumDataElements(3);
        r.setElement(0, CbusConstants.CBUS_DSPD); 
        r.setElement(1, 0x01); // session 1
        r.setElement(2, 77); // integer speed 77
        t.reply(r);
        
        Assert.assertEquals("speed 77",77,
            t.getValueAt(0,CbusSlotMonitorDataModel.LOCO_COMMANDED_SPEED_COLUMN) );
        
        String dirb = (String) t.getValueAt(0,CbusSlotMonitorDataModel.LOCO_DIRECTION_COLUMN);
        Assert.assertEquals("dir rev",Bundle.getMessage("REV"),dirb );
        
        r = new CanReply();
        r.setHeader(tcis.getCanid());
        r.setNumDataElements(4);
        r.setElement(0, CbusConstants.CBUS_DFUN); 
        r.setElement(1, 0x01); // session 1
        r.setElement(2, 0); // Function Range
        r.setElement(3, 0); // Data
        t.reply(r); // range value not valid
        
        r.setElement(2, 1); // Function Range
        r.setElement(3, 0x1f);
        t.reply(r);
        Assert.assertTrue(((String)t.getValueAt(0,CbusSlotMonitorDataModel.FUNCTION_LIST)).contains("0 "));
        Assert.assertTrue(((String)t.getValueAt(0,CbusSlotMonitorDataModel.FUNCTION_LIST)).contains("1 "));
        Assert.assertTrue(((String)t.getValueAt(0,CbusSlotMonitorDataModel.FUNCTION_LIST)).contains("2 "));
        Assert.assertTrue(((String)t.getValueAt(0,CbusSlotMonitorDataModel.FUNCTION_LIST)).contains("3 "));
        Assert.assertTrue(((String)t.getValueAt(0,CbusSlotMonitorDataModel.FUNCTION_LIST)).contains("4 "));

        r.setElement(3, 0x00);
        t.reply(r);
        Assert.assertFalse(((String)t.getValueAt(0,CbusSlotMonitorDataModel.FUNCTION_LIST)).contains("0 "));
        Assert.assertFalse(((String)t.getValueAt(0,CbusSlotMonitorDataModel.FUNCTION_LIST)).contains("1 "));
        Assert.assertFalse(((String)t.getValueAt(0,CbusSlotMonitorDataModel.FUNCTION_LIST)).contains("2 "));
        Assert.assertFalse(((String)t.getValueAt(0,CbusSlotMonitorDataModel.FUNCTION_LIST)).contains("3 "));
        Assert.assertFalse(((String)t.getValueAt(0,CbusSlotMonitorDataModel.FUNCTION_LIST)).contains("4 "));
        
        r.setElement(2, 2);
        r.setElement(3, 0x0f);
        t.reply(r);
        Assert.assertTrue(((String)t.getValueAt(0,CbusSlotMonitorDataModel.FUNCTION_LIST)).contains("5 "));
        Assert.assertTrue(((String)t.getValueAt(0,CbusSlotMonitorDataModel.FUNCTION_LIST)).contains("6 "));
        Assert.assertTrue(((String)t.getValueAt(0,CbusSlotMonitorDataModel.FUNCTION_LIST)).contains("7 "));
        Assert.assertTrue(((String)t.getValueAt(0,CbusSlotMonitorDataModel.FUNCTION_LIST)).contains("8 "));
        
        r.setElement(3, 0x00);
        t.reply(r);
        Assert.assertFalse(((String)t.getValueAt(0,CbusSlotMonitorDataModel.FUNCTION_LIST)).contains("5 "));
        Assert.assertFalse(((String)t.getValueAt(0,CbusSlotMonitorDataModel.FUNCTION_LIST)).contains("6 "));
        Assert.assertFalse(((String)t.getValueAt(0,CbusSlotMonitorDataModel.FUNCTION_LIST)).contains("7 "));
        Assert.assertFalse(((String)t.getValueAt(0,CbusSlotMonitorDataModel.FUNCTION_LIST)).contains("8 "));
        
        // make sure that CanMessages also acted on
        CanMessage m = new CanMessage( tcis.getCanid() );
        m.setNumDataElements(4);
        m.setElement(0, CbusConstants.CBUS_DFUN); 
        m.setElement(1, 0x01); // session 1
        m.setElement(2, 3); // Function Range
        m.setElement(3, 0x0f); // Data
        t.message(m);
        
        Assert.assertTrue(((String)t.getValueAt(0,CbusSlotMonitorDataModel.FUNCTION_LIST)).contains("9 "));
        Assert.assertTrue(((String)t.getValueAt(0,CbusSlotMonitorDataModel.FUNCTION_LIST)).contains("10"));
        Assert.assertTrue(((String)t.getValueAt(0,CbusSlotMonitorDataModel.FUNCTION_LIST)).contains("11"));
        Assert.assertTrue(((String)t.getValueAt(0,CbusSlotMonitorDataModel.FUNCTION_LIST)).contains("12"));
        
        m.setElement(3, 0x00); // Data
        t.message(m);
        Assert.assertFalse(((String)t.getValueAt(0,CbusSlotMonitorDataModel.FUNCTION_LIST)).contains("9 "));
        Assert.assertFalse(((String)t.getValueAt(0,CbusSlotMonitorDataModel.FUNCTION_LIST)).contains("10"));
        Assert.assertFalse(((String)t.getValueAt(0,CbusSlotMonitorDataModel.FUNCTION_LIST)).contains("11"));
        Assert.assertFalse(((String)t.getValueAt(0,CbusSlotMonitorDataModel.FUNCTION_LIST)).contains("12"));
        
        
        m.setElement(2, 4); // Function Range
        m.setElement(3, 0xff); // Data
        t.message(m);
        Assert.assertTrue(((String)t.getValueAt(0,CbusSlotMonitorDataModel.FUNCTION_LIST)).contains("13"));
        Assert.assertTrue(((String)t.getValueAt(0,CbusSlotMonitorDataModel.FUNCTION_LIST)).contains("14"));
        Assert.assertTrue(((String)t.getValueAt(0,CbusSlotMonitorDataModel.FUNCTION_LIST)).contains("15"));
        Assert.assertTrue(((String)t.getValueAt(0,CbusSlotMonitorDataModel.FUNCTION_LIST)).contains("16"));
        Assert.assertTrue(((String)t.getValueAt(0,CbusSlotMonitorDataModel.FUNCTION_LIST)).contains("17"));
        Assert.assertTrue(((String)t.getValueAt(0,CbusSlotMonitorDataModel.FUNCTION_LIST)).contains("18"));
        Assert.assertTrue(((String)t.getValueAt(0,CbusSlotMonitorDataModel.FUNCTION_LIST)).contains("19"));
        Assert.assertTrue(((String)t.getValueAt(0,CbusSlotMonitorDataModel.FUNCTION_LIST)).contains("20"));
        
        m.setElement(3, 0x00); // Data
        t.message(m);
        Assert.assertFalse(((String)t.getValueAt(0,CbusSlotMonitorDataModel.FUNCTION_LIST)).contains("13"));
        Assert.assertFalse(((String)t.getValueAt(0,CbusSlotMonitorDataModel.FUNCTION_LIST)).contains("14"));
        Assert.assertFalse(((String)t.getValueAt(0,CbusSlotMonitorDataModel.FUNCTION_LIST)).contains("15"));
        Assert.assertFalse(((String)t.getValueAt(0,CbusSlotMonitorDataModel.FUNCTION_LIST)).contains("16"));
        Assert.assertFalse(((String)t.getValueAt(0,CbusSlotMonitorDataModel.FUNCTION_LIST)).contains("17"));
        Assert.assertFalse(((String)t.getValueAt(0,CbusSlotMonitorDataModel.FUNCTION_LIST)).contains("18"));
        Assert.assertFalse(((String)t.getValueAt(0,CbusSlotMonitorDataModel.FUNCTION_LIST)).contains("19"));
        Assert.assertFalse(((String)t.getValueAt(0,CbusSlotMonitorDataModel.FUNCTION_LIST)).contains("20"));
        
        m.setElement(2, 5); // Function Range
        m.setElement(3, 0xff); // Data
        t.message(m);
        Assert.assertTrue(((String)t.getValueAt(0,CbusSlotMonitorDataModel.FUNCTION_LIST)).contains("21"));
        Assert.assertTrue(((String)t.getValueAt(0,CbusSlotMonitorDataModel.FUNCTION_LIST)).contains("22"));
        Assert.assertTrue(((String)t.getValueAt(0,CbusSlotMonitorDataModel.FUNCTION_LIST)).contains("23"));
        Assert.assertTrue(((String)t.getValueAt(0,CbusSlotMonitorDataModel.FUNCTION_LIST)).contains("24"));
        Assert.assertTrue(((String)t.getValueAt(0,CbusSlotMonitorDataModel.FUNCTION_LIST)).contains("25"));
        Assert.assertTrue(((String)t.getValueAt(0,CbusSlotMonitorDataModel.FUNCTION_LIST)).contains("26"));
        Assert.assertTrue(((String)t.getValueAt(0,CbusSlotMonitorDataModel.FUNCTION_LIST)).contains("27"));
        Assert.assertTrue(((String)t.getValueAt(0,CbusSlotMonitorDataModel.FUNCTION_LIST)).contains("28"));
        
        m.setElement(3, 0x00); // Data
        t.message(m);
        Assert.assertFalse(((String)t.getValueAt(0,CbusSlotMonitorDataModel.FUNCTION_LIST)).contains("21"));
        Assert.assertFalse(((String)t.getValueAt(0,CbusSlotMonitorDataModel.FUNCTION_LIST)).contains("22"));
        Assert.assertFalse(((String)t.getValueAt(0,CbusSlotMonitorDataModel.FUNCTION_LIST)).contains("23"));
        Assert.assertFalse(((String)t.getValueAt(0,CbusSlotMonitorDataModel.FUNCTION_LIST)).contains("24"));
        Assert.assertFalse(((String)t.getValueAt(0,CbusSlotMonitorDataModel.FUNCTION_LIST)).contains("25"));
        Assert.assertFalse(((String)t.getValueAt(0,CbusSlotMonitorDataModel.FUNCTION_LIST)).contains("26"));
        Assert.assertFalse(((String)t.getValueAt(0,CbusSlotMonitorDataModel.FUNCTION_LIST)).contains("27"));
        Assert.assertFalse(((String)t.getValueAt(0,CbusSlotMonitorDataModel.FUNCTION_LIST)).contains("28"));
        
        
        
        m = new CanMessage( tcis.getCanid() );
        m.setNumDataElements(4);
        m.setElement(0, CbusConstants.CBUS_DFNON); 
        m.setElement(1, 0x01); // session 1
        m.setElement(2, 23); // Function
        t.message(m);
        Assert.assertTrue(((String)t.getValueAt(0,CbusSlotMonitorDataModel.FUNCTION_LIST)).contains("23"));
        m.setElement(0, CbusConstants.CBUS_DFNOF);
        t.message(m);
        Assert.assertFalse(((String)t.getValueAt(0,CbusSlotMonitorDataModel.FUNCTION_LIST)).contains("23"));
        
        
        r = new CanReply();
        r.setHeader(tcis.getCanid());
        r.setNumDataElements(2);
        r.setElement(0, CbusConstants.CBUS_DFNON); 
        r.setElement(1, 0x01); // session 1
        r.setElement(2, 13); // Function
        t.reply(r);
        Assert.assertTrue(((String)t.getValueAt(0,CbusSlotMonitorDataModel.FUNCTION_LIST)).contains("13"));
        r.setElement(0, CbusConstants.CBUS_DFNOF);
        t.reply(r);
        Assert.assertFalse(((String)t.getValueAt(0,CbusSlotMonitorDataModel.FUNCTION_LIST)).contains("13"));
        
        
        r = new CanReply();
        r.setHeader(tcis.getCanid());
        r.setNumDataElements(2);
        r.setElement(0, CbusConstants.CBUS_KLOC); 
        r.setElement(1, 0x01); // session 1
        t.reply(r);
        
        Assert.assertEquals("Session Unset CanReply",0, t.getValueAt(0,CbusSlotMonitorDataModel.SESSION_ID_COLUMN) );
        
    }
    
    @Test
    public void testErrors() {
        // table hears session 1 already in progress
        CanReply r = new CanReply();
        r.setHeader(tcis.getCanid());
        r.setNumDataElements(2);
        r.setElement(0, CbusConstants.CBUS_DKEEP); 
        r.setElement(1, 0x01);
        t.reply(r);
        
        Assert.assertEquals("QLOC Message sent to request session 1 details", "[5f8] 22 01",
            tcis.outbound.elementAt(tcis.outbound.size() - 1).toString());
        
        // Command station responds with details for session 1
        r = new CanReply();
        r.setHeader(tcis.getCanid());
        r.setNumDataElements(8);
        r.setElement(0, CbusConstants.CBUS_PLOC);
        r.setElement(1, 0x01); // session 1
        r.setElement(2, 0xc3);
        r.setElement(3, 0x09);
        r.setElement(4, 0x00); // speed direction
        r.setElement(5, 0x00); // function f0 to f4
        r.setElement(6, 0x00); // function f5 to f8
        r.setElement(7, 0x00); // function f9 to f12
        t.reply(r);
    
        Assert.assertEquals("loco 777",777,t.getValueAt(0,CbusSlotMonitorDataModel.LOCO_ID_COLUMN) );
        Assert.assertEquals("Long",true, t.getValueAt(0,CbusSlotMonitorDataModel.LOCO_ID_LONG_COLUMN) );
        Assert.assertEquals("speed 0",0,t.getValueAt(0,CbusSlotMonitorDataModel.LOCO_COMMANDED_SPEED_COLUMN) );
        Assert.assertEquals("speed step 128","128",t.getValueAt(0,CbusSlotMonitorDataModel.SPEED_STEP_COLUMN) );
        
        r = new CanReply();
        r.setHeader(tcis.getCanid());
        r.setNumDataElements(3);
        r.setElement(0, CbusConstants.CBUS_STMOD);
        r.setElement(1, 0x01); // session 1
        r.setElement(2, 0b0000_1111); // bit 0 & 1 & 2 & 3 set
        t.reply(r);
        Assert.assertEquals("speed step 28","28",t.getValueAt(0,CbusSlotMonitorDataModel.SPEED_STEP_COLUMN) );

        r.setElement(2, 0b0000_0010); // bit 1 set
        t.reply(r);
        Assert.assertEquals("speed step 14","14",t.getValueAt(0,CbusSlotMonitorDataModel.SPEED_STEP_COLUMN) );
        
        r.setElement(2, 0b0000_0001); // bit 0 set
        t.reply(r);
        Assert.assertEquals("speed step 28I","28I",t.getValueAt(0,CbusSlotMonitorDataModel.SPEED_STEP_COLUMN) );
        
        CanMessage m = new CanMessage( tcis.getCanid() );
        m.setNumDataElements(3);
        m.setElement(0, CbusConstants.CBUS_STMOD); 
        m.setElement(1, 0x01); // session 1
        m.setElement(2, 0b0000_0000); // All bits unset
        t.message(m);
        Assert.assertEquals("speed step 128","128",t.getValueAt(0,CbusSlotMonitorDataModel.SPEED_STEP_COLUMN) );
        
        Assert.assertEquals("feedb","",t.tablefeedback().getText() );
        
        // command station error reporting
        r = new CanReply();
        r.setHeader(tcis.getCanid());
        r.setNumDataElements(4);
        r.setElement(0, CbusConstants.CBUS_ERR);
        r.setElement(1, 0x01); // session 1
        r.setElement(2, 0); // error byte 2
        r.setElement(3, 8); // error byte 3 session cancelled
        t.reply(r);
        
        Assert.assertEquals("Session cancelled err CanReply",0, t.getValueAt(0,CbusSlotMonitorDataModel.SESSION_ID_COLUMN) );
        
      //  Assert.assertEquals("feedb","",t.tablefeedback().getText() );
        
        Assert.assertTrue(t.tablefeedback().getText().contains("Throttle cancelled for session 1"));
        
        r = new CanReply();
        r.setHeader(tcis.getCanid());
        r.setNumDataElements(8);
        r.setElement(0, CbusConstants.CBUS_PLOC);
        r.setElement(1, 0x01); // session 1
        r.setElement(2, 0xc3);
        r.setElement(3, 0x09);
        r.setElement(4, 0x00); // speed direction
        r.setElement(5, 0x00); // function f0 to f4
        r.setElement(6, 0x00); // function f5 to f8
        r.setElement(7, 0x00); // function f9 to f12
        t.reply(r);
        Assert.assertEquals("Session resumed CanReply",1, t.getValueAt(0,CbusSlotMonitorDataModel.SESSION_ID_COLUMN) );
        
        r = new CanReply();
        r.setHeader(tcis.getCanid());
        r.setNumDataElements(4);
        r.setElement(0, CbusConstants.CBUS_ERR);
        r.setElement(1, 0xc3); // error byte 1
        r.setElement(2, 0x09); // error byte 2
        r.setElement(3, 1); // error byte 3 loco stack full
        t.reply(r);
        Assert.assertTrue(t.tablefeedback().getText().contains("Loco stack full for address 777"));
        
        t.tablefeedback().setText("");
        r.setElement(3, 2); // error byte 3 loco address taken
        t.reply(r);
        Assert.assertTrue(t.tablefeedback().getText().contains("Loco address 777 taken"));
        
        
        t.tablefeedback().setText("");
        r.setElement(3, 3); // error byte 3 session not present
        t.reply(r);
        Assert.assertTrue(t.tablefeedback().getText().contains("Session 195 not present"));
        
        t.tablefeedback().setText("");
        r.setElement(3, 4); // error byte 3 consist empty
        t.reply(r);
        Assert.assertTrue(t.tablefeedback().getText().contains("Consist empty for consist 195"));
        
        t.tablefeedback().setText("");
        r.setElement(3, 5); // error byte 3 loco not found
        t.reply(r);
        Assert.assertTrue(t.tablefeedback().getText().contains("Loco not found for session 195"));
        
        t.tablefeedback().setText("");
        r.setElement(3, 6); // error byte 3 CAN bus error
        t.reply(r);
        Assert.assertTrue(t.tablefeedback().getText().contains("CAN bus error"));
        
        t.tablefeedback().setText("");
        r.setElement(3, 7); // error byte 3 Invalid request
        t.reply(r);
        Assert.assertTrue(t.tablefeedback().getText().contains("Invalid request for address 777"));
    
    }
    
    @Before
    public void setUp() {
        JUnitUtil.setUp();
        tcis = new jmri.jmrix.can.TrafficControllerScaffold();
        memo = new jmri.jmrix.can.CanSystemConnectionMemo();
        memo.setTrafficController(tcis);
        t = new CbusSlotMonitorDataModel(memo,5,5);
    }

    @After
    public void tearDown() {
        t.dispose();
        t = null;
        memo.dispose();
        memo=null;
        tcis.terminateThreads();
        tcis=null;
        JUnitUtil.tearDown();    
    }

}
