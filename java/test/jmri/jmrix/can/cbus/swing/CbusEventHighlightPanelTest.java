package jmri.jmrix.can.cbus.swing;

import static org.assertj.core.api.Assertions.assertThat;

import jmri.util.JUnitUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.condition.DisabledIfSystemProperty;
import org.junit.jupiter.api.Test;

/**
 * Test simple functioning of CbusEventHighlightPanel
 *
 * @author Paul Bender Copyright (C) 2016
 */
public class CbusEventHighlightPanelTest {

    @Test
    @DisabledIfSystemProperty(named ="java.awt.headless", matches ="true")
    public void testCtor() {
        CbusEventHighlightFrame frame = new CbusEventHighlightFrame();
        CbusEventHighlightPanel panel = new CbusEventHighlightPanel(frame,1);
        assertThat(panel).isNotNull();
        frame.dispose();
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
