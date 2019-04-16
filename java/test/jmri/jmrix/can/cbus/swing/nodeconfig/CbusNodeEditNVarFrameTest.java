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
public class CbusNodeEditNVarFrameTest extends jmri.util.JmriJFrameTestBase {
    
    @Test
    public void testCtorWithMain() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        NodeConfigToolPane mainpane = new NodeConfigToolPane();
        
        CbusNodeEditNVarFrame t = new CbusNodeEditNVarFrame(mainpane);
        Assert.assertNotNull("exists",t);
        
        mainpane = null;
        t = null;
    }
    
    @Test
    public void testInit() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        NodeConfigToolPane mainpane = new NodeConfigToolPane();
        
        CbusNodeTableDataModel nodeModel = new CbusNodeTableDataModel(memo, 3,CbusNodeTableDataModel.MAX_COLUMN);
        jmri.InstanceManager.setDefault(CbusNodeTableDataModel.class,nodeModel );
        jmri.InstanceManager.setDefault(jmri.jmrix.can.cbus.CbusPreferences.class,new CbusPreferences() );
        
        mainpane.initComponents(memo);
        
        
        CbusNodeEditNVarFrame t = new CbusNodeEditNVarFrame(mainpane);
        Assert.assertNotNull("exists",t);
        
        t.initComponents(memo);
        
        CbusNode nodeToEdit = nodeModel.provideNodeByNodeNum(256);
        // set node to 3 node vars , param6
        nodeToEdit.setParameters(new int[]{8,1,2,3,4,5,3,7,8});
        
        
        t.setNode( nodeToEdit );
        
        
        JFrameOperator jfo = new JFrameOperator( t.getTitle() );
        
        Assert.assertFalse(getResetButtonEnabled(jfo));
        
        jfo.requestClose();
        
        jfo = null;
        nodeToEdit.dispose();
        nodeToEdit = null;
        
        mainpane = null;
        t = null;
    }
    
    private boolean getResetButtonEnabled( JFrameOperator jfo ){
        return ( new JButtonOperator(jfo,Bundle.getMessage("Reset")).isEnabled() );
    }

    CanSystemConnectionMemo memo;
    TrafficControllerScaffold tcis;

    @Before
    @Override
    public void setUp() {
        JUnitUtil.setUp();
        memo = new CanSystemConnectionMemo();
        tcis = new TrafficControllerScaffold();
        memo.setTrafficController(tcis);
        if(!GraphicsEnvironment.isHeadless()){
           frame = new CbusNodeEditNVarFrame(null);
        }
    }

    @After
    @Override
    public void tearDown() {

        memo = null;
        tcis = null;
        super.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(CbusNodeEditNVarFrameTest.class);

}
