package jmri.jmrix.can.cbus.swing.nodeconfig;

import jmri.jmrix.can.CanSystemConnectionMemo;
import jmri.jmrix.can.TrafficControllerScaffold;
import jmri.jmrix.can.cbus.CbusConfigurationManager;
import jmri.jmrix.can.cbus.CbusPreferences;
import jmri.jmrix.can.cbus.node.CbusNodeTableDataModel;
import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.DisabledIfSystemProperty;

/**
 * Test simple functioning of NodeConfigToolPane
 *
 * @author Paul Bender Copyright (C) 2016
 * @author Steve Young Copyright (C) 2019
 */
public class NodeConfigToolPaneTest extends jmri.util.swing.JmriPanelTest {

    @Test
    @DisabledIfSystemProperty(named ="java.awt.headless", matches ="true")
    public void testInitComp() {

        Assertions.assertNotNull(memo);
        memo.get(CbusPreferences.class).setNodeBackgroundFetchDelay(0);
        CbusNodeTableDataModel nodeModel = memo.get(CbusConfigurationManager.class)
            .provide(CbusNodeTableDataModel.class);

        NodeConfigToolPane nodeConfigpanel = new NodeConfigToolPane();
        nodeConfigpanel.initComponents(memo);

        Assert.assertNotNull("exists", nodeConfigpanel);
        Assert.assertNotNull("core node model exists", nodeModel);

        nodeModel.dispose();

    }

    private CanSystemConnectionMemo memo = null;
    private TrafficControllerScaffold tcis = null;

    @BeforeEach
    @Override
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetInstanceManager();
        memo = new CanSystemConnectionMemo();
        tcis = new TrafficControllerScaffold();
        memo.setTrafficController(tcis);
        memo.setProtocol(jmri.jmrix.can.CanConfigurationManager.MERGCBUS);
        panel = new NodeConfigToolPane();
        title = Bundle.getMessage("MenuItemNodeConfig");
        helpTarget = "package.jmri.jmrix.can.cbus.swing.nodeconfig.NodeConfigToolPane";
    }

    @AfterEach
    @Override
    public void tearDown() {
        Assertions.assertNotNull(tcis);
        tcis.terminateThreads();
        Assertions.assertNotNull(memo);
        memo.dispose();
        tcis = null;
        memo = null;
        JUnitUtil.resetWindows(false,false);
        JUnitUtil.tearDown();

    }

}
