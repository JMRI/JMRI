package jmri.util.swing;

import java.awt.Color;

import javax.swing.JButton;

import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;

/**
 *
 * @author Dave Sand Copyright (C) 2018
 */
public class JmriColorChooserPanelTest {

    protected JmriColorChooserPanel panel = null;

    @Test
    public void testCTor() {
        Assert.assertNotNull("exists", panel);
    }

    @Test
    public void testCreateColorButton() {
        JButton button = panel.createColorButton(Color.RED, true);
        Assert.assertNotNull("exists", button);
    }

    @Test
    public void testGetDisplayName() {
        Assert.assertEquals("display name", "JMRI", panel.getDisplayName());
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
