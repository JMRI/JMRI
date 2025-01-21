package jmri.jmrix.can.cbus.swing;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

/**
 * Test simple functioning of CbusEventHighlightPanel
 *
 * @author Paul Bender Copyright (C) 2016
 */
public class CbusEventHighlightPanelTest {

    @Test
    @jmri.util.junit.annotations.DisabledIfHeadless
    public void testCtor() {
        CbusEventHighlightFrame frame = new CbusEventHighlightFrame();
        CbusEventHighlightPanel panel = new CbusEventHighlightPanel(frame,1);
        Assertions.assertNotNull(panel);
        JUnitUtil.dispose(frame);
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }

}
