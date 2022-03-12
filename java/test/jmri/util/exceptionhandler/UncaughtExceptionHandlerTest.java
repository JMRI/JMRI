package jmri.util.exceptionhandler;

import jmri.util.JUnitAppender;

import org.junit.jupiter.api.*;

/**
 * Tests for the jmri.util.UncaughtExceptionHandler class.
 *
 * @author Bob Jacobsen Copyright 2010
 */
public class UncaughtExceptionHandlerTest {

    private boolean caught = false;
    private Thread.UncaughtExceptionHandler defaultExceptionHandler;

    @Test
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

    @Test
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

    @BeforeEach
    public void setUp() throws Exception {
        jmri.util.JUnitUtil.setUp();

        this.defaultExceptionHandler = Thread.getDefaultUncaughtExceptionHandler();
        Thread.setDefaultUncaughtExceptionHandler(new UncaughtExceptionHandler());
    }

    @AfterEach
    public void tearDown() throws Exception {
        Thread.setDefaultUncaughtExceptionHandler(this.defaultExceptionHandler);
        jmri.util.JUnitUtil.tearDown();

    }
}
