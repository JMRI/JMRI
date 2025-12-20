package jmri.util.swing;

import java.awt.Color;

import javax.swing.JButton;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 *
 * @author Dave Sand Copyright (C) 2018
 */
public class JmriColorChooserPanelTest {

    protected JmriColorChooserPanel panel = null;

    @Test
    public void testCTor() {
        assertNotNull( panel, "exists");
    }

    @Test
    public void testCreateColorButton() {
        JButton button = panel.createColorButton(Color.RED, true);
        assertNotNull( button, "exists");
    }

    @Test
    public void testGetDisplayName() {
        assertEquals( "JMRI", panel.getDisplayName(), "display name");
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        panel = new JmriColorChooserPanel();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
        panel = null;
    }

    // private final static Logger log = LoggerFactory.getLogger(JmriColorChooserPanelTest.class);

}
