package jmri.jmrix.can.cbus.swing.simulator;

import java.awt.GraphicsEnvironment;
import java.util.List;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import jmri.jmrix.can.CanSystemConnectionMemo;
import jmri.jmrix.can.TrafficControllerScaffold;
import jmri.util.JmriJFrame;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;
import org.netbeans.jemmy.operators.*;

/**
 * Test simple functioning of CBUS SimulatorPane.
 *
 * @author Paul Bender Copyright (C) 2016
 * @author Steve Young Copyright (C) 2019
 */
public class SimulatorPaneTest extends jmri.util.swing.JmriPanelTest {

    @Override
    @Before
    public void setUp() {
        JUnitUtil.setUp();
        panel = new SimulatorPane();
        title = Bundle.getMessage("MenuItemNetworkSim");
        helpTarget = "package.jmri.jmrix.can.cbus.swing.simulator.SimulatorPane";
    }
    
    @Override
    @After
    public void tearDown() {        JUnitUtil.tearDown();    }
    
    @Test
    public void testInitComp() {
        
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        
        CanSystemConnectionMemo memo = new CanSystemConnectionMemo();
        TrafficControllerScaffold tcis = new TrafficControllerScaffold();
        memo.setTrafficController(tcis);
        
        SimulatorPane panel = new SimulatorPane();
        panel.initComponents(memo);
        
        Assert.assertNotNull("exists", panel);
        Assert.assertEquals("name with memo","CAN " + Bundle.getMessage("MenuItemNetworkSim"), panel.getTitle());
        
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
        
        f.pack();
        f.setVisible(true);
        
        // Find new window by name
        JFrameOperator jfo = new JFrameOperator( panel.getTitle() );
        
        Assert.assertTrue(getResetCsButtonEnabled(jfo));

        // Ask to close window
        jfo.requestClose();

        tcis = null;
        memo = null;
        
    }
    
    private boolean getResetCsButtonEnabled( JFrameOperator jfo ){
        return ( new JButtonOperator(jfo, Bundle.getMessage("Reset")).isEnabled() );
    }

}
