package jmri.util.exceptionhandler;

import jmri.util.JUnitAppender;
import jmri.util.SwingTestCase;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.junit.After;
import org.junit.Before;

/**
 * Tests for the jmri.util.UncaughtExceptionHandler class.
 *
 * @author	Bob Jacobsen Copyright 2010
 */
public class UncaughtExceptionHandlerTest extends SwingTestCase {

    private boolean caught = false;
    private Thread.UncaughtExceptionHandler defaultExceptionHandler;

    @SuppressWarnings("all") // because we're deliberately forcing an NPE to test the handler
    public void testThread() throws Exception {
        Thread t = new Thread(() -> {
            // null.toString(); will not compile
            // ((Object) null).toString(); raises unnessesary cast warnings
            Object o = null;
            o.toString();
        });
        t.setName("Uncaught Exception Handler Test Thread");
        t.start();
        jmri.util.JUnitUtil.releaseThread(this);
        JUnitAppender.assertErrorMessage("Uncaught Exception caught by jmri.util.exceptionhandler.UncaughtExceptionHandler");
    }

    @SuppressWarnings("all") // because we're deliberately forcing an NPE to test the handler
    public void testSwing() throws Exception {
        try {
            javax.swing.SwingUtilities.invokeAndWait(() -> {
                // null.toString(); will not compile
                // ((Object) null).toString(); raises unnessesary cast warnings
                Object o = null;
                o.toString();
            });
        } catch (java.lang.reflect.InvocationTargetException e) {
            caught = true;
        }
        jmri.util.JUnitUtil.waitFor(() -> {
            return caught;
        }, "threw exception");
        // emits no logging, as the UncaughtExceptionHandlerTest handler isn't invoked
    }

    // from here down is testing infrastructure
    public UncaughtExceptionHandlerTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {UncaughtExceptionHandlerTest.class.getName()};
        junit.textui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(UncaughtExceptionHandlerTest.class);
        return suite;
    }

    @Before
    @Override
    public void setUp() throws Exception {
        apps.tests.Log4JFixture.setUp();
        this.defaultExceptionHandler = Thread.getDefaultUncaughtExceptionHandler();
        Thread.setDefaultUncaughtExceptionHandler(new UncaughtExceptionHandler());
        super.setUp();
    }

    @After
    @Override
    public void tearDown() throws Exception {
        super.tearDown();
        Thread.setDefaultUncaughtExceptionHandler(this.defaultExceptionHandler);
        apps.tests.Log4JFixture.tearDown();
    }
}
