package jmri.util.exceptionhandler;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import jmri.util.JUnitAppender;
import jmri.util.JUnitUtil;
import jmri.util.swing.JemmyUtil;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.DisabledIfSystemProperty;

/**
 * Tests for the jmri.util.UncaughtExceptionHandler class.
 *
 * @author Bob Jacobsen Copyright 2010
 */
public class UncaughtExceptionHandlerTest {

    private boolean caught = false;
    private Thread.UncaughtExceptionHandler defaultExceptionHandler;

    @Test
    public void testThread() {
        Thread t = new Thread(() -> {
            throwNullPointerException();
        });
        t.setName("Uncaught Exception Handler Test Thread");
        t.start();
        JUnitUtil.waitFor(JUnitUtil.WAITFOR_DEFAULT_DELAY);
        JUnitAppender.assertErrorMessage("Uncaught Exception caught by jmri.util.exceptionhandler.UncaughtExceptionHandler");
    }

    @Test
    @DisabledIfSystemProperty( named = "java.awt.headless", matches = "true" )
    public void testErrorWithGui(){
        Thread dialogThread = JemmyUtil.createModalDialogOperatorThread(
            Bundle.getMessage("UnrecoverableErrorTitle"), Bundle.getMessage("ButtonOK"));
        Thread t = new Thread(() -> {
            throw new Error("Error Text Foo");
        });
        t.setName("Error Text Foo Thread");
        t.setUncaughtExceptionHandler(new UncaughtHandlerImpl());
        t.start();

        JUnitUtil.waitFor(() -> !dialogThread.isAlive(), "dialog closed");
        JUnitAppender.assertErrorMessageStartsWith("Uncaught Exception caught by jmri.util.exceptionhandler.UncaughtExceptionHandler");

        JUnitUtil.waitFor(() -> !JUnitAppender.getBacklog().isEmpty(),"No Error Msg");
        JUnitAppender.assertErrorMessage("System Exiting");

    }

    @Test
    public void testSwing() throws InterruptedException {
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

    private static class UncaughtHandlerImpl extends UncaughtExceptionHandler {
        @Override
        protected void systemExit() {} // do nothing
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();

        this.defaultExceptionHandler = Thread.getDefaultUncaughtExceptionHandler();
        Thread.setDefaultUncaughtExceptionHandler(new UncaughtHandlerImpl());
    }

    @AfterEach
    public void tearDown() {
        Thread.setDefaultUncaughtExceptionHandler(this.defaultExceptionHandler);
        JUnitUtil.tearDown();

    }
}
