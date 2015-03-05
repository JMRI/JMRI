// UncaughtExceptionHandlerTest.java
package jmri.util.exceptionhandler;

import jmri.util.JUnitAppender;
import jmri.util.SwingTestCase;
import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Tests for the jmri.util.UncaughtExceptionHandler class.
 *
 * @author	Bob Jacobsen Copyright 2010
 * @version	$Revision$
 */
public class UncaughtExceptionHandlerTest extends SwingTestCase {

    public void testThread() throws Exception {
        Thread t = new Thread() {
            public void run() {
                deref(null);
            }

            void deref(Object o) {
                o.toString();
            }
        };
        //log.warn("before pauseAWT");
        pauseAWT();  // can't sleep unless you've paused
        //log.warn("before t.start");
        t.start();
        //log.warn("before sleep");
        sleep(100);
        //log.warn("before assertErrorMessage");
        JUnitAppender.assertErrorMessage("Unhandled Exception: java.lang.NullPointerException");
    }

    public void testSwing() throws Exception {
        boolean caught = false;
        Runnable r = new Runnable() {
            public void run() {
                deref(null);
            }

            void deref(Object o) {
                o.toString();
            }
        };
        try {
            javax.swing.SwingUtilities.invokeAndWait(r);
        } catch (java.lang.reflect.InvocationTargetException e) {
            caught = true;
        }
        flushAWT();
        Assert.assertTrue("threw exception", caught);
    }

    // from here down is testing infrastructure
    public UncaughtExceptionHandlerTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {UncaughtExceptionHandlerTest.class.getName()};
        junit.swingui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(UncaughtExceptionHandlerTest.class);
        return suite;
    }

    // The minimal setup for log4J
    protected void setUp() throws Exception {
        apps.tests.Log4JFixture.setUp();
        super.setUp();
    }

    protected void tearDown() throws Exception {
        super.tearDown();
        apps.tests.Log4JFixture.tearDown();
    }

    static Logger log = LoggerFactory.getLogger(UncaughtExceptionHandlerTest.class.getName());

}
