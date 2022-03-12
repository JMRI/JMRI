package jmri.jmrix.tams.swing.monitor;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

/**
 * Test simple functioning of TamsMonPane
 *
 * @author Paul Bender Copyright (C) 2016
 */
public class TamsMonPaneTest extends jmri.jmrix.AbstractMonPaneTestBase {


    @Override
    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        // pane for AbstractMonPaneTestBase, panel for JmriPanelTest
        panel = pane = new TamsMonPane();
        title="Tams Command Monitor";
    }

    @Override
    @AfterEach
    public void tearDown() {
        panel = pane = null;
        JUnitUtil.tearDown();
    }
}
