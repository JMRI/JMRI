package jmri.util.exceptionhandler;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.awt.GraphicsEnvironment;
import java.io.PrintWriter;
import java.io.StringWriter;
import javax.swing.JOptionPane;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
    @SuppressFBWarnings(value="DM_EXIT", justification="Errors should terminate the application")
    public void uncaughtException(Thread t, Throwable e) {

        // see http://docs.oracle.com/javase/7/docs/api/java/lang/ThreadDeath.html
        if (e instanceof java.lang.ThreadDeath) {
            log.info("Thread has stopped: {}", t.getName());
            return;
        }

        log.error("Uncaught Exception caught by jmri.util.exceptionhandler.UncaughtExceptionHandler", e);

        if (e instanceof Error) {
            if (!GraphicsEnvironment.isHeadless()) {
                JOptionPane.showMessageDialog(null,
                        Bundle.getMessage("UnrecoverableErrorMessage", generateStackTrace(e)),
                        Bundle.getMessage("UnrecoverableErrorTitle"),
                        JOptionPane.ERROR_MESSAGE);
            }
            System.exit(126);
        }
    }

    static protected String generateStackTrace(Throwable e) {
        StringWriter writer = new StringWriter();
        e.printStackTrace(new PrintWriter(writer));
        return writer.toString();
    }

    private final static Logger log = LoggerFactory.getLogger(UncaughtExceptionHandler.class);
}
