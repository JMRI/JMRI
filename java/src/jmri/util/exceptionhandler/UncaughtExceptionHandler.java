package jmri.util.exceptionhandler;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import java.awt.GraphicsEnvironment;

import jmri.util.swing.ExceptionContext;

/**
 * Class to log exceptions that rise to the top of threads, including to the top
 * of the AWT event processing loop.
 *
 * Using code must install this with
 * <pre>
 * Thread.setDefaultUncaughtExceptionHandler(new UncaughtExceptionHandler());
 * </pre>
 *
 * @author Bob Jacobsen Copyright 2003, 2010
 */
public class UncaughtExceptionHandler implements Thread.UncaughtExceptionHandler {

    @Override
    public void uncaughtException(Thread t, Throwable e) {

        // see http://docs.oracle.com/javase/7/docs/api/java/lang/ThreadDeath.html
        // 
        // The type ThreadDeath has been deprecated since version 20 and marked for removal
        // and the warning cannot be suppressed in Java 21. But external libraries might
        // throw the exception outside of JMRI control. So check the name of the exception
        // instead of using "instanceof".
        if ("java.lang.ThreadDeath".equals(e.getClass().getName())) {
            log.info("Thread has stopped: {}", t.getName());
            return;
        }

        log.error("Uncaught Exception caught by jmri.util.exceptionhandler.UncaughtExceptionHandler", e);

        if (e instanceof Error) {
            if (!GraphicsEnvironment.isHeadless()) {
                jmri.util.swing.ExceptionDisplayFrame.displayExceptionDisplayFrame(null,
                    new ErrorContext(e));
            }
            log.error("System Exiting");
            systemExit();
        }
    }

    @SuppressFBWarnings(value="DM_EXIT", justification="Errors should terminate the application")
    protected void systemExit(){
        System.exit(126);
    }

    private static class ErrorContext extends ExceptionContext {

        public ErrorContext(@javax.annotation.Nonnull Throwable ex) {
            super(ex, "", "");
            this.prefaceString = Bundle.getMessage("UnrecoverableErrorMessage");
        }

        @Override
        public String getTitle() {
            return Bundle.getMessage("UnrecoverableErrorTitle");
        }

    }

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(UncaughtExceptionHandler.class);

}
