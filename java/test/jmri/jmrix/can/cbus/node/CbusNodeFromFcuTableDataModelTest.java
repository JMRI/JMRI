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
public class CbusNodeFromFcuTableDataModelTest {

    @Test
    public void testCTor() {
        
        CbusNodeFromFcuTableDataModel t = new CbusNodeFromFcuTableDataModel(
            new CanSystemConnectionMemo(), 3,CbusNodeFromFcuTableDataModel.MAX_COLUMN);
        
        Assert.assertNotNull("exists",t);
        
        t.dispose();
        t = null;
        
    }
    
    @Test
    public void testDefaults() {
        
        CanSystemConnectionMemo memo = new CanSystemConnectionMemo();
        TrafficControllerScaffold tcis = new TrafficControllerScaffold();
        memo.setTrafficController(tcis);
        
        CbusNodeFromFcuTableDataModel t = new CbusNodeFromFcuTableDataModel(
            memo, 3,CbusNodeFromFcuTableDataModel.MAX_COLUMN);
        
        for (int i = 0; i <t.getColumnCount(); i++) {
            Assert.assertFalse("column has name", t.getColumnName(i).isEmpty() );
            Assert.assertTrue("column has a width", CbusNodeFromFcuTableDataModel.getPreferredWidth(i) > 0 );
        }
        
        Assert.assertTrue("column has NO name", t.getColumnName(999).equals("unknown 999") );
        Assert.assertTrue("column has NO width", CbusNodeFromFcuTableDataModel.getPreferredWidth(999) > 0 );
        
        Assert.assertTrue("cell not editable", 
            t.isCellEditable(0,CbusNodeFromFcuTableDataModel.NODE_NUMBER_COLUMN) == false );
        
        Assert.assertTrue("column class int", 
            t.getColumnClass(CbusNodeFromFcuTableDataModel.NODE_EVENTS_COLUMN) == Integer.class );
        
        Assert.assertTrue("column class string", 
            t.getColumnClass(CbusNodeFromFcuTableDataModel.NODE_USER_NAME_COLUMN) == String.class );
            
        Assert.assertTrue("column class null", t.getColumnClass(999) == null );
        
        Assert.assertTrue("default getNodeByNodeNum 1234",t.getNodeByNodeNum(1234) == null);
        Assert.assertTrue("default getNodeRowFromNodeNum 1234",t.getNodeRowFromNodeNum(1234) == -1 );
        Assert.assertTrue("default getRowCount 0",t.getRowCount() == 0 );
        
        t.provideNodeByNodeNum(1234);
        
        Assert.assertTrue("default getNodeByNodeNum 1234",t.getNodeByNodeNum(1234) != null);
        Assert.assertTrue("default getNodeRowFromNodeNum 1234",t.getNodeRowFromNodeNum(1234) == 0 );
        Assert.assertTrue("default getRowCount 0",t.getRowCount() == 1 );
        
        t.dispose();
        t = null;
        tcis = null;
        memo = null;
    }

    @Test
    public void testLoaded() {
        
        CanSystemConnectionMemo memo = new CanSystemConnectionMemo();
        TrafficControllerScaffold tcis = new TrafficControllerScaffold();
        memo.setTrafficController(tcis);
        
        CbusNodeFromFcuTableDataModel t = new CbusNodeFromFcuTableDataModel(
            memo, 3,CbusNodeFromFcuTableDataModel.MAX_COLUMN);
        
        CbusNodeFromBackup myNode = new CbusNodeFromBackup(memo,256);    
        
        // set node to 4 ev vars per event, para 5, 3 NV's, param 6
        myNode.setParameters(new int[]{7,1,2,3,4,4,3,7});
        
        myNode.setNV(1,1);
        myNode.setNV(2,2);
        myNode.setNV(3,3);
        
        t.addNode(myNode);
        
        Assert.assertTrue(" getNodeByNodeNum 256",t.getNodeByNodeNum(256) != null);
        Assert.assertTrue(" getNodeRowFromNodeNum 256",t.getNodeRowFromNodeNum(256) == 0 );
        Assert.assertTrue(" getRowCount 0",t.getRowCount() == 1 );
        
        Assert.assertTrue("getValueAt fcu node", (Integer)t.getValueAt(0,CbusNodeFromFcuTableDataModel.NODE_NUMBER_COLUMN)== 256 );
        Assert.assertTrue("getValueAt fcu user nm",(String)t.getValueAt(0,CbusNodeFromFcuTableDataModel.NODE_USER_NAME_COLUMN)=="" );
        Assert.assertTrue("getValueAt fcu type nm",(String)t.getValueAt(0,CbusNodeFromFcuTableDataModel.NODE_TYPE_NAME_COLUMN)=="" );
        Assert.assertTrue("getValueAt fcu ev",(Integer)t.getValueAt(0,CbusNodeFromFcuTableDataModel.NODE_EVENTS_COLUMN)== -1 );
        Assert.assertTrue("getValueAt fcu tot bytes",(Integer)t.getValueAt(0,CbusNodeFromFcuTableDataModel.NODE_TOTAL_BYTES_COLUMN)== -1 );
        Assert.assertTrue("getValueAt fcu nv tot",(Integer)t.getValueAt(0,CbusNodeFromFcuTableDataModel.NODE_NV_TOTAL_COLUMN)== 3 );
      
      
        t.setValueAt(7,0,CbusNodeFromFcuTableDataModel.NODE_NV_TOTAL_COLUMN);
        Assert.assertTrue("setValueAt does nothing",(Integer)t.getValueAt(0,CbusNodeFromFcuTableDataModel.NODE_NV_TOTAL_COLUMN)== 3 );
        
        t.dispose();
        t = null;
        
        tcis = null;
        memo = null;
        
    }

    // The minimal setup for log4J
    @Before
    public void setUp() {
        JUnitUtil.setUp();
    }

    @After
    public void tearDown() {
        JUnitUtil.clearShutDownManager(); // put in place because AbstractMRTrafficController implementing subclass was not terminated properly
        JUnitUtil.tearDown();

    }

    // private final static Logger log = LoggerFactory.getLogger(CbusNodeFromFcuTableDataModelTest.class);

}
