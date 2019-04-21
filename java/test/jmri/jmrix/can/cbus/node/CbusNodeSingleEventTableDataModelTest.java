package jmri.jmrix.can.cbus.node;

import jmri.jmrix.can.CanSystemConnectionMemo;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 * @author Steve Young Copyright (C) 2019
 */
public class CbusNodeSingleEventTableDataModelTest {

    @Test
    public void testCTor() {
        
        CbusNodeEvent ev = new CbusNodeEvent(0,1,0,0,0);
        CbusNodeSingleEventTableDataModel t = new CbusNodeSingleEventTableDataModel(
            new CanSystemConnectionMemo(), 3,CbusNodeSingleEventTableDataModel.MAX_COLUMN,ev);
        
        Assert.assertNotNull("exists",t);
        
        ev = null;
        t = null;
        
    }
    
    @Test
    public void testNoVars() {
        
        CbusNodeEvent evNoRows = new CbusNodeEvent(0,1,0,0,0);
        
        CbusNodeSingleEventTableDataModel t = new CbusNodeSingleEventTableDataModel(
            new CanSystemConnectionMemo(), 3,CbusNodeSingleEventTableDataModel.MAX_COLUMN,evNoRows);
        
        Assert.assertTrue( t.getRowCount()== 0 );
        Assert.assertTrue( t.getColumnCount()== 7 );
        Assert.assertTrue("isTableLoaded t false", t.isTableLoaded()== false );
        
        for (int i = 0; i <t.getColumnCount(); i++) {
            Assert.assertFalse("column has name", t.getColumnName(i).isEmpty() );
            Assert.assertTrue("column has a width", CbusNodeSingleEventTableDataModel.getPreferredWidth(i) > 0 );
        }
        
        Assert.assertTrue("column has NO name", t.getColumnName(999).equals("unknown 999") );
        Assert.assertTrue("column has NO width", CbusNodeSingleEventTableDataModel.getPreferredWidth(999) > 0 );
            
        t = null;
        evNoRows = null;
    }

