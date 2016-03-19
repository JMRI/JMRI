// SwingShutDownTaskTest.java
package jmri.implementation.swing;

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
 * @version $Revision$
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
                    public boolean checkPromptNeeded() {
                        log.debug("mDST " + modalDialogStopsTest);
                        return !modalDialogStopsTest;
                    }

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
        junit.swingui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(SwingShutDownTaskTest.class);
        return suite;
    }

    // The minimal setup for log4J
    protected void setUp() {
        apps.tests.Log4JFixture.setUp();
    }

    protected void tearDown() {
        apps.tests.Log4JFixture.tearDown();
    }

//    don't want log defined here, as makes the "log" references above ambiguous
//    static protected Logger log = LoggerFactory.getLogger(SwingShutDownTaskTest.class.getName());
}
