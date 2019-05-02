package jmri.jmrix.marklin.swing.monitor;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Before;

/**
 * Test simple functioning of MarklinMonPane
 *
 * @author	Paul Bender Copyright (C) 2016
 */
public class MarklinMonPaneTest extends jmri.jmrix.AbstractMonPaneTestBase {

    @Override
    @Before
    public void setUp() {
        JUnitUtil.setUp();
        // pane for AbstractMonPaneTestBase, panel for JmriJPanelTest
        panel = pane = new MarklinMonPane();
        title=Bundle.getMessage("MarklinMonitorTitle");
    }

    @Override
    @After
    public void tearDown() {        JUnitUtil.tearDown();    }
}
