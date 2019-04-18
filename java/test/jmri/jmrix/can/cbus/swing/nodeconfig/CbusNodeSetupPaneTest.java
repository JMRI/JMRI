package jmri.jmrix.can.cbus.swing.nodeconfig;

import java.awt.GraphicsEnvironment;
import jmri.jmrix.can.CanSystemConnectionMemo;
import jmri.jmrix.can.TrafficControllerScaffold;
import jmri.jmrix.can.cbus.node.CbusNode;
import jmri.jmrix.can.cbus.node.CbusNodeTableDataModel;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;

/**
 * Test simple functioning of CbusNodeSetupPane
 *
 * @author Paul Bender Copyright (C) 2016
 * @author Steve Young Copyright (C) 2019
 */
public class CbusNodeSetupPaneTest {

    @Test
    public void testCtor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        CbusNodeSetupPane t = new CbusNodeSetupPane(null);
        Assert.assertNotNull("exists",t);
    }
    
    @Test
    public void testInit() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        
        CbusNodeTableDataModel nodeModel = new CbusNodeTableDataModel(memo, 3,CbusNodeTableDataModel.MAX_COLUMN);
        jmri.InstanceManager.setDefault(CbusNodeTableDataModel.class,nodeModel );
        CbusNode nodeWithEventToEdit = nodeModel.provideNodeByNodeNum(256);
        // set node to 3 node vars , param6
        nodeWithEventToEdit.setParameters(new int[]{8,1,2,3,4,5,3,7,8});
        
        Assert.assertNotNull("exists",nodeWithEventToEdit);
        
        CbusNodeSetupPane t = new CbusNodeSetupPane(null);
        t.initComponents(256); // node num 256
        
        Assert.assertNotNull("exists",t);
        
        nodeModel.dispose();
        nodeModel = null;
        t = null;
    }
    
    CanSystemConnectionMemo memo;
    TrafficControllerScaffold tcis;

    @Before
    public void setUp() {
        JUnitUtil.setUp();
        
        memo = new CanSystemConnectionMemo();
        tcis = new TrafficControllerScaffold();
        memo.setTrafficController(tcis);
    }

    @After
    public void tearDown() {
        
        memo = null;
        tcis = null;
        
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(CbusNodeSetupPaneTest.class);

}
