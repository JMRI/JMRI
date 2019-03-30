package jmri.jmrix.can.cbus.node;

import java.awt.GraphicsEnvironment;
import jmri.jmrix.can.CanSystemConnectionMemo;
import jmri.jmrix.can.TrafficControllerScaffold;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 * @author Steve Young Copyright (C) 2019
 */
public class CbusNodeEventTableDataModelTest {

    @Test
    public void testCTor() {
        
        CbusNodeEventTableDataModel t = new CbusNodeEventTableDataModel(
            new CanSystemConnectionMemo(), 3,CbusNodeEventTableDataModel.MAX_COLUMN);
        Assert.assertNotNull("exists",t);
        
        t = null;
    }

    @Test
    public void testNodeNoEv() {
        
        CanSystemConnectionMemo memo = new CanSystemConnectionMemo();
        TrafficControllerScaffold tcis = new TrafficControllerScaffold();
        memo.setTrafficController(tcis);
        
        CbusNodeEventTableDataModel t = new CbusNodeEventTableDataModel(
            memo, 3,CbusNodeEventTableDataModel.MAX_COLUMN);
        
        CbusNode myNode = new CbusNode(memo,12345);
        
        t.setNode(myNode);
        
      //  Assert.assertEquals("starting 0 rowcount",0,t.getRowCount() );
        
        Assert.assertTrue( t.getRowCount()== 0 );
        Assert.assertTrue( t.getColumnCount()== 6 );
        
        for (int i = 0; i <t.getColumnCount(); i++) {
            Assert.assertFalse("column has name", t.getColumnName(i).isEmpty() );
            Assert.assertTrue("column has a width", CbusNodeEventTableDataModel.getPreferredWidth(i) > 0 );
        }
        
        Assert.assertTrue("column has NO name", t.getColumnName(999).equals("unknown 999") );
        Assert.assertTrue("column has NO width", CbusNodeEventTableDataModel.getPreferredWidth(999) > 0 );
        
        myNode.dispose();
        myNode = null;
        t = null;
        memo = null;
        tcis = null;
        
    }
    
    
    @Test
    public void testNodeWithNewEv() {
        
        // not headless as setValueAt triggers window open
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        
        CanSystemConnectionMemo memo = new CanSystemConnectionMemo();
        TrafficControllerScaffold tcis = new TrafficControllerScaffold();
        memo.setTrafficController(tcis);
        
        CbusNodeEventTableDataModel t = new CbusNodeEventTableDataModel(
            memo, 3,CbusNodeEventTableDataModel.MAX_COLUMN);
        
        CbusNode myNode = new CbusNode(memo,12345);        
        CbusNodeEvent myNodeEvent = new CbusNodeEvent(3011,7,12345,-1,4);
        myNodeEvent.setEvArr(new int[]{1,2,3,4});
        
        myNode.addNewEvent(myNodeEvent);
        t.setNode(myNode);
        
        Assert.assertTrue( t.getRowCount()== 1 );
        
        Assert.assertTrue("cell not editable", 
            t.isCellEditable(0,CbusNodeEventTableDataModel.NODE_NAME_COLUMN) == false );
        Assert.assertTrue("cell editable", 
            t.isCellEditable(0,CbusNodeEventTableDataModel.NODE_EDIT_BUTTON_COLUMN) == true );
        
        Assert.assertTrue("column class int", 
            t.getColumnClass(CbusNodeEventTableDataModel.EVENT_NUMBER_COLUMN) == Integer.class );
        Assert.assertTrue("column class string", 
            t.getColumnClass(CbusNodeEventTableDataModel.EVENT_NAME_COLUMN) == String.class );
        Assert.assertTrue("button class string", 
            t.getColumnClass(CbusNodeEventTableDataModel.NODE_EDIT_BUTTON_COLUMN) == javax.swing.JButton.class );
        Assert.assertTrue("class unknown", t.getColumnClass(999) == null );
        
        Assert.assertTrue("getValueAt NODE_NUMBER_COLUMN number", (Integer)t.getValueAt(
            0,CbusNodeEventTableDataModel.NODE_NUMBER_COLUMN)== 3011 );
        
        Assert.assertTrue("getValueAt EVENT_NUMBER_COLUMN number", (Integer)t.getValueAt(
            0,CbusNodeEventTableDataModel.EVENT_NUMBER_COLUMN)== 7 );
            
        Assert.assertTrue("getValueAt NODE_EDIT_BUTTON_COLUMN number", (String)t.getValueAt(
            0,CbusNodeEventTableDataModel.NODE_EDIT_BUTTON_COLUMN) != null );
            
        Assert.assertTrue("getValueAt NODE_NAME_COLUMN number", (String)t.getValueAt(
            0,CbusNodeEventTableDataModel.NODE_NAME_COLUMN) == "" );
            
        Assert.assertTrue("getValueAt EVENT_NAME_COLUMN number", (String)t.getValueAt(
            0,CbusNodeEventTableDataModel.EVENT_NAME_COLUMN) == "" );
            
        Assert.assertEquals("starting ev vars","[1, 2, 3, 4]",t.getValueAt( 
            0,CbusNodeEventTableDataModel.EV_VARS_COLUMN) );
            
        Assert.assertTrue("getValueAt nac", (String)t.getValueAt(0,999) == null );            
        
        t.updateFromNode(0,CbusNodeEventTableDataModel.EV_VARS_COLUMN);
        
        CbusNodeTableDataModel nodeModel = new CbusNodeTableDataModel(memo, 3,CbusNodeTableDataModel.MAX_COLUMN);
        jmri.InstanceManager.setDefault(jmri.jmrix.can.cbus.node.CbusNodeTableDataModel.class,nodeModel );
        
        nodeModel.addNode(myNode);
        
        t.setValueAt("doclick",0,CbusNodeEventTableDataModel.NODE_EDIT_BUTTON_COLUMN);
        t.disposeEvFrame();
        try {
            t.disposeEvFrame();
        } catch (Exception e) {
            Assert.assertTrue("edit event frame was successfully disposed of so caused a null exception",true);
        }
        
        t.setValueAt("doclick",0,CbusNodeEventTableDataModel.EVENT_NAME_COLUMN);
        try {
            t.disposeEvFrame();
        } catch (Exception e) {
            Assert.assertTrue("no event frame was created so caused a null exception",true);
        }
        
        nodeModel.dispose();
        nodeModel = null;
        myNode.dispose();
        myNode = null;
        myNodeEvent = null;
        t = null;
        memo = null;
        tcis = null;
        
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

    // private final static Logger log = LoggerFactory.getLogger(CbusNodeEventTableDataModelTest.class);

}
