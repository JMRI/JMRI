package jmri.jmrix.loconet.duplexgroup.swing;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Before;

/**
 * Test simple functioning of DuplexGroupTabbedPanel
 *
 * @author	Paul Bender Copyright (C) 2016
 */
public class DuplexGroupTabbedPanelTest extends jmri.util.swing.JmriPanelTest {

    @Override
    @Before
    public void setUp() {
        JUnitUtil.setUp();
        panel = new DuplexGroupTabbedPanel();
        helpTarget = "package.jmri.jmrix.loconet.duplexgroup.DuplexGroupTabbedPanel";
        title = "Duplex Group Configuration";
    }

    @Override
    @After
    public void tearDown() {        JUnitUtil.tearDown();    }
}
