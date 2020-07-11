package jmri.jmrix.rfid.swing.serialmon;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

/**
 * Test simple functioning of SerialMonPane
 *
 * @author Paul Bender Copyright (C) 2016
 */
public class SerialMonPaneTest extends jmri.jmrix.AbstractMonPaneTestBase {


    @Override
    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        // panel is for the AbstractMonPaneTestBase, pane is for it's parent (JmriPanelTest )
        panel = pane = new SerialMonPane();
        title="RFID Device Command Monitor";
    }

    @Override
    @AfterEach
    public void tearDown() {
        panel = pane = null;
        JUnitUtil.tearDown();
    }
}
