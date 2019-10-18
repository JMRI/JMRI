package jmri.jmrix.can.cbus.swing.nodeconfig;

import java.awt.GraphicsEnvironment;
import jmri.jmrix.can.CanSystemConnectionMemo;
import jmri.jmrix.can.TrafficControllerScaffold;
import jmri.jmrix.can.cbus.CbusPreferences;
import jmri.jmrix.can.cbus.node.CbusNodeTableDataModel;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;

/**
 * Test simple functioning of NodeConfigToolPane
 *
 * @author Paul Bender Copyright (C) 2016
 * @author Steve Young Copyright (C) 2019
 */
public class NodeConfigToolPaneTest extends jmri.util.swing.JmriPanelTest {

    @Test
    public void testInitComp() {
        
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        
        CbusNodeTableDataModel nodeModel = new CbusNodeTableDataModel(memo, 3,CbusNodeTableDataModel.MAX_COLUMN);
        jmri.InstanceManager.setDefault(CbusNodeTableDataModel.class,nodeModel );
        
        jmri.InstanceManager.setDefault(jmri.jmrix.can.cbus.CbusPreferences.class,new CbusPreferences() );
        
        NodeConfigToolPane panel = new NodeConfigToolPane();
        panel.initComponents(memo);
        
        Assert.assertNotNull("exists", panel);
        Assert.assertNotNull("core node model exists", nodeModel);
        
        
        nodeModel.dispose();
        nodeModel = null;

    }
    
    
    private CanSystemConnectionMemo memo;
    private TrafficControllerScaffold tcis;

    @Before
    @Override
    public void setUp() {
        JUnitUtil.setUp();
        memo = new CanSystemConnectionMemo();
        tcis = new TrafficControllerScaffold();
        memo.setTrafficController(tcis);
        
        panel = new NodeConfigToolPane();
        title = Bundle.getMessage("MenuItemNodeConfig");
        helpTarget = "package.jmri.jmrix.can.cbus.swing.nodeconfig.NodeConfigToolPane";
    }

    @After
    @Override
    public void tearDown() {
        tcis = null;
        memo = null;
        JUnitUtil.resetWindows(false,false);
        JUnitUtil.clearShutDownManager(); // put in place because AbstractMRTrafficController implementing subclass was not terminated properly
        JUnitUtil.tearDown();

    }

}
