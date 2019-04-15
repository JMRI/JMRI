package jmri.jmrix.can.cbus.swing.nodeconfig;

import java.awt.GraphicsEnvironment;
import jmri.jmrix.can.CanSystemConnectionMemo;
import jmri.jmrix.can.cbus.CbusPreferences;
import jmri.jmrix.can.cbus.node.CbusNodeTableDataModel;
import jmri.util.JmriJFrame;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;
import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.JFrameOperator;

/**
 * Test simple functioning of CbusNodeEditEventFrame
 *
 * @author Paul Bender Copyright (C) 2016
 * @author Steve Young Copyright (C) 2019
 */
public class CbusNodeRestoreFcuFrameTest {

    @Test
    public void testCtor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        CbusNodeRestoreFcuFrame t = new CbusNodeRestoreFcuFrame(null);
        Assert.assertNotNull("exists",t);
    }
    
    @Test
    public void testInit() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        
        CbusNodeTableDataModel nodeModel = new CbusNodeTableDataModel(memo, 3,CbusNodeTableDataModel.MAX_COLUMN);
        jmri.InstanceManager.setDefault(CbusNodeTableDataModel.class,nodeModel );
        
        jmri.InstanceManager.setDefault(jmri.jmrix.can.cbus.CbusPreferences.class,new CbusPreferences() );
        
        NodeConfigToolPane mainpane = new NodeConfigToolPane();
        mainpane.initComponents(memo);
        Assert.assertNotNull("menu items initialised so restore frame can disable",mainpane.getMenus());
        
        CbusNodeRestoreFcuFrame t = new CbusNodeRestoreFcuFrame(mainpane);
        Assert.assertNotNull("exists",t);
        
        t.initComponents(memo);
        
        Assert.assertEquals("title",Bundle.getMessage("FcuImportTitle"),t.getTitle());
        
        JFrameOperator jfo = new JFrameOperator( t.getTitle() );
        
        Assert.assertFalse(getTeachNodeButtonEnabled(jfo));
        
        // Ask to close window
        jfo.requestClose();
        
        jfo = null;
        t = null;
        
        nodeModel.dispose();
        nodeModel = null;
        mainpane.dispose();
        mainpane = null;
        
    }
    
    private boolean getTeachNodeButtonEnabled( JFrameOperator jfo ){
        return ( new JButtonOperator(jfo,Bundle.getMessage("UpdateNodeButton")).isEnabled() );
    }
    
    CanSystemConnectionMemo memo;

    @Before
    public void setUp() {
        JUnitUtil.setUp();
        memo = new CanSystemConnectionMemo();
    }

    @After
    public void tearDown() {
        
        memo = null;
        
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(CbusNodeRestoreFcuFrameTest.class);

}
