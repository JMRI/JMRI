package jmri.jmrix.can.cbus.node;

import jmri.jmrix.can.CanSystemConnectionMemo;
import jmri.jmrix.can.TrafficControllerScaffold;
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
public class CbusNodeNVTableDataModelTest {

    @Test
    public void testCTor() {
        
        t = new CbusNodeNVTableDataModel(
            memo, 3,CbusNodeNVTableDataModel.MAX_COLUMN);
        
        Assert.assertNotNull("exists",t);
        
    }
    
    @Test
    public void testNodeNoNv() {
        
        t = new CbusNodeNVTableDataModel(
            memo, 3,CbusNodeNVTableDataModel.MAX_COLUMN);
        
        Assert.assertEquals( "Column Count after construction", 7,t.getColumnCount() );
        
        CbusNode myNode = new CbusNode(memo,12345);
        
        t.setNode(myNode);
        
        Assert.assertTrue( t.getRowCount()== 0 );
        Assert.assertEquals( "Column Count", 7,t.getColumnCount() );
        
        for (int i = 0; i <t.getColumnCount(); i++) {
            Assert.assertFalse("column has name", t.getColumnName(i).isEmpty() );
        }
        
        Assert.assertTrue("column has NO name", t.getColumnName(999).equals("unknown 999") );
        
        myNode.dispose();
        myNode = null;
        
    }
    
