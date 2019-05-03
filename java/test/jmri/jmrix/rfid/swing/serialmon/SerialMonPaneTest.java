package jmri.jmrix.rfid.swing.serialmon;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Before;

/**
 * Test simple functioning of SerialMonPane
 *
 * @author	Paul Bender Copyright (C) 2016
 */
public class SerialMonPaneTest extends jmri.jmrix.AbstractMonPaneTestBase {


    @Override
    @Before
    public void setUp() {
        JUnitUtil.setUp();
        // panel is for the AbstractMonPaneTestBase, pane is for it's parent (JmriPanelTest )
        panel = pane = new SerialMonPane();
        title="RFID Device Command Monitor";
    }

    @Override
    @After
    public void tearDown() {        JUnitUtil.tearDown();    }
}
