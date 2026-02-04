package jmri.util.swing;

import java.awt.Color;
import java.util.ArrayList;

import javax.swing.JColorChooser;

import jmri.util.JUnitUtil;
import jmri.util.junit.annotations.DisabledIfHeadless;

import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 *
 * @author Dave Sand Copyright (C) 2018
 */
public class JmriColorChooserTest {

    @Test
    public void testAddRecentColor() {
        JmriColorChooser.addRecentColor(Color.WHITE);
        ArrayList<Color> colors = JmriColorChooser.getRecentColors();
        assertFalse( colors.isEmpty(), "recent color count > 0");
    }

    @Test
    public void testGetRecentList() {
        ArrayList<Color> colors = JmriColorChooser.getRecentColors();
        assertNotNull( colors, "exists");
    }

    @Test
    public void testExtendColorChooser() {
        JColorChooser jmriTab = JmriColorChooser.extendColorChooser(new JColorChooser(Color.WHITE));
        assertNotNull( jmriTab, "exists");
    }

    @Test
    @DisabledIfHeadless
    public void testShowDialog() {

        Thread t = JemmyUtil.createModalDialogOperatorThread("Test Title", "OK");  // NOI18N
        JmriColorChooser.addRecentColor(Color.WHITE);
        Color newColor = JmriColorChooser.showDialog(null, "Test Title", Color.RED);  // NOI18N
        assertNotNull( newColor, "exists");
        assertEquals(Color.RED, newColor);
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
