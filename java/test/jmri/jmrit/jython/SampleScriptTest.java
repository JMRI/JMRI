package jmri.jmrit.jython;

import javax.swing.JFrame;
import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Invokes Python-language scripts in jython/tests
 *
 * @author	Bob Jacobsen Copyright 2016
 */
public class SampleScriptTest extends TestCase {

    // This is just a placeholder now.
    public void testExec() {
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
        JFrame f = jmri.util.JmriJFrame.getFrame("Script Output");
        Assert.assertTrue("found output frame", f != null);
        f.dispose();

        // error messages are a fail
        if (jmri.util.JUnitAppender.clearBacklog() != 0) {
            Assert.fail("Emitted error messages caused test to fail");
        }
    }

    public void testInput() {
        new InputWindowAction().actionPerformed(null);
        JFrame f = jmri.util.JmriJFrame.getFrame("Script Entry");
        Assert.assertTrue("found input frame", f != null);
        f.dispose();
    }

    // from here down is testing infrastructure
    public SampleScriptTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {"-noloading", SampleScriptTest.class.getName()};
        junit.swingui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(SampleScriptTest.class);
        return suite;
    }

    // The minimal setup for log4J
    protected void setUp() throws Exception {
        apps.tests.Log4JFixture.setUp();
        super.setUp();
        
        jmri.util.JUnitUtil.resetInstanceManager();
        jmri.util.JUnitUtil.initDefaultUserMessagePreferences();
    }

    protected void tearDown() throws Exception {
        jmri.util.JUnitUtil.resetInstanceManager();
        super.tearDown();
        apps.tests.Log4JFixture.tearDown();
    }
}
