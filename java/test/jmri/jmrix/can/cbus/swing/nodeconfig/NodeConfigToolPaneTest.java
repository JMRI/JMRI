package jmri.jmrix.can.cbus.swing.nodeconfig;

import javax.swing.JFrame;

import jmri.jmrix.can.CanSystemConnectionMemo;
import jmri.jmrix.can.TrafficControllerScaffold;
import jmri.jmrix.can.cbus.CbusConfigurationManager;
import jmri.jmrix.can.cbus.CbusPreferences;
import jmri.jmrix.can.cbus.node.CbusNodeTableDataModel;
import jmri.util.JUnitUtil;
import jmri.util.ThreadingUtil;

import org.junit.jupiter.api.*;

import org.netbeans.jemmy.operators.JFrameOperator;
import org.netbeans.jemmy.operators.JTableOperator;

/**
 * Test simple functioning of NodeConfigToolPane
 *
 * @author Paul Bender Copyright (C) 2016
 * @author Steve Young Copyright (C) 2019
 */
@jmri.util.junit.annotations.DisabledIfHeadless
public class NodeConfigToolPaneTest extends jmri.util.swing.JmriPanelTest {

    @Test
    public void testInitComp() {

        Assertions.assertNotNull(memo);
        memo.get(CbusPreferences.class).setNodeBackgroundFetchDelay(0);
        CbusNodeTableDataModel nodeModel = memo.get(CbusConfigurationManager.class)
            .provide(CbusNodeTableDataModel.class);

        NodeConfigToolPane nodeConfigpanel = new NodeConfigToolPane();
        nodeConfigpanel.initComponents(memo);

        Assertions.assertNotNull( nodeConfigpanel, "exists");
        Assertions.assertNotNull( nodeModel, "core node model exists");

        nodeModel.dispose();

    }

    @Test
    public void testDuplicateCanIdHighlight() {

        Assertions.assertNotNull(memo);
        memo.get(CbusPreferences.class).setNodeBackgroundFetchDelay(0);
        CbusNodeTableDataModel nodeModel = memo.get(CbusConfigurationManager.class)
            .provide(CbusNodeTableDataModel.class);

        nodeModel.provideNodeByNodeNum(556).setCanId(1);
        nodeModel.provideNodeByNodeNum(557).setCanId(2);
        nodeModel.provideNodeByNodeNum(558).setCanId(1);
        nodeModel.provideNodeByNodeNum(559).setCanId(tcis.getCanid());

        NodeConfigToolPane nodeConfigpanel = new NodeConfigToolPane();
        nodeConfigpanel.initComponents(memo);
        JFrame f = new JFrame("testDuplicateCanIdHighlight");
        f.getContentPane().add(nodeConfigpanel);
        ThreadingUtil.runOnGUI( () -> {
            f.pack();
            f.setVisible(true);
        });

        JFrameOperator jfo = new JFrameOperator(f.getTitle());
        Assertions.assertNotNull(jfo);

        JTableOperator jto = new JTableOperator(jfo);
        Assertions.assertEquals(4, jto.getRowCount());

        nodeModel.dispose();
        JUnitUtil.dispose(f);
        jfo.waitClosed();
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
        // JUnitUtil.resetWindows(false,false);
        JUnitUtil.tearDown();

    }

}