    @Test
    public void testThreeVars() {
        
        CbusNodeEvent evThreeRows = new CbusNodeEvent(0,1,0,0,3);
        evThreeRows.setEvArr( new int[]{0,2,255});
        
        CbusNodeSingleEventTableDataModel t = new CbusNodeSingleEventTableDataModel(
            new CanSystemConnectionMemo(), 3,CbusNodeSingleEventTableDataModel.MAX_COLUMN,evThreeRows);
    
        Assert.assertTrue( t.getRowCount()== 3 );
        
        Assert.assertTrue("cell not editable", 
            t.isCellEditable(0,CbusNodeSingleEventTableDataModel.EV_SELECT_BIT_COLUMN) == false );
        
        Assert.assertTrue("cell editable", 
            t.isCellEditable(0,CbusNodeSingleEventTableDataModel.EV_SELECT_COLUMN) == true );
        
        Assert.assertTrue("column class int", 
            t.getColumnClass(CbusNodeSingleEventTableDataModel.EV_SELECT_COLUMN) == Integer.class );
        
        Assert.assertTrue("column class string", 
            t.getColumnClass(CbusNodeSingleEventTableDataModel.EV_SELECT_BIT_COLUMN) == String.class );
        
        Assert.assertTrue("getValueAt ev number", (Integer)t.getValueAt(
            0,CbusNodeSingleEventTableDataModel.EV_NUMBER_COLUMN)== 1 );
        Assert.assertTrue("getValueAt ev number", (Integer)t.getValueAt(
            1,CbusNodeSingleEventTableDataModel.EV_NUMBER_COLUMN)== 2 );
        Assert.assertTrue("getValueAt ev number", (Integer)t.getValueAt(
            2,CbusNodeSingleEventTableDataModel.EV_NUMBER_COLUMN)== 3 );
        
        Assert.assertTrue("getValueAt EV_CURRENT_VAL_COLUMN 0", (Integer)t.getValueAt(
            0,CbusNodeSingleEventTableDataModel.EV_CURRENT_VAL_COLUMN)== 0 );
        Assert.assertTrue("getValueAt EV_CURRENT_VAL_COLUMN 1", (Integer)t.getValueAt(
            1,CbusNodeSingleEventTableDataModel.EV_CURRENT_VAL_COLUMN)== 2 );
        Assert.assertTrue("getValueAt EV_CURRENT_VAL_COLUMN 2", (Integer)t.getValueAt(
            2,CbusNodeSingleEventTableDataModel.EV_CURRENT_VAL_COLUMN)== 255 );
        
        Assert.assertEquals("hex val 0","0", t.getValueAt(
            0,CbusNodeSingleEventTableDataModel.EV_CURRENT_HEX_COLUMN));
        Assert.assertEquals("hex val 1","2", t.getValueAt(
            1,CbusNodeSingleEventTableDataModel.EV_CURRENT_HEX_COLUMN));
        Assert.assertEquals("hex val 2","ff", t.getValueAt(
            2,CbusNodeSingleEventTableDataModel.EV_CURRENT_HEX_COLUMN));
        
        Assert.assertEquals("EV_CURRENT_BIT_COLUMN 0","0000 0000", t.getValueAt(
            0,CbusNodeSingleEventTableDataModel.EV_CURRENT_BIT_COLUMN));
        Assert.assertEquals("EV_CURRENT_BIT_COLUMN 1","0000 0010", t.getValueAt(
            1,CbusNodeSingleEventTableDataModel.EV_CURRENT_BIT_COLUMN));
        Assert.assertEquals("EV_CURRENT_BIT_COLUMN 2","1111 1111", t.getValueAt(
            2,CbusNodeSingleEventTableDataModel.EV_CURRENT_BIT_COLUMN));
            
        Assert.assertTrue("getValueAt EV_SELECT_COLUMN 0", (Integer)t.getValueAt(
            0,CbusNodeSingleEventTableDataModel.EV_SELECT_COLUMN)== 0 );
        Assert.assertTrue("getValueAt EV_SELECT_COLUMN 1", (Integer)t.getValueAt(
            1,CbusNodeSingleEventTableDataModel.EV_SELECT_COLUMN)== 2 );
        Assert.assertTrue("getValueAt EV_SELECT_COLUMN 2", (Integer)t.getValueAt(
            2,CbusNodeSingleEventTableDataModel.EV_SELECT_COLUMN)== 255 );
            
        Assert.assertEquals("EV_SELECT_HEX_COLUMN val 0","", t.getValueAt(
            0,CbusNodeSingleEventTableDataModel.EV_SELECT_HEX_COLUMN));
        Assert.assertEquals("EV_SELECT_HEX_COLUMN 1","", t.getValueAt(
            1,CbusNodeSingleEventTableDataModel.EV_SELECT_HEX_COLUMN));
        Assert.assertEquals("EV_SELECT_HEX_COLUMN 2","", t.getValueAt(
            2,CbusNodeSingleEventTableDataModel.EV_SELECT_HEX_COLUMN));
        
        Assert.assertEquals("EV_SELECT_BIT_COLUMN 0","", t.getValueAt(
            0,CbusNodeSingleEventTableDataModel.EV_SELECT_BIT_COLUMN));
        Assert.assertEquals("EV_SELECT_BIT_COLUMN 1","", t.getValueAt(
            1,CbusNodeSingleEventTableDataModel.EV_SELECT_BIT_COLUMN));
        Assert.assertEquals("EV_SELECT_BIT_COLUMN 2","", t.getValueAt(
            2,CbusNodeSingleEventTableDataModel.EV_SELECT_BIT_COLUMN));        
        
        Assert.assertTrue("isTableDirty t false", t.isTableDirty()== false );
        Assert.assertTrue("isTableLoaded t true", t.isTableLoaded()== true );
        Assert.assertTrue("getCountDirty 0 ", t.getCountDirty()== 0 );
        
        t.setValueAt(211,1,CbusNodeSingleEventTableDataModel.EV_SELECT_COLUMN);
        
        Assert.assertTrue("getValueAt EV_SELECT_COLUMN edit 1", (Integer)t.getValueAt(
            1,CbusNodeSingleEventTableDataModel.EV_SELECT_COLUMN)== 211 );
        Assert.assertEquals("EV_SELECT_HEX_COLUMN edit 1","d3", t.getValueAt(
            1,CbusNodeSingleEventTableDataModel.EV_SELECT_HEX_COLUMN));
        Assert.assertEquals("EV_SELECT_BIT_COLUMN edit 1","1101 0011", t.getValueAt(
            1,CbusNodeSingleEventTableDataModel.EV_SELECT_BIT_COLUMN));
        
        Assert.assertTrue("isSingleEvDirty 0 false", t.isSingleEvDirty(0)== false );
        Assert.assertTrue("isSingleEvDirty 1 true", t.isSingleEvDirty(1)== true );
        Assert.assertTrue("getCountDirty 1 ", t.getCountDirty()== 1 );
        
        Assert.assertTrue("isTableDirty t true", t.isTableDirty()== true );
        
        t.resetnewEVs();
        
        Assert.assertTrue("resetnewEVs EV_SELECT_COLUMN ", (Integer)t.getValueAt(
            1,CbusNodeSingleEventTableDataModel.EV_SELECT_COLUMN)== 2 );
        Assert.assertTrue("isTableDirty aftr reset false", t.isTableDirty()== false );
        
        t.setValueAt(211,1,CbusNodeSingleEventTableDataModel.EV_CURRENT_VAL_COLUMN);
        Assert.assertTrue("cell not editable ", (Integer)t.getValueAt(
            1,CbusNodeSingleEventTableDataModel.EV_CURRENT_VAL_COLUMN)== 2 );
        
        Assert.assertTrue("getValueAt 999", t.getValueAt(0,999) == null );
        
        evThreeRows = null;
        t = null;
    
    }
    
    @Test
    public void testLoading() {
        
        CbusNodeEvent evThreeRows = new CbusNodeEvent(0,1,0,0,3);
        evThreeRows.setEvArr( new int[]{-1,-1,-1});
        
        CbusNodeSingleEventTableDataModel t = new CbusNodeSingleEventTableDataModel(
            new CanSystemConnectionMemo(), 3,CbusNodeSingleEventTableDataModel.MAX_COLUMN,evThreeRows);    
    
        Assert.assertTrue("isTableLoaded fal 1", t.isTableLoaded()== false );
        
        Assert.assertEquals("hex val -1",-1, t.getValueAt(
            0,CbusNodeSingleEventTableDataModel.EV_CURRENT_HEX_COLUMN));
        Assert.assertEquals("bit val -1",-1, t.getValueAt(
            0,CbusNodeSingleEventTableDataModel.EV_CURRENT_BIT_COLUMN));
        Assert.assertTrue("select val -1",(Integer) t.getValueAt(
            0,CbusNodeSingleEventTableDataModel.EV_SELECT_COLUMN) == -1 );
            
            
            
    
        evThreeRows = null;
        t = null;
    
    }
    

    // The minimal setup for log4J
    @Before
    public void setUp() {
        JUnitUtil.setUp();
    }

    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(CbusNodeSingleEventTableDataModelTest.class);

}
