package jmri.util.exceptionhandler;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import jmri.util.JUnitAppender;
import jmri.util.JUnitUtil;

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
    public void testThread() throws Exception {
        Thread t = new Thread(() -> {
            throwNullPointerException();
        });
        t.setName("Uncaught Exception Handler Test Thread");
        t.start();
        JUnitUtil.waitFor(JUnitUtil.WAITFOR_DEFAULT_DELAY);
        JUnitAppender.assertErrorMessage("Uncaught Exception caught by jmri.util.exceptionhandler.UncaughtExceptionHandler");
    }

    @Test
    public void testSwing() throws Exception {
        try {
            javax.swing.SwingUtilities.invokeAndWait(() -> {
                throwNullPointerException();
            });
        } catch (java.lang.reflect.InvocationTargetException e) {
            caught = true;
        }
        JUnitUtil.waitFor(() -> {
            return caught;
        }, "threw exception");
        // emits no logging, as the UncaughtExceptionHandlerTest handler isn't invoked
    }

    @SuppressWarnings("null") // because we're deliberately forcing an NPE to test the handler
    @SuppressFBWarnings( value = {"NP_LOAD_OF_KNOWN_NULL_VALUE","NP_ALWAYS_NULL"},
        justification = "testing exception handler")
    private void throwNullPointerException() {
        // null.toString(); will not compile
        // ((Object) null).toString(); raises unnessesary cast warnings
        Object o = null;
        Assertions.assertNotNull(o.toString());
    }

    @BeforeEach
    public void setUp() throws Exception {
        JUnitUtil.setUp();

        this.defaultExceptionHandler = Thread.getDefaultUncaughtExceptionHandler();
        Thread.setDefaultUncaughtExceptionHandler(new UncaughtExceptionHandler());
    }

    @AfterEach
    public void tearDown() throws Exception {
        Thread.setDefaultUncaughtExceptionHandler(this.defaultExceptionHandler);
        JUnitUtil.tearDown();

    }
}
