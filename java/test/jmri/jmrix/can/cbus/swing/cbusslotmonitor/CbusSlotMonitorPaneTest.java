package jmri.jmrix.can.cbus.swing.cbusslotmonitor;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Before;

/**
 * Test simple functioning of CbusSlotMonitorPane.
 *
 * @author Paul Bender Copyright (C) 2016
 */
public class CbusSlotMonitorPaneTest extends jmri.util.swing.JmriPanelTest {

    @Override
    @Before
    public void setUp() {
        JUnitUtil.setUp();
        panel = new CbusSlotMonitorPane();
        title = Bundle.getMessage("MenuItemCbusSlotMonitor");
        helpTarget = "package.jmri.jmrix.can.cbus.swing.cbusslotmonitor.CbusSlotMonitorPane";
    }

    @Override
    @After
    public void tearDown() {        JUnitUtil.tearDown();    }

}
