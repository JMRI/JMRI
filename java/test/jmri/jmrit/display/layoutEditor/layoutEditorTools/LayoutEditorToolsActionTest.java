package jmri.jmrit.display.layoutEditor.layoutEditorTools;

import static java.lang.Thread.sleep;

import java.awt.GraphicsEnvironment;
import java.util.logging.Level;
import java.util.logging.Logger;
import jmri.jmrit.display.layoutEditor.LayoutEditor;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Test simple functioning of LayoutEditorToolsAction
 *
 * @author	George Warner Copyright (C) 2017
 */
public class LayoutEditorToolsActionTest {

    private static LayoutEditor layoutEditor = null;

    @BeforeClass
    public static void setupClass() {
        if (!GraphicsEnvironment.isHeadless()) {
            try {
                jmri.configurexml.ConfigXmlManager cm = new jmri.configurexml.ConfigXmlManager() {
                };
                Assert.assertNotNull("ConfigXmlManager exists", cm);

                // load and display sample file
                java.io.File leFile = new java.io.File("java/test/jmri/jmrit/display/layoutEditor/valid/SimpleLayoutEditorTest.xml");
                cm.load(leFile);
                sleep(100); // time for internal listeners to calm down

                // Find new window by name (should be more distinctive, comes from sample file)
                layoutEditor = (LayoutEditor) jmri.util.JmriJFrame.getFrame("My Layout");

                // make it editable
                layoutEditor.setAllEditable(true);
                Assert.assertTrue("isEditable after setAllEditable(true)", layoutEditor.isEditable());
            } catch (Exception ex) {
                Logger.getLogger(LayoutEditorToolsFrameTest.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    @AfterClass
    public static void teardownClass() throws InterruptedException {
        if (layoutEditor != null) {
            layoutEditor.dispose();
            layoutEditor = null;
        }
    }

    @Test
    public void testCtor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        Assert.assertNotNull("exist", layoutEditor);
    }

    @Test   //(expected = java.lang.IllegalArgumentException.class)
    public void testLayoutEditorTFrameExist() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        Assert.assertNotNull("exist", layoutEditor);
    }

    @Test
    public void testActionSetSignalsAtBlockBoundary() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
//        SpeedometerAction action = new SpeedometerAction("Sensor Group");
//        action.actionPerformed(null);
//        // wait for frame with "Speedometer" in title, case insensitive
//        // first boolean is false for exact to allow substring to match
//        // second boolean is false to all case insensitive match
//        JFrame frame = JFrameOperator.waitJFrame("Speedometer", false, false);
//        Assert.assertNotNull(frame);
//        // verify the action provided the expected frame class
//        Assert.assertEquals(SpeedometerFrame.class.getName(), frame.getClass().getName());
    }

    @Before
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.initInternalSensorManager();
    }

    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }
    // private final static Logger log = LoggerFactory.getLogger(LayoutEditorToolsActionTest.class.getName());
}
