package jmri.jmrix.can.cbus.swing.nodeconfig;

import java.awt.GraphicsEnvironment;

import jmri.jmrix.can.CanSystemConnectionMemo;
import jmri.jmrix.can.TrafficControllerScaffold;
import jmri.jmrix.can.cbus.node.CbusNode;
import jmri.jmrix.can.cbus.node.CbusNodeTableDataModel;
import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;
import org.junit.Assume;

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
        Assert.assertNotNull("exists",nodeToEdit);
    }
    
    @Test
    public void testInit() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        
        CbusNodeSetupPane t = new CbusNodeSetupPane(null);
        t.setNode(nodeToEdit); // node num 256
        
        Assert.assertNotNull("exists",t);
        
        nodeModel.dispose();

    }
    
    private CanSystemConnectionMemo memo;
    private TrafficControllerScaffold tcis;
    private CbusNodeTableDataModel nodeModel;
    private CbusNode nodeToEdit;

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetInstanceManager();
        memo = new CanSystemConnectionMemo();
        tcis = new TrafficControllerScaffold();
        memo.setTrafficController(tcis);
        
        nodeModel = new CbusNodeTableDataModel(memo, 3,CbusNodeTableDataModel.MAX_COLUMN);
        jmri.InstanceManager.setDefault(CbusNodeTableDataModel.class,nodeModel );
        nodeToEdit = nodeModel.provideNodeByNodeNum(256);
        // set node to 3 node vars , param6
        nodeToEdit.getNodeParamManager().setParameters(new int[]{8,1,2,3,4,5,3,7,8});
        
    }

    @AfterEach
    public void tearDown() {
        nodeToEdit.dispose();
        nodeModel.dispose();
        tcis.terminateThreads();
        memo.dispose();
        memo = null;
        tcis = null;
        
        JUnitUtil.tearDown();

    }

    // private final static Logger log = LoggerFactory.getLogger(CbusNodeSetupPaneTest.class);

}
