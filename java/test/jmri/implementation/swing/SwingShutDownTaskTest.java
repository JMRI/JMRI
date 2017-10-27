package jmri.implementation.swing;

import jmri.util.JUnitUtil;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Tests for the SwingShutDownTask class.
 * <p>
 * Invoked from higher-level tests, this does not display the modal dialogs that
 * stop execution until clicked/closed. When invoked via its own main() start
 * point, this does show the modal dialogs.
 * <p>
 * Careful - tests are loaded via a separate class loader!
 *
 * @author	Bob Jacobsen Copyright (C) 2008
 */
public class SwingShutDownTaskTest extends TestCase {

    static boolean modalDialogStopsTest = false;
    private final static Logger log = LoggerFactory.getLogger(SwingShutDownTaskTest.class);

    public void testCreate1() {

        // Just display for test
        SwingShutDownTask t = new SwingShutDownTask("SwingShutDownTask Window Check",
                "Do Something quits, click Continue Qutting to quit, Cancel Quit to continue",
                "Do Something and Stop",
                null) {
                    @Override
                    public boolean checkPromptNeeded() {
                        log.debug("mDST " + modalDialogStopsTest);
                        return !modalDialogStopsTest;
                    }
                };

        // and display
        t.execute();

        // Assert.assertTrue("NONE must be zero", 0==Path.NONE);
    }

    public void testCreate2() {

        // Just display for test
        SwingShutDownTask t = new SwingShutDownTask("SwingShutDownTask Window Check",
                "Do Something repeats, click Continue Qutting to quit, Cancel Quit to continue",
                "Do Something and repeats",
                null) {
                    @Override
                    public boolean checkPromptNeeded() {
                        log.debug("mDST " + modalDialogStopsTest);
                        return !modalDialogStopsTest;
                    }

                    @Override
                    public boolean doPrompt() {
                        return false;
                    }
                };

        // and display
        t.execute();

        // Assert.assertTrue("NONE must be zero", 0==Path.NONE);
    }

    // from here down is testing infrastructure
    public SwingShutDownTaskTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        modalDialogStopsTest = true;
        // -noloading needed so we can set the same class-loaded static variable
        String[] testCaseName = {"-noloading", SwingShutDownTaskTest.class.getName()};
        junit.textui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(SwingShutDownTaskTest.class);
        return suite;
    }

    // The minimal setup for log4J
    @Override
    protected void setUp() {
        JUnitUtil.setUp();
    }

    @Override
    protected void tearDown() {
        JUnitUtil.tearDown();
    }

//    don't want log defined here, as makes the "log" references above ambiguous
//    private final static Logger log = LoggerFactory.getLogger(SwingShutDownTaskTest.class);
}
