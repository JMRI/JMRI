package jmri.jmrit.display.layoutEditor;

import jmri.configurexml.ConfigXmlManager;
import jmri.jmrit.display.EditorFrameOperator;
import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.DisabledIfSystemProperty;

/**
 * Swing tests for the LayoutEditor.
 *
 * @author Bob Jacobsen Copyright 2009, 2010
 */
public class LayoutEditorWindowTest {

    private ConfigXmlManager cm = null;

    @Test
    @DisabledIfSystemProperty(named = "java.awt.headless", matches = "true")
    public void testShowAndClose() throws Exception {

        // load and display sample file
        java.io.File f = new java.io.File("java/test/jmri/jmrit/display/layoutEditor/valid/SimpleLayoutEditorTest.xml");
        Assertions.assertNotNull(cm);
        cm.load(f);

        // Find new window by name (should be more distinctive, comes from sample file)
        EditorFrameOperator to = new EditorFrameOperator("My Layout");

        // It's up at this point, and can be manipulated
        // Ask to close window
        to.closeFrameWithConfirmations();

        EditorFrameOperator.clearEditorFrameOperatorThreads();

    }

    // Setup for log4J
    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetProfileManager();
        JUnitUtil.initInternalTurnoutManager();
        JUnitUtil.initInternalSensorManager();
        cm = new jmri.configurexml.ConfigXmlManager() {
        };
    }

    @AfterEach
    public void tearDown() {
        cm = null;
        JUnitUtil.deregisterBlockManagerShutdownTask();
        JUnitUtil.tearDown();
    }

}
