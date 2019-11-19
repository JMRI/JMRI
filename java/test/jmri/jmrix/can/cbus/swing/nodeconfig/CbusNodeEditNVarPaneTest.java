package jmri.jmrix.can.cbus.swing.nodeconfig;

import java.awt.GraphicsEnvironment;
import jmri.jmrix.can.CanSystemConnectionMemo;
import jmri.jmrix.can.TrafficControllerScaffold;
import jmri.jmrix.can.cbus.CbusPreferences;
import jmri.jmrix.can.cbus.node.CbusNode;
import jmri.jmrix.can.cbus.node.CbusNodeTableDataModel;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;
import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.JFrameOperator;

/**
 * Test simple functioning of CbusNodeEditNVarFrame
 *
 * @author Paul Bender Copyright (C) 2016 2019
 * @author Steve Young Copyright (C) 2019
 */
public class CbusNodeEditNVarPaneTest {
    
    @Test
    public void testCtorWithMain() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        NodeConfigToolPane mainpane = new NodeConfigToolPane();
        
        t = new CbusNodeEditNVarPane(mainpane);
        Assert.assertNotNull("exists",t);
        
        mainpane = null;
    }
    
    @Test
    public void testInit() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        NodeConfigToolPane mainpane = new NodeConfigToolPane();
        
        CbusNodeTableDataModel nodeModel = new CbusNodeTableDataModel(memo, 3,CbusNodeTableDataModel.MAX_COLUMN);
        jmri.InstanceManager.setDefault(CbusNodeTableDataModel.class,nodeModel );
        jmri.InstanceManager.setDefault(jmri.jmrix.can.cbus.CbusPreferences.class,new CbusPreferences() );
        
        mainpane.initComponents(memo);
        
        
        t = new CbusNodeEditNVarPane(mainpane);
        Assert.assertNotNull("exists",t);
        
        t.initComponents(memo);
        
        CbusNode nodeToEdit = nodeModel.provideNodeByNodeNum(256);
        // set node to 3 node vars , param6
        nodeToEdit.setParameters(new int[]{8,1,2,3,4,5,3,7,8});
        
        t.setNode( nodeToEdit );
        
        nodeToEdit.dispose();
        nodeToEdit = null;
        
        mainpane = null;
    }

    private CanSystemConnectionMemo memo;
    private TrafficControllerScaffold tcis;
    private CbusNodeEditNVarPane t;

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
    }

    // private final static Logger log = LoggerFactory.getLogger(CbusNodeEditNVarFrameTest.class);

}
