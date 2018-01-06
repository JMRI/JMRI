package jmri.jmrix.powerline.swing.serialmon;

import jmri.jmrix.powerline.SerialTrafficControlScaffold;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Before;

/**
 * Test simple functioning of SerialMonPane
 *
 * @author	Paul Bender Copyright (C) 2016
 */
public class SerialMonPaneTest extends jmri.jmrix.AbstractMonPaneTestBase {


    private SerialTrafficControlScaffold tc = null;

    @Override
    @Before
    public void setUp() {
        JUnitUtil.setUp();
        tc = new SerialTrafficControlScaffold();
        // pane for AbstractMonPaneTestBase, panel for JmriPanel
        panel = pane = new SerialMonPane();
        title="Powerline_: Communication Monitor";
    }

    @Override
    @After
    public void tearDown() {        JUnitUtil.tearDown();        tc = null;
    }
}
