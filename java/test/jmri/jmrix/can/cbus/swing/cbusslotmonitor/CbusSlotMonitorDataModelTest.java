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
 
    jmri.jmrix.can.TrafficControllerScaffold tcis = null;
    jmri.jmrix.can.CanSystemConnectionMemo memo = null;

    @Test
    public void testCtor() {
        CbusSlotMonitorDataModel model = new CbusSlotMonitorDataModel(memo,5,5);
        Assert.assertNotNull("exists", model);
        model.dispose();
        model = null;
    }
    
    @Test
    public void testAddToTable() {
        
        CbusSlotMonitorDataModel t = new CbusSlotMonitorDataModel(
        memo,5,CbusSlotMonitorDataModel.MAX_COLUMN);
        
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
        
        t.dispose();
        t = null;
        
    }
    
    @Test
    public void testCanListenAndRemove() {
        Assert.assertEquals("no listener to start with",0,tcis.numListeners());
        CbusSlotMonitorDataModel model = new CbusSlotMonitorDataModel(
        memo,5,CbusSlotMonitorDataModel.MAX_COLUMN);
        Assert.assertTrue("table listening",1 == tcis.numListeners());
        model.dispose();
        Assert.assertTrue("no listener to finish with",0 == tcis.numListeners());
    }
    
    
    
    
    
    

    @Before
    public void setUp() {
        JUnitUtil.setUp();
        tcis = new jmri.jmrix.can.TrafficControllerScaffold();
        memo = new jmri.jmrix.can.CanSystemConnectionMemo();
        memo.setTrafficController(tcis);

    }

    @After
    public void tearDown() {
        JUnitUtil.tearDown();    
    }

}
