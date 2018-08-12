package jmri.jmrix.dcc4pc.swing.monitor;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Before;

/**
 * Test simple functioning of Dcc4PcMonPane
 *
 * @author	Paul Bender Copyright (C) 2016
 */
public class Dcc4PcMonPaneTest extends jmri.jmrix.AbstractMonPaneTestBase {


    @Override
    @Before
    public void setUp() {
        JUnitUtil.setUp();
        // panel is for the AbstractMonPaneTestBase, pane is for it's parent (JmriPanelTest )
        panel = pane = new Dcc4PcMonPane();
        title="Dcc4PC Command Monitor";
    }

    @Override
    @After
    public void tearDown() {        JUnitUtil.tearDown();    }
}
