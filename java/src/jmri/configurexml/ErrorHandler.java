package jmri.configurexml;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Default operation for reporting errors while loading.
 *
 * @author Bob Jacobsen Copyright (c) 2010
 */
public class ErrorHandler {

    /**
     * Handle an error.
     * <p>
     * Default implementation formats and puts in log.
     *
     * @param e the error
     */
    public void handle(ErrorMemo e) {
        StringBuilder m = new StringBuilder(e.description);
        if (e.systemName != null) {
            m.append(" System name \"").append(e.systemName).append("\"");
        }
        if (e.userName != null && !e.userName.equals("")) {
            m.append(" User name \"").append(e.userName).append("\"");
        }
        if (e.operation != null) {
            m.append(" while ").append(e.operation);
        }
        if (e.adapter != null) {
            m.append(" in adaptor of type ").append(e.adapter.getClass().getName());
        }
        if (e.exception != null) {
            m.append(" Exception: ").append(e.exception.toString());
        }
        m.append("\nSee http://jmri.org/help/en/package/jmri/configurexml/ErrorHandler.shtml for possibly more information.");
        if (e.exception != null) {
            log.error(m.toString(), e.exception);
        } else {
            log.error(m.toString());
        }
    }

    /**
     * Invoked when operation complete.
     * <p>
     * Default implementation doesn't do anything here, everything already
     * logged above.
     */
        @edu.umd.cs.findbugs.annotations.SuppressFBWarnings(value = "NM_CONFUSING", 
            justification = "Seems to be a false positive due to jmri.jmris.simpleserver.parser.SimpleCharStream.Done()")
    public void done() {
    }

    // initialize logging
    private final static Logger log = LoggerFactory.getLogger(ErrorHandler.class);
}
