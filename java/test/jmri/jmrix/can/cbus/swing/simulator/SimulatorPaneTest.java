package jmri.jmrix.can.cbus.swing.simulator;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Before;

/**
 * Test simple functioning of CbusSlotMonitorPane.
 *
 * @author Paul Bender Copyright (C) 2016
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

}
