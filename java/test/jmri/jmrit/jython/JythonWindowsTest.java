package jmri.jmrit.jython;

import java.awt.GraphicsEnvironment;
import javax.swing.JFrame;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;
import org.netbeans.jemmy.operators.JFrameOperator;

/**
 * Invokes complete set of tests in the jmri.jmrit.jython tree
 *
 * Some of these tests are here, as they're cross-class functions
 *
 * @author	Bob Jacobsen Copyright 2009, 2016
 */
public class JythonWindowsTest {

    // Really a check of Jython init, including the defaults file
    @Test
    public void testExec() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        jmri.util.JUnitAppender.clearBacklog();
        // open output window
        JythonWindow outputWindow;  // actually an Action class
        try {
            outputWindow = new JythonWindow();
            outputWindow.actionPerformed(null);
        } catch (Exception e) {
            Assert.fail("exception opening output window: " + e);
        }

        // create input window
        InputWindow w = new InputWindow();

        // run a null op test
        try {
            w.buttonPressed();
        } catch (Exception e) {
            Assert.fail("exception during execution: " + e);
        }

        // find, close output window
        JFrame f = JFrameOperator.waitJFrame("Script Output", true, true);
        Assert.assertNotNull("found output frame", f);
        JUnitUtil.dispose(f);

        // error messages are a fail
        if (jmri.util.JUnitAppender.clearBacklog(org.apache.log4j.Level.WARN) != 0) {
            Assert.fail("Emitted error messages caused test to fail");
        }
    }

    @Test
    public void testInput() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        new InputWindowAction().actionPerformed(null);
        JFrame f = JFrameOperator.findJFrame("Script Entry", true, true);
        Assert.assertNotNull("found input frame", f);
        JUnitUtil.dispose(f);
    }

    @Before
    public void setUp() throws Exception {
        JUnitUtil.setUp();
        JUnitUtil.resetProfileManager();
        jmri.util.JUnitUtil.initDefaultUserMessagePreferences();
    }

    @After
    public void tearDown() throws Exception {
        JUnitUtil.tearDown();
    }
}
