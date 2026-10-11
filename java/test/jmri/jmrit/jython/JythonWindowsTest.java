package jmri.jmrit.jython;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import javax.swing.JFrame;

import jmri.script.swing.InputWindowAction;
import jmri.script.swing.InputWindow;
import jmri.util.JUnitAppender;
import jmri.util.JUnitUtil;
import jmri.util.junit.annotations.DisabledIfHeadless;

import org.junit.jupiter.api.*;

import org.netbeans.jemmy.operators.JFrameOperator;

/**
 * Invokes complete set of tests in the jmri.jmrit.jython tree
 *
 * Some of these tests are here, as they're cross-class functions
 *
 * @author Bob Jacobsen Copyright 2009, 2016
 */
public class JythonWindowsTest {

    // Really a check of Jython init, including the defaults file
    @Test
    @DisabledIfHeadless
    public void testExec() {

        assertEquals(0,JUnitAppender.clearBacklog());
        // open output window
        assertDoesNotThrow( () -> {
            JythonWindow outputWindow = new JythonWindow(); // actually an Action class
            outputWindow.actionPerformed(null);
        }, "exception opening output window: ");

        // create input window
        InputWindow w = new InputWindow();

        // run a null op test
        assertDoesNotThrow( () ->
            w.buttonPressed(), "exception during execution: ");

        // find, close output window
        JFrame f = JFrameOperator.waitJFrame("Script Output", true, true);
        assertNotNull(f, "found output frame");
        JUnitUtil.dispose(f);

        // error messages are a fail
        assertEquals(0, JUnitAppender.clearBacklog(org.slf4j.event.Level.WARN),
            "Emitted WARN messages caused test to fail");

    }

    @Test
    @DisabledIfHeadless
    public void testInput() {
        new InputWindowAction().actionPerformed(null);
        JFrame f = JFrameOperator.findJFrame("Script Entry", true, true);
        assertNotNull(f, "found input frame");
        JUnitUtil.dispose(f);
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetProfileManager();
        JUnitUtil.initDefaultUserMessagePreferences();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.deregisterBlockManagerShutdownTask();
        JUnitUtil.tearDown();
    }
}
