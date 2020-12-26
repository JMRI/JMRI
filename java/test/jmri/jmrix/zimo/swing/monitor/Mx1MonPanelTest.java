package jmri.jmrix.zimo.swing.monitor;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

/**
 * Test simple functioning of Mx1MonPanel
 *
 * @author Paul Bender Copyright (C) 2016
 */
public class Mx1MonPanelTest extends jmri.util.swing.JmriPanelTest {

    @Override
    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        panel = new Mx1MonPanel();
        helpTarget="package.jmri.jmrix.zimo.swing.monitor.Mx1MonPanel";
        title="Mx1_: Command Monitor";
    }

    @Override
    @AfterEach
    public void tearDown() {        JUnitUtil.tearDown();    }
}
