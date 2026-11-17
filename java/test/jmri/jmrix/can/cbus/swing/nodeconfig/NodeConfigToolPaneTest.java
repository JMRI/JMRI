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

    @Test
    public void testDividerPlaceSavedOnceItStopsMoving() throws InterruptedException {

        Assertions.assertNotNull(memo);
        CbusPreferences prefs = memo.get(CbusPreferences.class);
        prefs.setNodeBackgroundFetchDelay(0);
        CbusNodeTableDataModel nodeModel = memo.get(CbusConfigurationManager.class)
            .provide(CbusNodeTableDataModel.class);

        NodeConfigToolPane nodeConfigpanel = new NodeConfigToolPane();
        nodeConfigpanel.initComponents(memo);
        javax.swing.JSplitPane split = findSplit(nodeConfigpanel);
        Assertions.assertNotNull(split);

        // count the times the divider's place is written to the preferences store
        java.util.prefs.Preferences store = jmri.profile.ProfileUtils
            .getPreferences(prefs.getProfile(), CbusPreferences.class, true);
        java.util.concurrent.atomic.AtomicInteger saves = new java.util.concurrent.atomic.AtomicInteger();
        java.util.prefs.PreferenceChangeListener counter = e -> {
            if ("nodetablesplit".equals(e.getKey())) {
                saves.incrementAndGet();
            }
        };
        store.addPreferenceChangeListener(counter);

        // a drag reports every pixel the divider moves; it ends away from
        // where it started, which may be kept from an earlier run
        int start = prefs.getNodeTableSplit();
        ThreadingUtil.runOnGUI(() -> {
            for (int px = start + 1; px <= start + 40; px++) {
                split.setDividerLocation(px);
            }
        });
        JUnitUtil.waitFor(() -> prefs.getNodeTableSplit() == start + 40, "divider place saved");
        JUnitUtil.waitFor(() -> saves.get() > 0, "save reported");
        Thread.sleep(200); // preference change events are delivered on their own thread
        Assertions.assertEquals(1, saves.get(), "saved once, not for every pixel");

        store.removePreferenceChangeListener(counter);
        nodeConfigpanel.dispose();
        nodeModel.dispose();
    }

    private static javax.swing.JSplitPane findSplit(java.awt.Container c) {
        for (java.awt.Component child : c.getComponents()) {
            if (child instanceof javax.swing.JSplitPane) {
                return (javax.swing.JSplitPane) child;
            }
            if (child instanceof java.awt.Container) {
                javax.swing.JSplitPane found = findSplit((java.awt.Container) child);
                if (found != null) {
                    return found;
                }
            }
        }
        return null;
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
