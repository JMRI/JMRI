package jmri.jmrit.display.layoutEditor;

import java.awt.GraphicsEnvironment;
import jmri.jmrit.display.EditorFrameOperator;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;

/**
 * Swing tests for the LayoutEditor.
 *
 * @author	Bob Jacobsen Copyright 2009, 2010
 */
public class LayoutEditorWindowTest {

    @Test
    public void testShowAndClose() throws Exception {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        jmri.configurexml.ConfigXmlManager cm = new jmri.configurexml.ConfigXmlManager() {
        };

        // load and display sample file
        java.io.File f = new java.io.File("java/test/jmri/jmrit/display/layoutEditor/valid/SimpleLayoutEditorTest.xml");
        cm.load(f);

        // Find new window by name (should be more distinctive, comes from sample file)
        EditorFrameOperator to = new EditorFrameOperator("My Layout");

        // It's up at this point, and can be manipulated
        // Ask to close window
        to.closeFrameWithConfirmations();
    }

    // Setup for log4J
    @Before
    public void setUp() throws Exception {
        JUnitUtil.setUp();
        JUnitUtil.resetProfileManager();
        JUnitUtil.initInternalTurnoutManager();
        JUnitUtil.initInternalSensorManager();
    }

    @After
    public void tearDown() throws Exception {
        JUnitUtil.tearDown();
    }

}
