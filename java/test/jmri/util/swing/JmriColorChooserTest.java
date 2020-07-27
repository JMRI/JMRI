package jmri.util.swing;

import java.awt.Color;
import java.awt.GraphicsEnvironment;
import java.util.ArrayList;

import javax.swing.JColorChooser;

import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;
import org.junit.Assume;
import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.JDialogOperator;

/**
 *
 * @author Dave Sand Copyright (C) 2018
 */
public class JmriColorChooserTest {

    @Test
    public void testAddRecentColor() {
        JmriColorChooser.addRecentColor(Color.WHITE);
        ArrayList<Color> colors = JmriColorChooser.getRecentColors();
        boolean gtzero = colors.size() > 0;
        Assert.assertTrue("color count", gtzero);  // NOI18N
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
    public void testShowDialog() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());

        createModalDialogOperatorThread("Test Title", "OK");  // NOI18N
        JmriColorChooser.addRecentColor(Color.WHITE);
        Color newColor = JmriColorChooser.showDialog(null, "Test Title", Color.RED);  // NOI18N
        Assert.assertNotNull("exists", newColor);
    }

    void createModalDialogOperatorThread(String dialogTitle, String buttonText) {
        Thread t = new Thread(() -> {
            // constructor for jdo will wait until the dialog is visible
            JDialogOperator jdo = new JDialogOperator(dialogTitle);
            JButtonOperator jbo = new JButtonOperator(jdo, buttonText);
            jbo.pushNoBlock();
        });
        t.setName(dialogTitle + " Close Dialog Thread");  // NOI18N
        t.start();
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
