package jmri.util.swing;

import java.awt.Color;
import java.util.ArrayList;

import javax.swing.JColorChooser;

import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.DisabledIfSystemProperty;

/**
 *
 * @author Dave Sand Copyright (C) 2018
 */
public class JmriColorChooserTest {

    @Test
    public void testAddRecentColor() {
        JmriColorChooser.addRecentColor(Color.WHITE);
        ArrayList<Color> colors = JmriColorChooser.getRecentColors();
        Assert.assertFalse("recent color count > 0", colors.isEmpty());  // NOI18N
    }

    @Test
    public void testGetRecentList() {
        ArrayList<Color> colors = JmriColorChooser.getRecentColors();
        Assert.assertNotNull("exists", colors);  // NOI18N
    }

    @Test
    public void testExtendColorChooser() {
        JColorChooser jmriTab = JmriColorChooser.extendColorChooser(new JColorChooser(Color.WHITE));
        Assert.assertNotNull("exists", jmriTab);
    }

    @Test
    @DisabledIfSystemProperty(named ="java.awt.headless", matches ="true")
    public void testShowDialog() {

        Thread t = JemmyUtil.createModalDialogOperatorThread("Test Title", "OK");  // NOI18N
        JmriColorChooser.addRecentColor(Color.WHITE);
        Color newColor = JmriColorChooser.showDialog(null, "Test Title", Color.RED);  // NOI18N
        Assert.assertNotNull("exists", newColor);
        Assert.assertEquals(Color.RED, newColor);
        JUnitUtil.waitFor(() -> ( !t.isAlive() ), "ColorChooser Dialog did not click ok");
        JUnitUtil.waitFor(5); // jemmy still finishing button push
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.resetWindows(false,false);
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(JmriColorChooserTest.class);

}
