package jmri.jmrix.can.cbus.swing.simulator;

import java.util.List;

import javax.swing.JMenu;
import javax.swing.JMenuBar;

import jmri.jmrix.can.CanSystemConnectionMemo;
import jmri.jmrix.can.TrafficControllerScaffold;
import jmri.util.JmriJFrame;
import jmri.util.JUnitUtil;
import jmri.util.ThreadingUtil;

import org.junit.jupiter.api.*;

import org.netbeans.jemmy.operators.*;

/**
 * Test simple functioning of CBUS SimulatorPane.
 *
 * @author Paul Bender Copyright (C) 2016
 * @author Steve Young Copyright (C) 2019
 */
@jmri.util.junit.annotations.DisabledIfHeadless
public class SimulatorPaneTest extends jmri.util.swing.JmriPanelTest {

    @Test
    public void testInitComp() {

        ((SimulatorPane)panel).initComponents(memo);

        Assertions.assertNotNull(panel, "exists");
        Assertions.assertEquals("CAN " + Bundle.getMessage("MenuItemNetworkSim"), panel.getTitle(), "name with memo");

        // check pane has loaded something
        JmriJFrame f = new JmriJFrame();
        f.add(panel);
        f.setTitle(panel.getTitle());

        List<JMenu> list = panel.getMenus();
        JMenuBar bar = f.getJMenuBar();
        if (bar == null) {
            bar = new JMenuBar();
        }
        for (JMenu menu : list) {
            bar.add(menu);
        }
        f.setJMenuBar(bar);

        ThreadingUtil.runOnGUI(() -> {
            f.pack();
            f.setVisible(true);
        });
        // Find new window by name
        JFrameOperator jfo = new JFrameOperator( panel.getTitle() );

        Assertions.assertTrue(getResetCsButtonEnabled(jfo));

        // Ask to close window
        jfo.requestClose();
        jfo.waitClosed();
 
    }

    private boolean getResetCsButtonEnabled( JFrameOperator jfo ){
        return ( new JButtonOperator(jfo, Bundle.getMessage("Reset")).isEnabled() );
    }

    private CanSystemConnectionMemo memo = null; 
    private TrafficControllerScaffold tcis = null;

    @Override
    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        title = Bundle.getMessage("MenuItemNetworkSim");
        helpTarget = "package.jmri.jmrix.can.cbus.swing.simulator.SimulatorPane";
        memo = new CanSystemConnectionMemo();
        tcis = new TrafficControllerScaffold();
        memo.setTrafficController(tcis);
        memo.setProtocol(jmri.jmrix.can.CanConfigurationManager.MERGCBUS);

        panel = new SimulatorPane();
    }

    @Override
    @AfterEach
    public void tearDown() {
        Assertions.assertNotNull(tcis);
        tcis.terminateThreads();
        Assertions.assertNotNull(memo);
        memo.dispose();
        tcis = null;
        memo = null;
        super.tearDown();
    }

}
