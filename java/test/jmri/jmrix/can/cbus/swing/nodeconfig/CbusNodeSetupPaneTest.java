package jmri.jmrix.can.cbus.swing.nodeconfig;

import jmri.jmrix.can.CanSystemConnectionMemo;
import jmri.jmrix.can.TrafficControllerScaffold;
import jmri.jmrix.can.cbus.CbusConfigurationManager;
import jmri.jmrix.can.cbus.node.CbusNode;
import jmri.jmrix.can.cbus.node.CbusNodeTableDataModel;
import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.DisabledIfSystemProperty;

/**
 * Test simple functioning of CbusNodeSetupPane
 *
 * @author Paul Bender Copyright (C) 2016
 * @author Steve Young Copyright (C) 2019
 */
@DisabledIfSystemProperty(named ="java.awt.headless", matches ="true")
public class CbusNodeSetupPaneTest {

    @Test
    public void testCtor() {
        CbusNodeSetupPane t = new CbusNodeSetupPane(null);
        Assertions.assertNotNull(t);
        Assertions.assertNotNull(nodeToEdit);
    }

    @Test
    public void testInit() {

        CbusNodeSetupPane t = new CbusNodeSetupPane(null);
        t.setNode(nodeToEdit); // node num 256

        Assertions.assertNotNull(t);

    }

    @Test
    public void testCanIdControlsOnlyForCbusNodes() {

        CbusNodeSetupPane t = new CbusNodeSetupPane(null);
        t.setNode(nodeToEdit); // flags 8: a CBUS node
        Assertions.assertTrue(isInVisiblePanel(t, "Force set CAN ID"), "CAN ID controls for a CBUS node");
        Assertions.assertTrue(isInVisiblePanel(t, "CAN ID Self Enumeration"), "CAN ID controls for a CBUS node");

        // the flags arrive with bit 6 set: a VLCB node, which chooses its own CAN ID
        nodeToEdit.getNodeParamManager().setParameter(8, 0x48);
        JUnitUtil.waitFor(() -> !isInVisiblePanel(t, "Force set CAN ID"), "no CAN ID controls for a VLCB node");
        Assertions.assertFalse(isInVisiblePanel(t, "CAN ID Self Enumeration"), "no CAN ID controls for a VLCB node");

        // chosen afresh, as a VLCB node
        CbusNodeSetupPane fresh = new CbusNodeSetupPane(null);
        fresh.setNode(nodeToEdit);
        Assertions.assertFalse(isInVisiblePanel(fresh, "Force set CAN ID"), "no CAN ID controls for a VLCB node");
        fresh.dispose();
        t.dispose();
    }

    private static javax.swing.JButton findButton(java.awt.Container c, String text) {
        for (java.awt.Component child : c.getComponents()) {
            if (child instanceof javax.swing.JButton && text.equals(((javax.swing.JButton) child).getText())) {
                return (javax.swing.JButton) child;
            }
            if (child instanceof java.awt.Container) {
                javax.swing.JButton b = findButton((java.awt.Container) child, text);
                if (b != null) {
                    return b;
                }
            }
        }
        return null;
    }

    // the button is there and it and every container up to the pane is visible
    private static boolean isInVisiblePanel(java.awt.Container pane, String text) {
        java.awt.Component c = findButton(pane, text);
        Assertions.assertNotNull(c, text);
        while (c != null && c != pane) {
            if (!c.isVisible()) {
                return false;
            }
            c = c.getParent();
        }
        return true;
    }

    private CanSystemConnectionMemo memo = null;
    private TrafficControllerScaffold tcis = null;
    private CbusNodeTableDataModel nodeModel = null;
    private CbusNode nodeToEdit = null;

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetInstanceManager();
        memo = new CanSystemConnectionMemo();
        memo.setProtocol(jmri.jmrix.can.CanConfigurationManager.MERGCBUS);
        tcis = new TrafficControllerScaffold();
        memo.setTrafficController(tcis);
        memo.register();
        nodeModel = memo.get(CbusConfigurationManager.class).provide(CbusNodeTableDataModel.class);
        nodeToEdit = nodeModel.provideNodeByNodeNum(256);
        // set node to 3 node vars , param6
        nodeToEdit.getNodeParamManager().setParameters(new int[]{8,1,2,3,4,5,3,7,8});

    }

    @AfterEach
    public void tearDown() {
        Assertions.assertNotNull(nodeToEdit);
        nodeToEdit.dispose();
        Assertions.assertNotNull(nodeModel);
        nodeModel.dispose();
        Assertions.assertNotNull(tcis);
        tcis.terminateThreads();
        Assertions.assertNotNull(memo);
        memo.dispose();
        memo = null;
        tcis = null;

        JUnitUtil.tearDown();

    }

    // private static final Logger log = LoggerFactory.getLogger(CbusNodeSetupPaneTest.class);

}
