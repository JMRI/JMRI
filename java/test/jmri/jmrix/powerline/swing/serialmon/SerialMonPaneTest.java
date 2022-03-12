package jmri.jmrix.powerline.swing.serialmon;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

/**
 * Test simple functioning of SerialMonPane
 *
 * @author Paul Bender Copyright (C) 2016
 */
public class SerialMonPaneTest extends jmri.jmrix.AbstractMonPaneTestBase {

    // private SerialTrafficControlScaffold tc = null;

    @Override
    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        // tc = new SerialTrafficControlScaffold();
        // pane for AbstractMonPaneTestBase, panel for JmriPanel
        panel = pane = new SerialMonPane();
        title="Powerline_: Communication Monitor";
        helpTarget="package.jmri.jmrix.powerline.serialmon.SerialMonFrame";
    }

    @Override
    @AfterEach
    public void tearDown() {
        panel = pane = null;
        JUnitUtil.tearDown();
        // tc = null;
    }
}