    @Test
    public void testNodeWithNv() {
        
        t = new CbusNodeNVTableDataModel(
            memo, 3,CbusNodeNVTableDataModel.MAX_COLUMN);
        
        CbusNode myNode = new CbusNode(memo,12345);        
        
        // set node to 3 node vars , param6
        myNode.setParameters(new int[]{7,1,2,3,4,5,3,7});
        
        t.setNode(myNode);
        
        Assert.assertTrue( t.getRowCount()== 3 );
        
        Assert.assertTrue("cell not editable", 
            t.isCellEditable(0,CbusNodeNVTableDataModel.NV_NUMBER_COLUMN) == false );
        Assert.assertTrue("cell editable", 
            t.isCellEditable(0,CbusNodeNVTableDataModel.NV_SELECT_COLUMN) == true );
        
        Assert.assertTrue("column class int", 
            t.getColumnClass(CbusNodeNVTableDataModel.NV_NUMBER_COLUMN) == Integer.class );
        Assert.assertTrue("column class string", 
            t.getColumnClass(CbusNodeNVTableDataModel.NV_SELECT_BIT_COLUMN) == String.class );
        
        
        Assert.assertTrue("getValueAt NV_NUMBER_COLUMN number", (Integer)t.getValueAt(
            0,CbusNodeNVTableDataModel.NV_NUMBER_COLUMN)== 1 );
        Assert.assertTrue("getValueAt NV_NUMBER_COLUMN number", (Integer)t.getValueAt(
            1,CbusNodeNVTableDataModel.NV_NUMBER_COLUMN)== 2 );
        Assert.assertTrue("getValueAt NV_NUMBER_COLUMN number", (Integer)t.getValueAt(
            2,CbusNodeNVTableDataModel.NV_NUMBER_COLUMN)== 3 );
        
        
        Assert.assertEquals("NV_CURRENT_HEX_COLUMN","",t.getValueAt( 
            0,CbusNodeNVTableDataModel.NV_CURRENT_HEX_COLUMN) );
        
        Assert.assertEquals("NV_CURRENT_BIT_COLUMN","",t.getValueAt( 
            0,CbusNodeNVTableDataModel.NV_CURRENT_BIT_COLUMN) );
        
        Assert.assertEquals("NV_SELECT_COLUMN",-1,t.getValueAt( 
            0,CbusNodeNVTableDataModel.NV_SELECT_COLUMN) );
        
        Assert.assertEquals("NV_SELECT_HEX_COLUMN","",t.getValueAt( 
            0,CbusNodeNVTableDataModel.NV_SELECT_HEX_COLUMN) );
        
        Assert.assertEquals("NV_SELECT_BIT_COLUMN","",t.getValueAt( 
            0,CbusNodeNVTableDataModel.NV_SELECT_BIT_COLUMN) );
            
        Assert.assertTrue("getValueAt nac", (String)t.getValueAt(0,999) == null );
        
        Assert.assertTrue("isTableDirty nac", t.isTableDirty() == false );
        Assert.assertTrue("getCountDirty nac", t.getCountDirty() == 0 );
        
        t.setValueAt(0,0,CbusNodeNVTableDataModel.NV_SELECT_HEX_COLUMN);
        Assert.assertTrue("isTableDirty no edit", t.isTableDirty() == false );
        
        t.setValueAt(0,0,CbusNodeNVTableDataModel.NV_SELECT_COLUMN);
        t.setValueAt(122,1,CbusNodeNVTableDataModel.NV_SELECT_COLUMN);
        
        Assert.assertTrue("isTableDirty edit 0", t.isTableDirty() == true );
        Assert.assertTrue("getCountDirty 1", t.getCountDirty() == 2 );
        
        t.setValueAt(255,2,CbusNodeNVTableDataModel.NV_SELECT_COLUMN);
        
        Assert.assertTrue("isTableDirty 2", t.isTableDirty() == true );
        
        Assert.assertTrue("getValueAt NV_NUMBER_COLUMN number", (Integer)t.getValueAt(
            0,CbusNodeNVTableDataModel.NV_NUMBER_COLUMN)== 1 );
        Assert.assertTrue("getValueAt NV_NUMBER_COLUMN number", (Integer)t.getValueAt(
            1,CbusNodeNVTableDataModel.NV_NUMBER_COLUMN)== 2 );
        Assert.assertTrue("getValueAt NV_NUMBER_COLUMN number", (Integer)t.getValueAt(
            2,CbusNodeNVTableDataModel.NV_NUMBER_COLUMN)== 3 );
        
        Assert.assertEquals("NV_CURRENT_HEX_COLUMN","",t.getValueAt( 
            0,CbusNodeNVTableDataModel.NV_CURRENT_HEX_COLUMN) );
        Assert.assertEquals("NV_CURRENT_HEX_COLUMN","",t.getValueAt( 
            1,CbusNodeNVTableDataModel.NV_CURRENT_HEX_COLUMN) );
        Assert.assertEquals("NV_CURRENT_HEX_COLUMN","",t.getValueAt( 
            2,CbusNodeNVTableDataModel.NV_CURRENT_HEX_COLUMN) );
        
        Assert.assertEquals("NV_CURRENT_BIT_COLUMN","",t.getValueAt( 
            0,CbusNodeNVTableDataModel.NV_CURRENT_BIT_COLUMN) );
        Assert.assertEquals("NV_CURRENT_BIT_COLUMN","",t.getValueAt( 
            1,CbusNodeNVTableDataModel.NV_CURRENT_BIT_COLUMN) );
        Assert.assertEquals("NV_CURRENT_BIT_COLUMN","",t.getValueAt( 
            2,CbusNodeNVTableDataModel.NV_CURRENT_BIT_COLUMN) );
        
        Assert.assertEquals("NV_SELECT_COLUMN",0,t.getValueAt( 
            0,CbusNodeNVTableDataModel.NV_SELECT_COLUMN) );
        Assert.assertEquals("NV_SELECT_COLUMN",122,t.getValueAt( 
            1,CbusNodeNVTableDataModel.NV_SELECT_COLUMN) );
        Assert.assertEquals("NV_SELECT_COLUMN",255,t.getValueAt( 
            2,CbusNodeNVTableDataModel.NV_SELECT_COLUMN) );
        
        Assert.assertEquals("NV_SELECT_HEX_COLUMN","0",t.getValueAt( 
            0,CbusNodeNVTableDataModel.NV_SELECT_HEX_COLUMN) );
        Assert.assertEquals("NV_SELECT_HEX_COLUMN","7a",t.getValueAt( 
            1,CbusNodeNVTableDataModel.NV_SELECT_HEX_COLUMN) );
        Assert.assertEquals("NV_SELECT_HEX_COLUMN","ff",t.getValueAt( 
            2,CbusNodeNVTableDataModel.NV_SELECT_HEX_COLUMN) );
        
        Assert.assertEquals("NV_SELECT_BIT_COLUMN","0000 0000",t.getValueAt( 
            0,CbusNodeNVTableDataModel.NV_SELECT_BIT_COLUMN) );
        Assert.assertEquals("NV_SELECT_BIT_COLUMN","0111 1010",t.getValueAt( 
            1,CbusNodeNVTableDataModel.NV_SELECT_BIT_COLUMN) );
        Assert.assertEquals("NV_SELECT_BIT_COLUMN","1111 1111",t.getValueAt( 
            2,CbusNodeNVTableDataModel.NV_SELECT_BIT_COLUMN) );
        
        t.resetNewNvs();
        Assert.assertTrue("isTableDirty after reset", t.isTableDirty() == false );
        
        Assert.assertEquals("NV_SELECT_COLUMN after reset",-1,t.getValueAt( 
            1,CbusNodeNVTableDataModel.NV_SELECT_COLUMN) );
        
        t.setValueAt(255,0,CbusNodeNVTableDataModel.NV_SELECT_COLUMN);
        // t.setValueAt(122,1,CbusNodeNVTableDataModel.NV_SELECT_COLUMN);
        // t.setValueAt(255,2,CbusNodeNVTableDataModel.NV_SELECT_COLUMN);
        
        
        myNode.dispose();
        myNode = null;
    
    }
    
    private CanSystemConnectionMemo memo;
    private TrafficControllerScaffold tcis;
    private CbusNodeNVTableDataModel t;
    

    // The minimal setup for log4J
    @Before
    public void setUp() {
        JUnitUtil.setUp();
        
        memo = new CanSystemConnectionMemo();
        tcis = new TrafficControllerScaffold();
        memo.setTrafficController(tcis);
        
    }

    @After
    public void tearDown() {
        t = null;
        memo = null;
        tcis = null;
        
        JUnitUtil.clearShutDownManager(); // put in place because AbstractMRTrafficController implementing subclass was not terminated properly
        JUnitUtil.tearDown();

    }

    // private final static Logger log = LoggerFactory.getLogger(CbusNodeNVTableDataModelTest.class);

}
