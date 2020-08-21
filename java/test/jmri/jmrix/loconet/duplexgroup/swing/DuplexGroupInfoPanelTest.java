package jmri.jmrix.loconet.duplexgroup.swing;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

/**
 * Test simple functioning of DuplexGroupInfoPanel
 *
 * @author Paul Bender Copyright (C) 2016
 */
public class DuplexGroupInfoPanelTest extends jmri.util.swing.JmriPanelTest {

    @Override
    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        panel = new DuplexGroupInfoPanel();
        helpTarget = "package.jmri.jmrix.loconet.duplexgroup.DuplexGroupTabbedPanel";
        title = "Configure Duplex Group Information"; 
    }

    @Override
    @AfterEach
    public void tearDown() {        JUnitUtil.tearDown();    }
}
