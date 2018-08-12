package jmri.jmrix.loconet.duplexgroup.swing;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Before;

/**
 * Test simple functioning of DuplexGroupScanPanel
 *
 * @author	Paul Bender Copyright (C) 2016
 */
public class DuplexGroupScanPanelTest extends jmri.util.swing.JmriPanelTest {

    @Override
    @Before
    public void setUp() {
        JUnitUtil.setUp();
        panel = new DuplexGroupScanPanel();
        helpTarget = "package.jmri.jmrix.loconet.DuplexGroupSetup.DuplexGroupScanPanel";
        title = "Scan Duplex Channels";
    }

    @Override
    @After
    public void tearDown() {        JUnitUtil.tearDown();    }
}
