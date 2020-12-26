package jmri.jmrix.marklin.swing.monitor;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

/**
 * Test simple functioning of MarklinMonPane
 *
 * @author Paul Bender Copyright (C) 2016
 */
public class MarklinMonPaneTest extends jmri.jmrix.AbstractMonPaneTestBase {

    @Override
    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        // pane for AbstractMonPaneTestBase, panel for JmriJPanelTest
        panel = pane = new MarklinMonPane();
        title=Bundle.getMessage("MarklinMonitorTitle");
    }

    @Override
    @AfterEach
    public void tearDown() {
        panel = pane = null;
        JUnitUtil.tearDown();
    }
}
