package jmri.jmrix.tams.swing.monitor;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Before;

/**
 * Test simple functioning of TamsMonPane
 *
 * @author	Paul Bender Copyright (C) 2016
 */
public class TamsMonPaneTest extends jmri.jmrix.AbstractMonPaneTestBase {


    @Override
    @Before
    public void setUp() {
        JUnitUtil.setUp();
        // pane for AbstractMonPaneTestBase, panel for JmriPanelTest
        panel = pane = new TamsMonPane();
        title="Tams Command Monitor";
    }

    @Override
    @After
    public void tearDown() {        JUnitUtil.tearDown();    }
}
