package jmri.jmrix.loconet.duplexgroup.swing;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

/**
 * Test simple functioning of DuplexGroupTabbedPanel
 *
 * @author Paul Bender Copyright (C) 2016
 */
public class DuplexGroupTabbedPanelTest extends jmri.util.swing.JmriPanelTest {

    @Override
    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        panel = new DuplexGroupTabbedPanel();
        helpTarget = "package.jmri.jmrix.loconet.duplexgroup.DuplexGroupTabbedPanel";
        title = "Duplex Group Configuration";
    }

    @Override
    @AfterEach
    public void tearDown() {        JUnitUtil.tearDown();    }
}
