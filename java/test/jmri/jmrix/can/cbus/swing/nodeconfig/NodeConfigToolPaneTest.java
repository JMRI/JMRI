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
import org.netbeans.jemmy.operators.JFrameOperator;

/**
 * Test simple functioning of NodeConfigToolPane
 *
 * @author Paul Bender Copyright (C) 2016
 * @author Steve Young Copyright (C) 2019
 */
public class NodeConfigToolPaneTest {

    @Test
    public void testCtor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        NodeConfigToolPane t = new NodeConfigToolPane();
        Assert.assertNotNull("exists", t);
        
        t.dispose();
        t = null;
    }

    @Test
    public void testInitComp() {
        
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        
        CbusNodeTableDataModel nodeModel = new CbusNodeTableDataModel(memo, 3,CbusNodeTableDataModel.MAX_COLUMN);
        jmri.InstanceManager.setDefault(CbusNodeTableDataModel.class,nodeModel );
        
        jmri.InstanceManager.setDefault(jmri.jmrix.can.cbus.CbusPreferences.class,new CbusPreferences() );
        
        NodeConfigToolPane t = new NodeConfigToolPane();
        t.initComponents(memo);
        
        Assert.assertNotNull("exists", t);
        
        // for Jemmy to work, we need the pane inside of a frame
        JmriJFrame f = new JmriJFrame();
        f.add(t);
        if (t.getTitle() != null) {
            f.setTitle(t.getTitle());
        }
        f.pack();
        f.setVisible(true);
        
        // Find new window by name
        JFrameOperator jfo = new JFrameOperator( t.getTitle() );
        
        // Ask to close window
        jfo.requestClose();
        
        nodeModel.dispose();
        nodeModel = null;
        
        jfo = null;
        t = null;

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

}
