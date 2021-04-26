package jmri.jmrix.loconet.duplexgroup.swing;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

/**
 * Test simple functioning of DuplexGroupScanPanel
 *
 * @author Paul Bender Copyright (C) 2016
 */
public class DuplexGroupScanPanelTest extends jmri.util.swing.JmriPanelTest {

    @Override
    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        panel = new DuplexGroupScanPanel();
        helpTarget = "package.jmri.jmrix.loconet.duplexgroup.DuplexGroupTabbedPanel";
        title = "Scan Duplex Channels";
    }

    @Override
    @AfterEach
    public void tearDown() {        JUnitUtil.tearDown();    }
}
